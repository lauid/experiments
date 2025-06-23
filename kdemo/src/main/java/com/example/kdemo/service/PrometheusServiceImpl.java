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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * Prometheus服务实现类
 */
@Service
public class PrometheusServiceImpl implements PrometheusService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrometheusServiceImpl.class);
    
    private final PrometheusRepository prometheusRepository;
    private final PrometheusConfig config;
    
    @Autowired
    public PrometheusServiceImpl(PrometheusRepository prometheusRepository, PrometheusConfig config) {
        this.prometheusRepository = prometheusRepository;
        this.config = config;
    }
    
    @Override
    public PrometheusBatchQueryResponse batchQuery(String cluster, PrometheusQueryRequest request) throws PrometheusException {
        if (cluster == null || cluster.isEmpty()) {
            cluster = request.getCluster();
        }
        final String finalCluster = cluster;
        logger.info("Starting batch query for cluster {} with {} metrics", finalCluster, request.getQueries().size());
        long startTime = System.currentTimeMillis();
        List<PrometheusBatchQueryResponse.MetricResult> results = new ArrayList<>();
        List<PrometheusBatchQueryResponse.QueryError> errors = new ArrayList<>();
        Semaphore semaphore = new Semaphore(config.getMaxConcurrency());
        List<CompletableFuture<PrometheusBatchQueryResponse.MetricResult>> futures = request.getQueries().stream()
                .map(metricQuery -> CompletableFuture.supplyAsync(() -> {
                    try {
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new PrometheusException("Semaphore acquire interrupted", "INTERRUPTED", null, ie);
                        }
                        PrometheusQueryResponse result = executeSingleQuery(finalCluster, metricQuery, request);
                        return new PrometheusBatchQueryResponse.MetricResult(
                                metricQuery.getName(),
                                metricQuery.getDescription(),
                                metricQuery.getQuery(),
                                result,
                                System.currentTimeMillis() - startTime
                        );
                    } catch (Exception e) {
                        logger.error("Query failed for metric {}: {}", metricQuery.getName(), e.getMessage());
                        return null;
                    } finally {
                        semaphore.release();
                    }
                })).collect(Collectors.toList());

        for (int i = 0; i < futures.size(); i++) {
            try {
                PrometheusBatchQueryResponse.MetricResult result = futures.get(i).get();
                if (result != null) {
                    results.add(result);
                } else {
                    PrometheusQueryRequest.MetricQuery query = request.getQueries().get(i);
                    errors.add(new PrometheusBatchQueryResponse.QueryError(
                            query.getName(),
                            query.getQuery(),
                            "Query execution failed",
                            "EXECUTION_ERROR"
                    ));
                }
            } catch (InterruptedException | ExecutionException e) {
                PrometheusQueryRequest.MetricQuery query = request.getQueries().get(i);
                errors.add(new PrometheusBatchQueryResponse.QueryError(
                        query.getName(),
                        query.getQuery(),
                        "Query execution failed: " + e.getMessage(),
                        "EXECUTION_ERROR"
                ));
            }
        }

        String status = errors.isEmpty() ? "success" : "partial_success";
        if (results.isEmpty()) {
            status = "failed";
        }
        logger.info("Batch query completed for cluster {}. Successful: {}, Failed: {}", 
                finalCluster, results.size(), errors.size());
        return new PrometheusBatchQueryResponse(status, results, errors);
    }
    
    @Override
    public PrometheusBatchQueryResponse batchQueryRange(String cluster, PrometheusBatchRangeQueryRequest request) throws PrometheusException {
        if (cluster == null || cluster.isEmpty()) {
            cluster = request.getCluster();
        }
        final String finalCluster = cluster;
        logger.info("Starting batch range query for cluster {} with {} metrics", finalCluster, request.getQueries().size());
        long startTime = System.currentTimeMillis();
        List<PrometheusBatchQueryResponse.MetricResult> results = new ArrayList<>();
        List<PrometheusBatchQueryResponse.QueryError> errors = new ArrayList<>();
        Semaphore semaphore = new Semaphore(config.getMaxConcurrency());
        List<CompletableFuture<PrometheusBatchQueryResponse.MetricResult>> futures = request.getQueries().stream()
                .map(rangeMetricQuery -> CompletableFuture.supplyAsync(() -> {
                    try {
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new PrometheusException("Semaphore acquire interrupted", "INTERRUPTED", null, ie);
                        }
                        PrometheusQueryResponse result = executeSingleRangeQuery(finalCluster, rangeMetricQuery, request);
                        return new PrometheusBatchQueryResponse.MetricResult(
                                rangeMetricQuery.getName(),
                                rangeMetricQuery.getDescription(),
                                rangeMetricQuery.getQuery(),
                                result,
                                System.currentTimeMillis() - startTime
                        );
                    } catch (Exception e) {
                        logger.error("Range query failed for metric {}: {}", rangeMetricQuery.getName(), e.getMessage());
                        return null;
                    } finally {
                        semaphore.release();
                    }
                })).collect(Collectors.toList());

        for (int i = 0; i < futures.size(); i++) {
            try {
                PrometheusBatchQueryResponse.MetricResult result = futures.get(i).get();
                if (result != null) {
                    results.add(result);
                } else {
                    PrometheusBatchRangeQueryRequest.RangeMetricQuery query = request.getQueries().get(i);
                    errors.add(new PrometheusBatchQueryResponse.QueryError(
                            query.getName(),
                            query.getQuery(),
                            "Range query execution failed",
                            "EXECUTION_ERROR"
                    ));
                }
            } catch (InterruptedException | ExecutionException e) {
                PrometheusBatchRangeQueryRequest.RangeMetricQuery query = request.getQueries().get(i);
                errors.add(new PrometheusBatchQueryResponse.QueryError(
                        query.getName(),
                        query.getQuery(),
                        "Range query execution failed: " + e.getMessage(),
                        "EXECUTION_ERROR"
                ));
            }
        }

        String status = errors.isEmpty() ? "success" : "partial_success";
        if (results.isEmpty()) {
            status = "failed";
        }
        logger.info("Batch range query completed for cluster {}. Successful: {}, Failed: {}", 
                finalCluster, results.size(), errors.size());
        return new PrometheusBatchQueryResponse(status, results, errors);
    }
    
    @Override
    public PrometheusQueryResponse queryRange(String cluster, String query, String startTime, String endTime, String step) throws PrometheusException {
        logger.debug("Executing range query for cluster {}: {} from {} to {} with step {}", cluster, query, startTime, endTime, step);
        return prometheusRepository.queryRange(cluster, query, startTime, endTime, step);
    }
    
    @Override
    public PrometheusQueryResponse query(String cluster, String query, String time) throws PrometheusException {
        logger.debug("Executing instant query for cluster {}: {} at time {}", cluster, query, time);
        return prometheusRepository.query(cluster, query, time);
    }
    
    @Override
    public Boolean checkConnection(String cluster) throws PrometheusException {
        logger.debug("Checking Prometheus connection for cluster {}", cluster);
        return prometheusRepository.checkConnection(cluster);
    }
    
    @Override
    public String getVersion(String cluster) throws PrometheusException {
        logger.debug("Getting Prometheus version for cluster {}", cluster);
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
            PrometheusQueryRequest.MetricQuery metricQuery, 
            PrometheusQueryRequest request) {
        long startTime = System.currentTimeMillis();
        if (request.getStartTime() != null && request.getEndTime() != null && request.getStep() != null) {
            PrometheusQueryResponse response = prometheusRepository.queryRange(
                    cluster,
                    metricQuery.getQuery(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getStep()
            );
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Range query completed for {} in {}ms", metricQuery.getName(), duration);
            return response;
        } else {
            PrometheusQueryResponse response = prometheusRepository.query(cluster, metricQuery.getQuery(), null);
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Instant query completed for {} in {}ms", metricQuery.getName(), duration);
            return response;
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
        
        // 优先使用查询级别的时间参数，如果没有则使用请求级别的时间参数
        String startTimeParam = rangeMetricQuery.getStartTime() != null ? 
                rangeMetricQuery.getStartTime() : request.getStartTime();
        String endTimeParam = rangeMetricQuery.getEndTime() != null ? 
                rangeMetricQuery.getEndTime() : request.getEndTime();
        String stepParam = rangeMetricQuery.getStep() != null ? 
                rangeMetricQuery.getStep() : request.getStep();
        
        if (startTimeParam == null || endTimeParam == null || stepParam == null) {
            throw new PrometheusException(
                    "Missing required time parameters for range query: " + rangeMetricQuery.getName(),
                    "PARAMETER_ERROR",
                    rangeMetricQuery.getQuery(),
                    null);
        }
        
        PrometheusQueryResponse response = prometheusRepository.queryRange(
                cluster,
                rangeMetricQuery.getQuery(),
                startTimeParam,
                endTimeParam,
                stepParam
        );
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("Range query completed for {} in {}ms", rangeMetricQuery.getName(), duration);
        return response;
    }
} 