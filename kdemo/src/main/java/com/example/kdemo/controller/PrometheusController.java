package com.example.kdemo.controller;

import com.example.kdemo.dto.PrometheusBatchQueryResponse;
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
@RequestMapping("/api/prometheus")
public class PrometheusController {
    
    private static final Logger logger = LoggerFactory.getLogger(PrometheusController.class);
    
    private final PrometheusService prometheusService;
    
    @Autowired
    public PrometheusController(PrometheusService prometheusService) {
        this.prometheusService = prometheusService;
    }
    
    /**
     * 批量查询多个指标
     */
    @PostMapping("/batch-query")
    public Mono<ResponseEntity<PrometheusBatchQueryResponse>> batchQuery(
            @RequestParam(value = "cluster", required = false) String cluster,
            @RequestBody PrometheusQueryRequest request) {
        logger.info("Received batch query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        return prometheusService.batchQuery(cluster, request)
                .map(response -> {
                    logger.info("Batch query completed successfully");
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(PrometheusException.class, e -> {
                    logger.error("Batch query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new PrometheusBatchQueryResponse("error", null, null)));
                });
    }
    
    /**
     * 执行范围查询
     */
    @GetMapping("/query-range")
    public Mono<ResponseEntity<PrometheusQueryResponse>> queryRange(
            @RequestParam String query,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String step,
            @RequestParam(value = "cluster", required = false) String cluster) {
        logger.debug("Received range query request for cluster {}: {} from {} to {} with step {}", cluster, query, start, end, step);
        return prometheusService.queryRange(cluster, query, start, end, step)
                .map(ResponseEntity::ok)
                .onErrorResume(PrometheusException.class, e -> {
                    logger.error("Range query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse(e)));
                });
    }
    
    /**
     * 执行即时查询
     */
    @GetMapping("/query")
    public Mono<ResponseEntity<PrometheusQueryResponse>> query(
            @RequestParam String query,
            @RequestParam(required = false) String time,
            @RequestParam(value = "cluster", required = false) String cluster) {
        logger.debug("Received instant query request for cluster {}: {} at time {}", cluster, query, time);
        return prometheusService.query(cluster, query, time)
                .map(ResponseEntity::ok)
                .onErrorResume(PrometheusException.class, e -> {
                    logger.error("Instant query failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createErrorResponse(e)));
                });
    }
    
    /**
     * 检查Prometheus连接状态
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkHealth(@RequestParam(value = "cluster", required = false) String cluster) {
        logger.debug("Received health check request for cluster {}", cluster);
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
                    logger.error("Health check failed: {}", e.getMessage());
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("connected", false);
                    response.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }
    
    /**
     * 获取指标查询模板
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTemplates() {
        logger.debug("Received templates request");
        
        Map<String, Object> response = new HashMap<>();
        response.put("templates", prometheusService.getMetricTemplates());
        response.put("count", prometheusService.getMetricTemplates().size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取Prometheus版本信息
     */
    @GetMapping("/version")
    public Mono<ResponseEntity<Map<String, String>>> getVersion(@RequestParam(value = "cluster", required = false) String cluster) {
        logger.debug("Received version request for cluster {}", cluster);
        
        return prometheusService.getVersion(cluster)
                .map(version -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("version", version);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(PrometheusException.class, e -> {
                    logger.error("Version request failed: {}", e.getMessage());
                    Map<String, String> response = new HashMap<>();
                    response.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }
    
    /**
     * 异常处理器
     */
    @ExceptionHandler(PrometheusException.class)
    public ResponseEntity<Map<String, String>> handlePrometheusException(PrometheusException e) {
        logger.error("Prometheus exception: {}", e.getMessage());
        
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