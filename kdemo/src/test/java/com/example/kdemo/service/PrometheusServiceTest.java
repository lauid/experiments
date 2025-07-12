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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        when(config.getMaxConcurrency()).thenReturn(5);
        PrometheusQueryRequest request = new PrometheusQueryRequest();
        request.setCluster("test-cluster");
        List<PrometheusQueryRequest.MetricQuery> queries = new ArrayList<>();
        queries.add(testQuery);
        request.setQueries(queries);
        request.setStartTime(1640995200L); // 2024-01-01T00:00:00Z (秒级时间戳)
        request.setEndTime(1640998800L);   // 2024-01-01T01:00:00Z (秒级时间戳)
        request.setStep("1m");
        when(prometheusRepository.query(eq("test-cluster"), eq(testQuery.getQuery()), isNull()))
                .thenReturn(testResponse);
        PrometheusBatchQueryResponse response = prometheusService.batchQuery("test-cluster", request);
        assert "success".equals(response.getStatus());
        assert response.getSuccessfulQueries() == 1;
        assert response.getFailedQueries() == 0;
        assert response.getData().size() == 1;
        assert "cpu_usage".equals(response.getData().get(0).getName());
    }

    @Test
    void testBatchQuery_PartialFailure() {
        when(config.getMaxConcurrency()).thenReturn(5);
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
        when(prometheusRepository.query(eq("test-cluster"), eq(testQuery.getQuery()), isNull()))
                .thenReturn(testResponse);
        when(prometheusRepository.query(eq("test-cluster"), eq(failingQuery.getQuery()), isNull()))
                .thenThrow(new PrometheusException("Invalid query"));
        PrometheusBatchQueryResponse response = prometheusService.batchQuery("test-cluster", request);
        assert "partial_success".equals(response.getStatus());
        assert response.getSuccessfulQueries() == 1;
        assert response.getFailedQueries() == 1;
        assert response.getData().size() == 1;
        assert response.getErrors().size() == 1;
    }

    @Test
    void testQueryRange_Success() {
        when(prometheusRepository.queryRange(anyString(), anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(testResponse);
        PrometheusQueryResponse response = prometheusService.queryRange(
                "test-cluster",
                "rate(container_cpu_usage_seconds_total[5m])",
                1640995200L, // 2024-01-01T00:00:00Z (秒级时间戳)
                1640998800L, // 2024-01-01T01:00:00Z (秒级时间戳)
                "1m"
        );
        assert response == testResponse;
    }

    @Test
    void testQuery_Success() {
        when(prometheusRepository.query(anyString(), anyString(), any()))
                .thenReturn(testResponse);
        PrometheusQueryResponse response = prometheusService.query(
                "test-cluster",
                "container_cpu_usage_seconds_total",
                null
        );
        assert response == testResponse;
    }

    @Test
    void testCheckConnection_Success() {
        when(prometheusRepository.checkConnection(anyString()))
                .thenReturn(true);
        Boolean result = prometheusService.checkConnection("test-cluster");
        assert result;
    }

    @Test
    void testGetVersion_Success() {
        when(prometheusRepository.getVersion(anyString()))
                .thenReturn("2.45.0");
        String version = prometheusService.getVersion("test-cluster");
        assert "2.45.0".equals(version);
    }

    @Test
    void testGetMetricTemplates() {
        List<PrometheusQueryRequest.MetricQuery> templates = prometheusService.getMetricTemplates();
        assert templates.size() == 6;
        assert templates.stream().anyMatch(t -> "cpu_usage".equals(t.getName()));
        assert templates.stream().anyMatch(t -> "memory_usage".equals(t.getName()));
        assert templates.stream().anyMatch(t -> "network_traffic".equals(t.getName()));
    }
} 