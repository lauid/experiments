package com.example.kdemo.controller;

import com.example.kdemo.dto.PrometheusBatchQueryResponse;
import com.example.kdemo.dto.PrometheusBatchRangeQueryRequest;
import com.example.kdemo.dto.PrometheusQueryRequest;
import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import com.example.kdemo.service.PrometheusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Prometheus查询控制器
 */
@RestController
@RequestMapping("/api")
public class PrometheusController {
    
    private static final Logger log = LoggerFactory.getLogger(PrometheusController.class);
    
    private final PrometheusService prometheusService;
    
    @Autowired
    public PrometheusController(PrometheusService prometheusService) {
        this.prometheusService = prometheusService;
    }
    
    /**
     * 批量查询多个指标
     */
    @PostMapping("/prometheus/batch-query")
    public Mono<ResponseEntity<PrometheusBatchQueryResponse>> batchQuery(
            @RequestParam(value = "cluster", required = false) String cluster,
            @RequestBody PrometheusQueryRequest request) {
        log.info("Received batch query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        return prometheusService.batchQuery(cluster, request)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Batch query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new PrometheusBatchQueryResponse("error", null, null)));
                });
    }
    
    /**
     * 批量查询多个指标（指定集群）
     */
    @PostMapping("/clusters/{cluster}/prometheus/batch-query")
    public Mono<ResponseEntity<PrometheusBatchQueryResponse>> batchQueryWithCluster(
            @PathVariable String cluster,
            @RequestBody PrometheusQueryRequest request) {
        log.info("Received batch query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        return prometheusService.batchQuery(cluster, request)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Batch query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new PrometheusBatchQueryResponse("error", null, null)));
                });
    }
    
    /**
     * 批量范围查询多个指标
     */
    @PostMapping("/prometheus/batch-query-range")
    public Mono<ResponseEntity<PrometheusBatchQueryResponse>> batchQueryRange(
            @RequestParam(value = "cluster", required = false) String cluster,
            @RequestBody PrometheusBatchRangeQueryRequest request) {
        log.info("Received batch range query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        return prometheusService.batchQueryRange(cluster, request)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Batch range query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new PrometheusBatchQueryResponse("error", null, null)));
                });
    }
    
    /**
     * 批量范围查询多个指标（指定集群）
     */
    @PostMapping("/clusters/{cluster}/prometheus/batch-query-range")
    public Mono<ResponseEntity<PrometheusBatchQueryResponse>> batchQueryRangeWithCluster(
            @PathVariable String cluster,
            @RequestBody PrometheusBatchRangeQueryRequest request) {
        log.info("Received batch range query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        return prometheusService.batchQueryRange(cluster, request)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Batch range query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new PrometheusBatchQueryResponse("error", null, null)));
                });
    }
    
    /**
     * 执行范围查询
     */
    @GetMapping("/prometheus/query-range")
    public Mono<ResponseEntity<PrometheusQueryResponse>> queryRange(
            @RequestParam String query,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String step,
            @RequestParam(value = "cluster", required = false) String cluster) {
        log.debug("Received range query request for cluster {}: {} from {} to {} with step {}", cluster, query, start, end, step);
        return prometheusService.queryRange(cluster, query, start, end, step)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Range query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse(e)));
                });
    }
    
    /**
     * 执行范围查询（指定集群）
     */
    @GetMapping("/clusters/{cluster}/prometheus/query-range")
    public Mono<ResponseEntity<PrometheusQueryResponse>> queryRangeWithCluster(
            @PathVariable String cluster,
            @RequestParam String query,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String step) {
        log.debug("Received range query request for cluster {}: {} from {} to {} with step {}", cluster, query, start, end, step);
        return prometheusService.queryRange(cluster, query, start, end, step)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Range query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse(e)));
                });
    }
    
    /**
     * 执行即时查询
     */
    @GetMapping("/prometheus/query")
    public Mono<ResponseEntity<PrometheusQueryResponse>> query(
            @RequestParam String query,
            @RequestParam(required = false) String time,
            @RequestParam(value = "cluster", required = false) String cluster) {
        log.debug("Received instant query request for cluster {}: {} at time {}", cluster, query, time);
        return prometheusService.query(cluster, query, time)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Instant query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse(e)));
                });
    }
    
    /**
     * 执行即时查询（指定集群）
     */
    @GetMapping("/clusters/{cluster}/prometheus/query")
    public Mono<ResponseEntity<PrometheusQueryResponse>> queryWithCluster(
            @PathVariable String cluster,
            @RequestParam String query,
            @RequestParam(required = false) String time) {
        log.debug("Received instant query request for cluster {}: {} at time {}", cluster, query, time);
        return prometheusService.query(cluster, query, time)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Instant query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse(e)));
                });
    }
    
    /**
     * 检查Prometheus连接状态
     */
    @GetMapping("/prometheus/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkHealth(@RequestParam(value = "cluster", required = false) String cluster) {
        log.debug("Received health check request for cluster {}", cluster);
        return prometheusService.checkConnection(cluster)
                .flatMap(connected -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", connected ? "healthy" : "unhealthy");
                    response.put("connected", connected);
                    if (connected) {
                        return prometheusService.getVersion(cluster)
                                .map(version -> {
                                    response.put("version", version);
                                    return ResponseEntity.ok(response);
                                })
                                .onErrorResume(e -> {
                                    response.put("version", "unknown");
                                    return Mono.just(ResponseEntity.ok(response));
                                });
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Health check failed: {}", e.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("connected", false);
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }
    
    /**
     * 检查Prometheus连接状态（指定集群）
     */
    @GetMapping("/clusters/{cluster}/prometheus/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkHealthWithCluster(@PathVariable String cluster) {
        log.debug("Received health check request for cluster {}", cluster);
        return prometheusService.checkConnection(cluster)
                .flatMap(connected -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", connected ? "healthy" : "unhealthy");
                    response.put("connected", connected);
                    response.put("cluster", cluster);
                    if (connected) {
                        return prometheusService.getVersion(cluster)
                                .map(version -> {
                                    response.put("version", version);
                                    return ResponseEntity.ok(response);
                                })
                                .onErrorResume(e -> {
                                    response.put("version", "unknown");
                                    return Mono.just(ResponseEntity.ok(response));
                                });
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Health check failed: {}", e.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("connected", false);
                    errorResponse.put("cluster", cluster);
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }
    
    /**
     * 获取指标查询模板
     */
    @GetMapping("/prometheus/templates")
    public ResponseEntity<Map<String, Object>> getTemplates() {
        log.debug("Received templates request");
        Map<String, Object> response = new HashMap<>();
        response.put("templates", prometheusService.getMetricTemplates());
        response.put("count", prometheusService.getMetricTemplates().size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取Prometheus版本信息
     */
    @GetMapping("/prometheus/version")
    public Mono<ResponseEntity<Map<String, String>>> getVersion(@RequestParam(value = "cluster", required = false) String cluster) {
        log.debug("Received version request for cluster {}", cluster);
        return prometheusService.getVersion(cluster)
                .map(version -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("version", version);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Version request failed: {}", e.getMessage());
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }
    
    /**
     * 获取Prometheus版本信息（指定集群）
     */
    @GetMapping("/clusters/{cluster}/prometheus/version")
    public Mono<ResponseEntity<Map<String, String>>> getVersionWithCluster(@PathVariable String cluster) {
        log.debug("Received version request for cluster {}", cluster);
        return prometheusService.getVersion(cluster)
                .map(version -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("version", version);
                    response.put("cluster", cluster);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(PrometheusException.class, e -> {
                    log.error("Version request failed: {}", e.getMessage());
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", e.getMessage());
                    errorResponse.put("cluster", cluster);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }
    
    /**
     * 异常处理器
     */
    @ExceptionHandler(PrometheusException.class)
    public ResponseEntity<Map<String, String>> handlePrometheusException(PrometheusException e) {
        log.error("Prometheus exception: {}", e.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("error_type", e.getErrorType());
        if (e.getQuery() != null) {
            response.put("query", e.getQuery());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 错误响应方法
    private PrometheusQueryResponse createErrorResponse(PrometheusException e) {
        PrometheusQueryResponse errorResponse = new PrometheusQueryResponse();
        errorResponse.setStatus("error");
        errorResponse.setError(e.getMessage());
        errorResponse.setErrorType(e.getErrorType());
        return errorResponse;
    }
} 