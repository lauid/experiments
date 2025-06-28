package com.example.kdemo.service;

import com.example.kdemo.config.PrometheusConfig;
import com.example.kdemo.dto.PrometheusBatchQueryResponse;
import com.example.kdemo.dto.PrometheusQueryRequest;
import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import com.example.kdemo.repository.PrometheusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Prometheus服务集成测试
 */
@ExtendWith(MockitoExtension.class)
public class PrometheusServiceIntegrationTest {

    @Mock
    private PrometheusRepository prometheusRepository;

    @Mock
    private PrometheusConfig config;

    @InjectMocks
    private PrometheusServiceImpl prometheusService;

    @BeforeEach
    void setUp() {
        // 设置配置
        when(config.getMaxConcurrency()).thenReturn(5);
        when(config.getBatchSize()).thenReturn(10);
        when(config.getIndividualQueryTimeout()).thenReturn(5000);
        when(config.getCorePoolSize()).thenReturn(2);
        when(config.getMaximumPoolSize()).thenReturn(10);
        when(config.getQueueCapacity()).thenReturn(100);
        when(config.getKeepAliveTime()).thenReturn(60);
        
        // 手动创建线程池并注入
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(5);
        ReflectionTestUtils.setField(prometheusService, "prometheusQueryExecutor", executor);
    }

    @Test
    void testBatchQueryWithMultipleMetrics() throws PrometheusException {
        // 准备测试数据
        PrometheusQueryRequest request = new PrometheusQueryRequest();
        request.setCluster("test-cluster");
        
        List<PrometheusQueryRequest.MetricQuery> queries = new ArrayList<>();
        
        // 添加多个指标查询
        queries.add(new PrometheusQueryRequest.MetricQuery("up", "up", "Target up status", new HashMap<>()));
        queries.add(new PrometheusQueryRequest.MetricQuery("cpu_usage", "rate(process_cpu_seconds_total[5m])", "CPU usage", new HashMap<>()));
        queries.add(new PrometheusQueryRequest.MetricQuery("memory_usage", "process_resident_memory_bytes", "Memory usage", new HashMap<>()));
        queries.add(new PrometheusQueryRequest.MetricQuery("go_goroutines", "go_goroutines", "Goroutines count", new HashMap<>()));
        
        request.setQueries(queries);

        // Mock Prometheus响应
        PrometheusQueryResponse mockResponse = createMockPrometheusResponse();
        when(prometheusRepository.query(anyString(), anyString(), any())).thenReturn(mockResponse);

        // 执行批量查询
        PrometheusBatchQueryResponse response = prometheusService.batchQuery("test-cluster", request);

        // 验证结果
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(4, response.getData().size());
        assertEquals(0, response.getErrors().size());
        assertEquals(4, response.getSuccessfulQueries());
        assertEquals(0, response.getFailedQueries());

        // 验证每个查询的结果
        for (PrometheusBatchQueryResponse.MetricResult result : response.getData()) {
            assertNotNull(result.getName());
            assertNotNull(result.getQuery());
            assertNotNull(result.getResult());
            assertTrue(result.getExecutionTimeMs() >= 0);
        }
    }

    @Test
    void testBatchQueryWithPartialFailures() throws PrometheusException {
        // 准备测试数据
        PrometheusQueryRequest request = new PrometheusQueryRequest();
        request.setCluster("test-cluster");
        
        List<PrometheusQueryRequest.MetricQuery> queries = new ArrayList<>();
        queries.add(new PrometheusQueryRequest.MetricQuery("up", "up", "Target up status", new HashMap<>()));
        queries.add(new PrometheusQueryRequest.MetricQuery("invalid_query", "invalid_promql", "Invalid query", new HashMap<>()));
        
        request.setQueries(queries);

        // Mock部分成功，部分失败
        PrometheusQueryResponse mockResponse = createMockPrometheusResponse();
        when(prometheusRepository.query(eq("test-cluster"), eq("up"), any())).thenReturn(mockResponse);
        when(prometheusRepository.query(eq("test-cluster"), eq("invalid_promql"), any()))
                .thenThrow(new RuntimeException("Invalid PromQL"));

        // 执行批量查询
        PrometheusBatchQueryResponse response = prometheusService.batchQuery("test-cluster", request);

        // 验证结果
        assertNotNull(response);
        assertEquals("partial_success", response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals(1, response.getErrors().size());
        assertEquals(1, response.getSuccessfulQueries());
        assertEquals(1, response.getFailedQueries());
    }

    @Test
    void testBatchQueryWithLargeNumberOfQueries() throws PrometheusException {
        // 准备大量查询（测试批量处理）
        PrometheusQueryRequest request = new PrometheusQueryRequest();
        request.setCluster("test-cluster");
        
        List<PrometheusQueryRequest.MetricQuery> queries = new ArrayList<>();
        
        // 创建25个查询（超过默认batch size 10）
        for (int i = 0; i < 25; i++) {
            queries.add(new PrometheusQueryRequest.MetricQuery(
                "metric_" + i, 
                "up{job=\"test" + i + "\"}", 
                "Test metric " + i, 
                new HashMap<>()
            ));
        }
        
        request.setQueries(queries);

        // Mock所有查询都成功
        PrometheusQueryResponse mockResponse = createMockPrometheusResponse();
        when(prometheusRepository.query(anyString(), anyString(), any())).thenReturn(mockResponse);

        // 执行批量查询
        PrometheusBatchQueryResponse response = prometheusService.batchQuery("test-cluster", request);

        // 验证结果
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals(25, response.getData().size());
        assertEquals(0, response.getErrors().size());
        assertEquals(25, response.getSuccessfulQueries());
        assertEquals(0, response.getFailedQueries());
    }

    @Test
    void testBatchQueryTimeout() throws PrometheusException {
        // 准备测试数据
        PrometheusQueryRequest request = new PrometheusQueryRequest();
        request.setCluster("test-cluster");
        
        List<PrometheusQueryRequest.MetricQuery> queries = new ArrayList<>();
        queries.add(new PrometheusQueryRequest.MetricQuery("slow_query", "up", "Slow query", new HashMap<>()));
        
        request.setQueries(queries);

        // Mock超时响应
        when(prometheusRepository.query(anyString(), anyString(), any()))
                .thenAnswer(invocation -> {
                    Thread.sleep(6000); // 超过5秒超时
                    return createMockPrometheusResponse();
                });

        // 执行批量查询
        PrometheusBatchQueryResponse response = prometheusService.batchQuery("test-cluster", request);

        // 验证结果
        assertNotNull(response);
        assertEquals("failed", response.getStatus());
        assertEquals(0, response.getData().size());
        assertEquals(1, response.getErrors().size());
        assertEquals(0, response.getSuccessfulQueries());
        assertEquals(1, response.getFailedQueries());
        
        // 验证错误类型
        PrometheusBatchQueryResponse.QueryError error = response.getErrors().get(0);
        assertEquals("TIMEOUT", error.getErrorType());
        assertTrue(error.getError().contains("timeout"));
    }

    /**
     * 创建模拟的Prometheus响应
     */
    private PrometheusQueryResponse createMockPrometheusResponse() {
        PrometheusQueryResponse response = new PrometheusQueryResponse();
        response.setStatus("success");
        
        PrometheusQueryResponse.QueryData data = new PrometheusQueryResponse.QueryData();
        data.setResultType("vector");
        
        List<PrometheusQueryResponse.QueryResult> results = new ArrayList<>();
        PrometheusQueryResponse.QueryResult result = new PrometheusQueryResponse.QueryResult();
        result.setMetric(new HashMap<>());
        result.setValue(List.of("1640995200", "1"));
        results.add(result);
        
        data.setResult(results);
        response.setData(data);
        
        return response;
    }
} 