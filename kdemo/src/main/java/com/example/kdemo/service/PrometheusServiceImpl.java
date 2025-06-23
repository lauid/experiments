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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Mono<PrometheusBatchQueryResponse> batchQuery(String cluster, PrometheusQueryRequest request) throws PrometheusException {
        if (cluster == null || cluster.isEmpty()) {
            cluster = request.getCluster();
        }
        logger.info("Starting batch query for cluster {} with {} metrics", cluster, request.getQueries().size());
        long startTime = System.currentTimeMillis();
        String finalCluster = cluster;
        return Flux.fromIterable(request.getQueries())
                .flatMap(metricQuery -> executeSingleQuery(finalCluster, metricQuery, request)
                        .map(result -> new PrometheusBatchQueryResponse.MetricResult(
                                metricQuery.getName(),
                                metricQuery.getDescription(),
                                metricQuery.getQuery(),
                                result,
                                System.currentTimeMillis() - startTime
                        )), config.getMaxConcurrency())
                .collectList()
                .flatMap(results -> {
                    List<PrometheusBatchQueryResponse.QueryError> errors = new ArrayList<>();
                    for (PrometheusQueryRequest.MetricQuery query : request.getQueries()) {
                        boolean found = results.stream()
                                .anyMatch(result -> result.getName().equals(query.getName()));
                        if (!found) {
                            errors.add(new PrometheusBatchQueryResponse.QueryError(
                                    query.getName(),
                                    query.getQuery(),
                                    "Query execution failed",
                                    "EXECUTION_ERROR"
                            ));
                        }
                    }
                    PrometheusBatchQueryResponse response = new PrometheusBatchQueryResponse();
                    response.setStatus(errors.isEmpty() ? "success" : (results.isEmpty() ? "error" : "partial_success"));
                    response.setData(results);
                    response.setErrors(errors);
                    response.setTotalQueries(request.getQueries().size());
                    response.setSuccessfulQueries(results.size());
                    response.setFailedQueries(errors.size());
                    return Mono.just(response);
                })
                .doOnSuccess(response -> {
                    logger.info("Batch query completed for cluster {}. Successful: {}, Failed: {}", 
                            finalCluster, response.getSuccessfulQueries(), response.getFailedQueries());
                })
                .doOnError(throwable -> {
                    logger.error("Batch query failed for cluster {}: {}", finalCluster, throwable.getMessage());
                });
    }
    
    @Override
    public Mono<PrometheusBatchQueryResponse> batchQueryRange(String cluster, PrometheusBatchRangeQueryRequest request) throws PrometheusException {
        if (cluster == null || cluster.isEmpty()) {
            cluster = request.getCluster();
        }
        logger.info("Starting batch range query for cluster {} with {} metrics", cluster, request.getQueries().size());
        long startTime = System.currentTimeMillis();
        String finalCluster = cluster;
        return Flux.fromIterable(request.getQueries())
                .flatMap(rangeMetricQuery -> executeSingleRangeQuery(finalCluster, rangeMetricQuery, request)
                        .map(result -> new PrometheusBatchQueryResponse.MetricResult(
                                rangeMetricQuery.getName(),
                                rangeMetricQuery.getDescription(),
                                rangeMetricQuery.getQuery(),
                                result,
                                System.currentTimeMillis() - startTime
                        )), config.getMaxConcurrency())
                .collectList()
                .flatMap(results -> {
                    List<PrometheusBatchQueryResponse.QueryError> errors = new ArrayList<>();
                    for (PrometheusBatchRangeQueryRequest.RangeMetricQuery query : request.getQueries()) {
                        boolean found = results.stream()
                                .anyMatch(result -> result.getName().equals(query.getName()));
                        if (!found) {
                            errors.add(new PrometheusBatchQueryResponse.QueryError(
                                    query.getName(),
                                    query.getQuery(),
                                    "Range query execution failed",
                                    "EXECUTION_ERROR"
                            ));
                        }
                    }
                    PrometheusBatchQueryResponse response = new PrometheusBatchQueryResponse();
                    response.setStatus(errors.isEmpty() ? "success" : (results.isEmpty() ? "error" : "partial_success"));
                    response.setData(results);
                    response.setErrors(errors);
                    response.setTotalQueries(request.getQueries().size());
                    response.setSuccessfulQueries(results.size());
                    response.setFailedQueries(errors.size());
                    return Mono.just(response);
                })
                .doOnSuccess(response -> {
                    logger.info("Batch range query completed for cluster {}. Successful: {}, Failed: {}", 
                            finalCluster, response.getSuccessfulQueries(), response.getFailedQueries());
                })
                .doOnError(throwable -> {
                    logger.error("Batch range query failed for cluster {}: {}", finalCluster, throwable.getMessage());
                });
    }
    
    @Override
    public Mono<PrometheusQueryResponse> queryRange(String cluster, String query, String startTime, String endTime, String step) throws PrometheusException {
        logger.debug("Executing range query for cluster {}: {} from {} to {} with step {}", cluster, query, startTime, endTime, step);
        return prometheusRepository.queryRange(cluster, query, startTime, endTime, step);
    }
    
    @Override
    public Mono<PrometheusQueryResponse> query(String cluster, String query, String time) throws PrometheusException {
        logger.debug("Executing instant query for cluster {}: {} at time {}", cluster, query, time);
        return prometheusRepository.query(cluster, query, time);
    }
    
    @Override
    public Mono<Boolean> checkConnection(String cluster) throws PrometheusException {
        logger.debug("Checking Prometheus connection for cluster {}", cluster);
        return prometheusRepository.checkConnection(cluster);
    }
    
    @Override
    public Mono<String> getVersion(String cluster) throws PrometheusException {
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
    private Mono<PrometheusQueryResponse> executeSingleQuery(
            String cluster,
            PrometheusQueryRequest.MetricQuery metricQuery, 
            PrometheusQueryRequest request) {
        long startTime = System.currentTimeMillis();
        if (request.getStartTime() != null && request.getEndTime() != null && request.getStep() != null) {
            return prometheusRepository.queryRange(
                    cluster,
                    metricQuery.getQuery(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getStep()
            ).doOnSuccess(result -> {
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("Range query completed for {} in {}ms", metricQuery.getName(), duration);
            });
        } else {
            return prometheusRepository.query(cluster, metricQuery.getQuery(), null)
                    .doOnSuccess(result -> {
                        long duration = System.currentTimeMillis() - startTime;
                        logger.debug("Instant query completed for {} in {}ms", metricQuery.getName(), duration);
                    });
        }
    }
    
    /**
     * 执行单个范围查询
     */
    private Mono<PrometheusQueryResponse> executeSingleRangeQuery(
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
            return Mono.error(new PrometheusException(
                    "Missing required time parameters for range query: " + rangeMetricQuery.getName(),
                    "PARAMETER_ERROR",
                    rangeMetricQuery.getQuery(),
                    null));
        }
        
        return prometheusRepository.queryRange(
                cluster,
                rangeMetricQuery.getQuery(),
                startTimeParam,
                endTimeParam,
                stepParam
        ).doOnSuccess(result -> {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Range query completed for {} in {}ms", rangeMetricQuery.getName(), duration);
        });
    }
} 