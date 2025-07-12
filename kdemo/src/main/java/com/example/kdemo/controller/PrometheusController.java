package com.example.kdemo.controller;

import com.example.kdemo.dto.PrometheusBatchQueryResponse;
import com.example.kdemo.dto.PrometheusBatchRangeQueryRequest;
import com.example.kdemo.dto.PrometheusQueryRequest;
import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import com.example.kdemo.service.PrometheusService;
import com.example.kdemo.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Prometheus查询控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class PrometheusController {
    
    private final PrometheusService prometheusService;
    private final RestTemplate restTemplate;
    
    @Autowired
    public PrometheusController(PrometheusService prometheusService, RestTemplate restTemplate) {
        this.prometheusService = prometheusService;
        this.restTemplate = restTemplate;
    }
    
    /**
     * 批量查询多个指标
     */
    @PostMapping("/prometheus/batch-query")
    public ResponseEntity<PrometheusBatchQueryResponse> batchQuery(
            @RequestParam(value = "cluster", required = false) String cluster,
            @RequestBody PrometheusQueryRequest request) {
        log.info("Received batch query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        try {
            PrometheusBatchQueryResponse response = prometheusService.batchQuery(cluster, request);
            log.info("Batch query completed successfully");
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Batch query failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PrometheusBatchQueryResponse("error", null, null));
        }
    }
    
    /**
     * 批量查询多个指标（指定集群）
     */
    @PostMapping("/clusters/{cluster}/prometheus/batch-query")
    public ResponseEntity<PrometheusBatchQueryResponse> batchQueryWithCluster(
            @PathVariable String cluster,
            @RequestBody PrometheusQueryRequest request) {
        log.info("Received batch query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        try {
            PrometheusBatchQueryResponse response = prometheusService.batchQuery(cluster, request);
            log.info("Batch query completed successfully");
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Batch query failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PrometheusBatchQueryResponse("error", null, null));
        }
    }
    
    /**
     * 批量范围查询多个指标
     */
    @PostMapping("/prometheus/batch-query-range")
    public ResponseEntity<PrometheusBatchQueryResponse> batchQueryRange(
            @RequestParam(value = "cluster", required = false) String cluster,
            @RequestBody PrometheusBatchRangeQueryRequest request) {
        log.info("Received batch range query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        try {
            PrometheusBatchQueryResponse response = prometheusService.batchQueryRange(cluster, request);
            log.info("Batch range query completed successfully");
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Batch range query failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PrometheusBatchQueryResponse("error", null, null));
        }
    }
    
    /**
     * 批量范围查询多个指标（指定集群）
     */
    @PostMapping("/clusters/{cluster}/prometheus/batch-query-range")
    public ResponseEntity<PrometheusBatchQueryResponse> batchQueryRangeWithCluster(
            @PathVariable String cluster,
            @RequestBody PrometheusBatchRangeQueryRequest request) {
        log.info("Received batch range query request for cluster {} with {} metrics", cluster, request.getQueries().size());
        try {
            PrometheusBatchQueryResponse response = prometheusService.batchQueryRange(cluster, request);
            log.info("Batch range query completed successfully");
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Batch range query failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PrometheusBatchQueryResponse("error", null, null));
        }
    }
    
    /**
     * 执行范围查询
     */
    @GetMapping("/prometheus/query-range")
    public ResponseEntity<PrometheusQueryResponse> queryRange(
            @RequestParam String query,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String step,
            @RequestParam(value = "cluster", required = false) String cluster) {
        log.debug("Received range query request for cluster {}: {} from {} to {} with step {}", cluster, query, start, end, step);
        try {
            // 将字符串时间转换为时间戳（毫秒）
            Long startTime = TimeUtils.parseTimeParameter(start);
            Long endTime = TimeUtils.parseTimeParameter(end);
            
            PrometheusQueryResponse response = prometheusService.queryRange(cluster, query, startTime, endTime, step);
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Range query failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e));
        }
    }
    
    /**
     * 执行范围查询（指定集群）
     */
    @GetMapping("/clusters/{cluster}/prometheus/query-range")
    public ResponseEntity<PrometheusQueryResponse> queryRangeWithCluster(
            @PathVariable String cluster,
            @RequestParam String query,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String step) {
        log.debug("Received range query request for cluster {}: {} from {} to {} with step {}", cluster, query, start, end, step);
        try {
            // 将字符串时间转换为时间戳（毫秒）
            Long startTime = TimeUtils.parseTimeParameter(start);
            Long endTime = TimeUtils.parseTimeParameter(end);
            
            PrometheusQueryResponse response = prometheusService.queryRange(cluster, query, startTime, endTime, step);
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Range query failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e));
        }
    }
    
    /**
     * 执行即时查询
     */
    @GetMapping("/prometheus/query")
    public ResponseEntity<PrometheusQueryResponse> query(
            @RequestParam String query,
            @RequestParam(required = false) String time,
            @RequestParam(value = "cluster", required = false) String cluster) {
        log.debug("Received instant query request for cluster {}: {} at time {}", cluster, query, time);
        try {
            // 将字符串时间转换为时间戳（毫秒）
            Long timeParam = time != null ? TimeUtils.parseTimeParameter(time) : null;
            
            PrometheusQueryResponse response = prometheusService.query(cluster, query, timeParam);
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Instant query failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e));
        }
    }
    
    /**
     * 执行即时查询（指定集群）
     */
    @GetMapping("/clusters/{cluster}/prometheus/query")
    public ResponseEntity<PrometheusQueryResponse> queryWithCluster(
            @PathVariable String cluster,
            @RequestParam String query,
            @RequestParam(required = false) String time) {
        log.debug("Received instant query request for cluster {}: {} at time {}", cluster, query, time);
        try {
            // 将字符串时间转换为时间戳（毫秒）
            Long timeParam = time != null ? TimeUtils.parseTimeParameter(time) : null;
            
            PrometheusQueryResponse response = prometheusService.query(cluster, query, timeParam);
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Instant query failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e));
        }
    }
    
    /**
     * 检查Prometheus连接状态
     */
    @GetMapping("/prometheus/health")
    public ResponseEntity<Map<String, Object>> checkHealth(@RequestParam(value = "cluster", required = false) String cluster) {
        log.debug("Received health check request for cluster {}", cluster);
        Map<String, Object> response = new HashMap<>();
        try {
            Boolean connected = prometheusService.checkConnection(cluster);
            response.put("status", connected ? "healthy" : "unhealthy");
            response.put("connected", connected);
            if (connected) {
                try {
                    String version = prometheusService.getVersion(cluster);
                    response.put("version", version);
                } catch (Exception e) {
                    response.put("version", "unknown");
                }
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            response.put("status", "error");
            response.put("connected", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 检查Prometheus连接状态（指定集群）
     */
    @GetMapping("/clusters/{cluster}/prometheus/health")
    public ResponseEntity<Map<String, Object>> checkHealthWithCluster(@PathVariable String cluster) {
        log.debug("Received health check request for cluster {}", cluster);
        Map<String, Object> response = new HashMap<>();
        try {
            Boolean connected = prometheusService.checkConnection(cluster);
            response.put("status", connected ? "healthy" : "unhealthy");
            response.put("connected", connected);
            response.put("cluster", cluster);
            if (connected) {
                try {
                    String version = prometheusService.getVersion(cluster);
                    response.put("version", version);
                } catch (Exception e) {
                    response.put("version", "unknown");
                }
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            response.put("status", "error");
            response.put("connected", false);
            response.put("cluster", cluster);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    public ResponseEntity<Map<String, String>> getVersion(@RequestParam(value = "cluster", required = false) String cluster) {
        log.debug("Received version request for cluster {}", cluster);
        Map<String, String> response = new HashMap<>();
        try {
            String version = prometheusService.getVersion(cluster);
            response.put("version", version);
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Version request failed: {}", e.getMessage());
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取Prometheus版本信息（指定集群）
     */
    @GetMapping("/clusters/{cluster}/prometheus/version")
    public ResponseEntity<Map<String, String>> getVersionWithCluster(@PathVariable String cluster) {
        log.debug("Received version request for cluster {}", cluster);
        Map<String, String> response = new HashMap<>();
        try {
            String version = prometheusService.getVersion(cluster);
            response.put("version", version);
            response.put("cluster", cluster);
            return ResponseEntity.ok(response);
        } catch (PrometheusException e) {
            log.error("Version request failed: {}", e.getMessage());
            response.put("error", e.getMessage());
            response.put("cluster", cluster);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 测试Prometheus连接
     */
    @GetMapping("/prometheus/test-connection")
    public ResponseEntity<String> testConnection(@RequestParam(defaultValue = "cluster-local") String cluster) {
        try {
            String url = String.format("http://localhost:9090/api/v1/query?query=up");
            String result = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok("Connection successful: " + result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Connection failed: " + e.getMessage());
        }
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