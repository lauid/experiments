package com.example.kdemo.service;

import com.example.kdemo.config.PrometheusConfig;
import com.example.kdemo.dto.PrometheusBatchQueryResponse;
import com.example.kdemo.dto.PrometheusBatchRangeQueryRequest;
import com.example.kdemo.dto.PrometheusQueryRequest;
import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import com.example.kdemo.repository.PrometheusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Prometheus服务实现类
 * 优化版本：使用专用线程池和批量处理策略
 */
@Service
public class PrometheusServiceImpl implements PrometheusService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrometheusServiceImpl.class);
    
    private final PrometheusRepository prometheusRepository;
    private final PrometheusConfig config;
    private final ExecutorService prometheusQueryExecutor;
    
    @Autowired
    public PrometheusServiceImpl(PrometheusRepository prometheusRepository, 
                               PrometheusConfig config,
                               @Qualifier("prometheusQueryExecutor") ExecutorService prometheusQueryExecutor) {
        this.prometheusRepository = prometheusRepository;
        this.config = config;
        this.prometheusQueryExecutor = prometheusQueryExecutor;
    }
    
    @Override
    public PrometheusBatchQueryResponse batchQuery(String cluster, PrometheusQueryRequest request) throws PrometheusException {
        if (cluster == null || cluster.isEmpty()) {
            cluster = request.getCluster();
        }
        final String finalCluster = cluster;
        
        logger.info("Starting optimized batch query for cluster {} with {} metrics", finalCluster, request.getQueries().size());
        long startTime = System.currentTimeMillis();
        
        try {
            // 使用批量处理策略
            return processBatchQueries(finalCluster, request, startTime);
        } catch (Exception e) {
            logger.error("Batch query failed for cluster {}: {}", finalCluster, e.getMessage());
            throw new PrometheusException("Batch query execution failed: " + e.getMessage(), "BATCH_EXECUTION_ERROR", null, e);
        }
    }
    
    /**
     * 批量处理查询，防止线程池爆炸
     */
    private PrometheusBatchQueryResponse processBatchQueries(String cluster, PrometheusQueryRequest request, long startTime) {
        List<PrometheusQueryRequest.MetricQuery> queries = request.getQueries();
        int batchSize = config.getBatchSize();
        
        List<PrometheusBatchQueryResponse.MetricResult> allResults = new ArrayList<>();
        List<PrometheusBatchQueryResponse.QueryError> allErrors = new ArrayList<>();
        
        // 分批处理
        for (int i = 0; i < queries.size(); i += batchSize) {
            int end = Math.min(i + batchSize, queries.size());
            List<PrometheusQueryRequest.MetricQuery> batch = queries.subList(i, end);
            
            // 处理这一批
            PrometheusBatchQueryResponse batchResponse = processBatch(cluster, batch);
            allResults.addAll(batchResponse.getData());
            allErrors.addAll(batchResponse.getErrors());
        }
        
        // 确定最终状态
        String status = determineBatchStatus(allResults, allErrors);
        
        logger.info("Batch query completed for cluster {}. Successful: {}, Failed: {}, Total time: {}ms", 
                cluster, allResults.size(), allErrors.size(), System.currentTimeMillis() - startTime);
        
        return new PrometheusBatchQueryResponse(status, allResults, allErrors);
    }
    
    /**
     * 处理单个查询批次
     */
    private PrometheusBatchQueryResponse processBatch(String cluster, List<PrometheusQueryRequest.MetricQuery> batchQueries) {
        List<PrometheusBatchQueryResponse.MetricResult> results = new ArrayList<>();
        List<PrometheusBatchQueryResponse.QueryError> errors = new ArrayList<>();
        
        // 使用信号量控制并发数
        Semaphore semaphore = new Semaphore(config.getMaxConcurrency());
        
        // 创建CompletableFuture列表
        List<CompletableFuture<QueryResult>> futures = batchQueries.stream()
                .map(metricQuery -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                        return executeSingleQueryWithTimeout(cluster, metricQuery);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new QueryResult(null, new PrometheusBatchQueryResponse.QueryError(
                                metricQuery.getName(),
                                metricQuery.getQuery(),
                                "Query interrupted",
                                "INTERRUPTED"
                        ));
                    } catch (Exception e) {
                        return new QueryResult(null, new PrometheusBatchQueryResponse.QueryError(
                                metricQuery.getName(),
                                metricQuery.getQuery(),
                                "Query execution failed: " + e.getMessage(),
                                "EXECUTION_ERROR"
                        ));
                    } finally {
                        semaphore.release();
                    }
                }, prometheusQueryExecutor)) // 使用专用线程池
                .collect(Collectors.toList());
        
        // 等待所有查询完成，带超时控制
        for (int i = 0; i < futures.size(); i++) {
            try {
                QueryResult queryResult = futures.get(i).get(config.getIndividualQueryTimeout(), TimeUnit.MILLISECONDS);
                if (queryResult.result != null) {
                    results.add(queryResult.result);
                } else if (queryResult.error != null) {
                    errors.add(queryResult.error);
                }
            } catch (TimeoutException e) {
                PrometheusQueryRequest.MetricQuery query = batchQueries.get(i);
                errors.add(new PrometheusBatchQueryResponse.QueryError(
                        query.getName(),
                        query.getQuery(),
                        "Query timeout after " + config.getIndividualQueryTimeout() + "ms",
                        "TIMEOUT"
                ));
            } catch (InterruptedException | ExecutionException e) {
                PrometheusQueryRequest.MetricQuery query = batchQueries.get(i);
                errors.add(new PrometheusBatchQueryResponse.QueryError(
                        query.getName(),
                        query.getQuery(),
                        "Query execution failed: " + e.getMessage(),
                        "EXECUTION_ERROR"
                ));
            }
        }
        
        return new PrometheusBatchQueryResponse("success", results, errors);
    }
    
    /**
     * 执行单个查询（带超时）
     */
    private QueryResult executeSingleQueryWithTimeout(String cluster, PrometheusQueryRequest.MetricQuery metricQuery) {
        long queryStartTime = System.currentTimeMillis();
        try {
            PrometheusQueryResponse result = executeSingleQuery(cluster, metricQuery);
            PrometheusBatchQueryResponse.MetricResult metricResult = new PrometheusBatchQueryResponse.MetricResult(
                    metricQuery.getName(),
                    metricQuery.getDescription(),
                    metricQuery.getQuery(),
                    result,
                    System.currentTimeMillis() - queryStartTime
            );
            return new QueryResult(metricResult, null);
        } catch (Exception e) {
            logger.error("Query failed for metric {}: {}", metricQuery.getName(), e.getMessage());
            return new QueryResult(null, new PrometheusBatchQueryResponse.QueryError(
                    metricQuery.getName(),
                    metricQuery.getQuery(),
                    "Query execution failed: " + e.getMessage(),
                    "EXECUTION_ERROR"
            ));
        }
    }
    
    /**
     * 确定批量查询状态
     */
    private String determineBatchStatus(List<PrometheusBatchQueryResponse.MetricResult> results, 
                                      List<PrometheusBatchQueryResponse.QueryError> errors) {
        if (results.isEmpty() && !errors.isEmpty()) {
            return "failed";
        } else if (!errors.isEmpty()) {
            return "partial_success";
        } else {
            return "success";
        }
    }
    
    @Override
    public PrometheusBatchQueryResponse batchQueryRange(String cluster, PrometheusBatchRangeQueryRequest request) throws PrometheusException {
        if (cluster == null || cluster.isEmpty()) {
            cluster = request.getCluster();
        }
        final String finalCluster = cluster;
        
        logger.info("Starting optimized batch range query for cluster {} with {} metrics", finalCluster, request.getQueries().size());
        long startTime = System.currentTimeMillis();
        
        try {
            // 使用类似的批量处理策略
            return processBatchRangeQueries(finalCluster, request, startTime);
        } catch (Exception e) {
            logger.error("Batch range query failed for cluster {}: {}", finalCluster, e.getMessage());
            throw new PrometheusException("Batch range query execution failed: " + e.getMessage(), "BATCH_RANGE_EXECUTION_ERROR", null, e);
        }
    }
    
    /**
     * 批量处理范围查询
     */
    private PrometheusBatchQueryResponse processBatchRangeQueries(String cluster, PrometheusBatchRangeQueryRequest request, long startTime) {
        List<PrometheusBatchRangeQueryRequest.RangeMetricQuery> queries = request.getQueries();
        List<PrometheusBatchQueryResponse.MetricResult> allResults = new ArrayList<>();
        List<PrometheusBatchQueryResponse.QueryError> allErrors = new ArrayList<>();
        
        // 分批处理查询
        int batchSize = config.getBatchSize();
        int totalBatches = (queries.size() + batchSize - 1) / batchSize;
        
        for (int i = 0; i < totalBatches; i++) {
            int fromIndex = i * batchSize;
            int toIndex = Math.min(fromIndex + batchSize, queries.size());
            List<PrometheusBatchRangeQueryRequest.RangeMetricQuery> batchQueries = queries.subList(fromIndex, toIndex);
            
            try {
                BatchResult batchResult = processRangeQueryBatch(cluster, batchQueries, request, startTime);
                allResults.addAll(batchResult.results);
                allErrors.addAll(batchResult.errors);
            } catch (Exception e) {
                logger.error("Range batch {}/{} processing failed: {}", i + 1, totalBatches, e.getMessage());
                for (PrometheusBatchRangeQueryRequest.RangeMetricQuery query : batchQueries) {
                    allErrors.add(new PrometheusBatchQueryResponse.QueryError(
                            query.getName(),
                            query.getQuery(),
                            "Range batch processing failed: " + e.getMessage(),
                            "RANGE_BATCH_ERROR"
                    ));
                }
            }
        }
        
        String status = determineBatchStatus(allResults, allErrors);
        
        logger.info("Batch range query completed for cluster {}. Successful: {}, Failed: {}, Total time: {}ms", 
                cluster, allResults.size(), allErrors.size(), System.currentTimeMillis() - startTime);
        
        return new PrometheusBatchQueryResponse(status, allResults, allErrors);
    }
    
    /**
     * 处理单个范围查询批次
     */
    private BatchResult processRangeQueryBatch(String cluster, List<PrometheusBatchRangeQueryRequest.RangeMetricQuery> batchQueries, 
                                             PrometheusBatchRangeQueryRequest request, long startTime) {
        List<PrometheusBatchQueryResponse.MetricResult> results = new ArrayList<>();
        List<PrometheusBatchQueryResponse.QueryError> errors = new ArrayList<>();
        
        Semaphore semaphore = new Semaphore(config.getMaxConcurrency());
        
        List<CompletableFuture<QueryResult>> futures = batchQueries.stream()
                .map(rangeMetricQuery -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                        return executeSingleRangeQueryWithTimeout(cluster, rangeMetricQuery, request, startTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new QueryResult(null, new PrometheusBatchQueryResponse.QueryError(
                                rangeMetricQuery.getName(),
                                rangeMetricQuery.getQuery(),
                                "Range query interrupted",
                                "INTERRUPTED"
                        ));
                    } catch (Exception e) {
                        return new QueryResult(null, new PrometheusBatchQueryResponse.QueryError(
                                rangeMetricQuery.getName(),
                                rangeMetricQuery.getQuery(),
                                "Range query execution failed: " + e.getMessage(),
                                "EXECUTION_ERROR"
                        ));
                    } finally {
                        semaphore.release();
                    }
                }, prometheusQueryExecutor))
                .collect(Collectors.toList());
        
        for (int i = 0; i < futures.size(); i++) {
            try {
                QueryResult queryResult = futures.get(i).get(config.getIndividualQueryTimeout(), TimeUnit.MILLISECONDS);
                if (queryResult.result != null) {
                    results.add(queryResult.result);
                } else if (queryResult.error != null) {
                    errors.add(queryResult.error);
                }
            } catch (TimeoutException e) {
                PrometheusBatchRangeQueryRequest.RangeMetricQuery query = batchQueries.get(i);
                errors.add(new PrometheusBatchQueryResponse.QueryError(
                        query.getName(),
                        query.getQuery(),
                        "Range query timeout after " + config.getIndividualQueryTimeout() + "ms",
                        "TIMEOUT"
                ));
            } catch (InterruptedException | ExecutionException e) {
                PrometheusBatchRangeQueryRequest.RangeMetricQuery query = batchQueries.get(i);
                errors.add(new PrometheusBatchQueryResponse.QueryError(
                        query.getName(),
                        query.getQuery(),
                        "Range query execution failed: " + e.getMessage(),
                        "EXECUTION_ERROR"
                ));
            }
        }
        
        return new BatchResult(results, errors);
    }
    
    /**
     * 执行单个范围查询（带超时）
     */
    private QueryResult executeSingleRangeQueryWithTimeout(String cluster, PrometheusBatchRangeQueryRequest.RangeMetricQuery rangeMetricQuery, 
                                                         PrometheusBatchRangeQueryRequest request, long startTime) {
        long queryStartTime = System.currentTimeMillis();
        try {
            PrometheusQueryResponse result = executeSingleRangeQuery(cluster, rangeMetricQuery, request);
            PrometheusBatchQueryResponse.MetricResult metricResult = new PrometheusBatchQueryResponse.MetricResult(
                    rangeMetricQuery.getName(),
                    rangeMetricQuery.getDescription(),
                    rangeMetricQuery.getQuery(),
                    result,
                    System.currentTimeMillis() - queryStartTime
            );
            return new QueryResult(metricResult, null);
        } catch (Exception e) {
            logger.error("Range query failed for metric {}: {}", rangeMetricQuery.getName(), e.getMessage());
            return new QueryResult(null, new PrometheusBatchQueryResponse.QueryError(
                    rangeMetricQuery.getName(),
                    rangeMetricQuery.getQuery(),
                    "Range query execution failed: " + e.getMessage(),
                    "EXECUTION_ERROR"
            ));
        }
    }
    
    @Override
    public PrometheusQueryResponse queryRange(String cluster, String query, String startTime, String endTime, String step) 
            throws PrometheusException {
        return prometheusRepository.queryRange(cluster, query, startTime, endTime, step);
    }
    
    @Override
    public PrometheusQueryResponse query(String cluster, String query, String time) throws PrometheusException {
        return prometheusRepository.query(cluster, query, time);
    }
    
    @Override
    public Boolean checkConnection(String cluster) throws PrometheusException {
        return prometheusRepository.checkConnection(cluster);
    }
    
    @Override
    public String getVersion(String cluster) throws PrometheusException {
        return prometheusRepository.getVersion(cluster);
    }
    
    @Override
    public List<PrometheusQueryRequest.MetricQuery> getMetricTemplates() {
        List<PrometheusQueryRequest.MetricQuery> templates = new ArrayList<>();
        
        // CPU使用率
        templates.add(new PrometheusQueryRequest.MetricQuery(
                "cpu_usage",
                "rate(container_cpu_usage_seconds_total{container!=\"\"}[5m]) * 100",
                "Container CPU usage percentage",
                new HashMap<>()
        ));
        
        // 内存使用率
        templates.add(new PrometheusQueryRequest.MetricQuery(
                "memory_usage",
                "container_memory_usage_bytes{container!=\"\"} / container_spec_memory_limit_bytes{container!=\"\"} * 100",
                "Container memory usage percentage",
                new HashMap<>()
        ));
        
        // 网络流量
        templates.add(new PrometheusQueryRequest.MetricQuery(
                "network_traffic",
                "rate(container_network_receive_bytes_total{container!=\"\"}[5m])",
                "Container network receive traffic",
                new HashMap<>()
        ));
        
        // 磁盘IO
        templates.add(new PrometheusQueryRequest.MetricQuery(
                "disk_io",
                "rate(container_fs_reads_bytes_total{container!=\"\"}[5m])",
                "Container disk read operations",
                new HashMap<>()
        ));
        
        // Pod状态
        templates.add(new PrometheusQueryRequest.MetricQuery(
                "pod_status",
                "kube_pod_status_phase",
                "Kubernetes pod status",
                new HashMap<>()
        ));
        
        // 节点资源使用
        templates.add(new PrometheusQueryRequest.MetricQuery(
                "node_cpu",
                "100 - (avg by (instance) (irate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)",
                "Node CPU usage percentage",
                new HashMap<>()
        ));
        
        return templates;
    }
    
    /**
     * 执行单个查询
     */
    private PrometheusQueryResponse executeSingleQuery(
            String cluster,
            PrometheusQueryRequest.MetricQuery metricQuery) {
        long startTime = System.currentTimeMillis();
        if (metricQuery.getQuery() != null) {
            PrometheusQueryResponse response = prometheusRepository.query(cluster, metricQuery.getQuery(), null);
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Instant query completed for {} in {}ms", metricQuery.getName(), duration);
            return response;
        } else {
            throw new IllegalArgumentException("Query expression cannot be null");
        }
    }
    
    /**
     * 执行单个范围查询
     */
    private PrometheusQueryResponse executeSingleRangeQuery(
            String cluster,
            PrometheusBatchRangeQueryRequest.RangeMetricQuery rangeMetricQuery, 
            PrometheusBatchRangeQueryRequest request) {
        long startTime = System.currentTimeMillis();
        if (request.getStartTime() != null && request.getEndTime() != null && request.getStep() != null) {
            PrometheusQueryResponse response = prometheusRepository.queryRange(
                    cluster,
                    rangeMetricQuery.getQuery(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getStep()
            );
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Range query completed for {} in {}ms", rangeMetricQuery.getName(), duration);
            return response;
        } else {
            throw new IllegalArgumentException("Start time, end time, and step cannot be null for range queries");
        }
    }
    
    /**
     * 查询结果包装类
     */
    private static class QueryResult {
        final PrometheusBatchQueryResponse.MetricResult result;
        final PrometheusBatchQueryResponse.QueryError error;
        
        QueryResult(PrometheusBatchQueryResponse.MetricResult result, PrometheusBatchQueryResponse.QueryError error) {
            this.result = result;
            this.error = error;
        }
    }
    
    /**
     * 批量结果包装类
     */
    private static class BatchResult {
        final List<PrometheusBatchQueryResponse.MetricResult> results;
        final List<PrometheusBatchQueryResponse.QueryError> errors;
        
        BatchResult(List<PrometheusBatchQueryResponse.MetricResult> results, List<PrometheusBatchQueryResponse.QueryError> errors) {
            this.results = results;
            this.errors = errors;
        }
    }
} 