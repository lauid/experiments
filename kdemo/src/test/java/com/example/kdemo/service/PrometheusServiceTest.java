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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrometheusServiceTest {

    @Mock
    private PrometheusRepository prometheusRepository;

    @Mock
    private PrometheusConfig config;

    @InjectMocks
    private PrometheusServiceImpl prometheusService;

    private PrometheusQueryRequest.MetricQuery testQuery;
    private PrometheusQueryResponse testResponse;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testQuery = new PrometheusQueryRequest.MetricQuery(
                "cpu_usage",
                "rate(container_cpu_usage_seconds_total[5m]) * 100",
                "Container CPU usage percentage",
                new HashMap<>()
        );

        testResponse = new PrometheusQueryResponse();
        testResponse.setStatus("success");
        
        PrometheusQueryResponse.QueryData queryData = new PrometheusQueryResponse.QueryData();
        queryData.setResultType("matrix");
        queryData.setResult(new ArrayList<>());
        testResponse.setData(queryData);
    }

    @Test
    void testBatchQuery_Success() {
        // 设置配置
        when(config.getMaxConcurrency()).thenReturn(5);
        
        // 准备测试数据
        PrometheusQueryRequest request = new PrometheusQueryRequest();
        request.setCluster("test-cluster");
        
        List<PrometheusQueryRequest.MetricQuery> queries = new ArrayList<>();
        queries.add(testQuery);
        request.setQueries(queries);
        
        request.setStartTime("2024-01-01T00:00:00Z");
        request.setEndTime("2024-01-01T01:00:00Z");
        request.setStep("1m");

        // Mock repository response
        when(prometheusRepository.queryRange(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(testResponse));

        // 执行测试
        Mono<PrometheusBatchQueryResponse> result = prometheusService.batchQuery("test-cluster", request);

        // 验证结果
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    return "success".equals(response.getStatus()) &&
                           response.getSuccessfulQueries() == 1 &&
                           response.getFailedQueries() == 0 &&
                           response.getData().size() == 1 &&
                           "cpu_usage".equals(response.getData().get(0).getName());
                })
                .verifyComplete();
    }

    @Test
    void testBatchQuery_PartialFailure() {
        // 设置配置
        when(config.getMaxConcurrency()).thenReturn(5);
        
        // 准备测试数据
        PrometheusQueryRequest request = new PrometheusQueryRequest();
        request.setCluster("test-cluster");
        
        List<PrometheusQueryRequest.MetricQuery> queries = new ArrayList<>();
        queries.add(testQuery);
        
        PrometheusQueryRequest.MetricQuery failingQuery = new PrometheusQueryRequest.MetricQuery(
                "memory_usage",
                "invalid_query",
                "Memory usage",
                new HashMap<>()
        );
        queries.add(failingQuery);
        request.setQueries(queries);

        // Mock repository responses
        when(prometheusRepository.query(eq("test-cluster"), eq(testQuery.getQuery()), isNull()))
                .thenReturn(Mono.just(testResponse));
        when(prometheusRepository.query(eq("test-cluster"), eq(failingQuery.getQuery()), isNull()))
                .thenReturn(Mono.error(new PrometheusException("Invalid query")));

        // 执行测试
        Mono<PrometheusBatchQueryResponse> result = prometheusService.batchQuery("test-cluster", request);

        // 验证结果
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    return "partial_success".equals(response.getStatus()) &&
                           response.getSuccessfulQueries() == 1 &&
                           response.getFailedQueries() == 1 &&
                           response.getData().size() == 1 &&
                           response.getErrors().size() == 1;
                })
                .verifyComplete();
    }

    @Test
    void testQueryRange_Success() {
        // Mock repository response
        when(prometheusRepository.queryRange(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(testResponse));

        // 执行测试
        Mono<PrometheusQueryResponse> result = prometheusService.queryRange(
                "test-cluster",
                "rate(container_cpu_usage_seconds_total[5m])",
                "1640995200",
                "1640998800",
                "1m"
        );

        // 验证结果
        StepVerifier.create(result)
                .expectNext(testResponse)
                .verifyComplete();
    }

    @Test
    void testQuery_Success() {
        // Mock repository response
        when(prometheusRepository.query(anyString(), anyString(), any()))
                .thenReturn(Mono.just(testResponse));

        // 执行测试
        Mono<PrometheusQueryResponse> result = prometheusService.query(
                "test-cluster",
                "container_cpu_usage_seconds_total",
                null
        );

        // 验证结果
        StepVerifier.create(result)
                .expectNext(testResponse)
                .verifyComplete();
    }

    @Test
    void testCheckConnection_Success() {
        // Mock repository response
        when(prometheusRepository.checkConnection(anyString()))
                .thenReturn(Mono.just(true));

        // 执行测试
        Mono<Boolean> result = prometheusService.checkConnection("test-cluster");

        // 验证结果
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testGetVersion_Success() {
        // Mock repository response
        when(prometheusRepository.getVersion(anyString()))
                .thenReturn(Mono.just("2.45.0"));

        // 执行测试
        Mono<String> result = prometheusService.getVersion("test-cluster");

        // 验证结果
        StepVerifier.create(result)
                .expectNext("2.45.0")
                .verifyComplete();
    }

    @Test
    void testGetMetricTemplates() {
        // 执行测试
        List<PrometheusQueryRequest.MetricQuery> templates = prometheusService.getMetricTemplates();

        // 验证结果
        assert templates.size() == 6;
        assert templates.stream().anyMatch(t -> "cpu_usage".equals(t.getName()));
        assert templates.stream().anyMatch(t -> "memory_usage".equals(t.getName()));
        assert templates.stream().anyMatch(t -> "network_traffic".equals(t.getName()));
    }
} 