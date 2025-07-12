package com.example.kdemo.repository;

import com.example.kdemo.config.PrometheusConfig;
import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import com.example.kdemo.util.KubernetesSecretUtils;
import com.example.kdemo.util.TlsUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Prometheus数据访问实现类
 */
@Repository
public class PrometheusRepositoryImpl implements PrometheusRepository {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusRepositoryImpl.class);
    private final PrometheusConfig config;
    private final ObjectMapper objectMapper;
    private final KubernetesSecretUtils kubernetesSecretUtils;
    private final TlsUtils tlsUtils;
    private final RestTemplate restTemplate;
    private final ExecutorService executor;

    @Autowired
    public PrometheusRepositoryImpl(PrometheusConfig config, ObjectMapper objectMapper,
                                   KubernetesSecretUtils kubernetesSecretUtils, TlsUtils tlsUtils, RestTemplate restTemplate) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.kubernetesSecretUtils = kubernetesSecretUtils;
        this.tlsUtils = tlsUtils;
        this.restTemplate = restTemplate;
        this.executor = new ThreadPoolExecutor(
                8, // corePoolSize
                16, // maxPoolSize
                60L, TimeUnit.SECONDS, // keepAliveTime
                new LinkedBlockingQueue<>(50), // queueCapacity
                new ThreadPoolExecutor.AbortPolicy() // 拒绝策略，默认推荐
        );
    }

    @Override
    public PrometheusQueryResponse queryRange(String cluster, String query, Long startTime, Long endTime, String step)
            throws PrometheusException {
        // Prometheus API 期望秒级时间戳，直接传递 Long 值即可
        String startTimeStr = startTime != null ? String.valueOf(startTime) : null;
        String endTimeStr = endTime != null ? String.valueOf(endTime) : null;
        
        String url = String.format("%s/api/v1/query_range?query=%s&start=%s&end=%s&step=%s",
                config.getPrometheusBaseUrl(cluster), query, startTimeStr, endTimeStr, step);
        return executeQueryWithRetry(url, PrometheusQueryResponse.class, config.getMaxRetries(), config.getRetryDelay());
    }

    @Override
    public PrometheusQueryResponse query(String cluster, String query, Long time) throws PrometheusException {
        String baseUrl = config.getPrometheusBaseUrl(cluster);
        String url = String.format("%s/api/v1/query?query=%s", baseUrl, query);
        if (time != null) {
            // Prometheus API 期望秒级时间戳，直接传递 Long 值即可
            String timeStr = String.valueOf(time);
            url += "&time=" + timeStr;
        }
        logger.info("Prometheus query URL: {}", url);
        logger.info("Base URL for cluster {}: {}", cluster, baseUrl);
        logger.info("Original query: {}", query);
        return executeQueryWithRetry(url, PrometheusQueryResponse.class, config.getMaxRetries(), config.getRetryDelay());
    }

    @Override
    public Boolean checkConnection(String cluster) throws PrometheusException {
        String url = String.format("%s/api/v1/status/config", config.getPrometheusBaseUrl(cluster));
        try {
            restTemplate.getForObject(url, String.class);
            logger.info("Prometheus connection check successful for cluster: {}", cluster);
            return true;
        } catch (Exception e) {
            logger.error("Prometheus connection check failed for cluster {}: {}", cluster, e.getMessage());
            return false;
        }
    }

    @Override
    public String getVersion(String cluster) throws PrometheusException {
        String url = String.format("%s/api/v1/status/buildinfo", config.getPrometheusBaseUrl(cluster));
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            logger.error("Failed to get Prometheus version for cluster {}: {}", cluster, e.getMessage());
            throw new PrometheusException("Failed to get Prometheus version", "VERSION_ERROR", null, e);
        }
    }

    /**
     * 执行同步查询请求
     */
    private <T> T executeQuery(String url, Class<T> responseType) throws PrometheusException {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (Exception e) {
            logger.error("Prometheus query failed: {}", e.getMessage());
            throw new PrometheusException("Query execution failed: " + e.getMessage(), "HTTP_ERROR", url, e);
        }
    }

    /**
     * 并发批量查询工具（示例，可在Service层调用）
     */
    public <T> List<T> parallelQuery(List<String> urls, Class<T> responseType) {
        Semaphore semaphore = new Semaphore(config.getMaxConcurrency());
        List<CompletableFuture<T>> futures = urls.stream()
                .map(query -> CompletableFuture.supplyAsync(() -> {
                    try {
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Semaphore acquire interrupted", ie);
                        }
                        return executeQuery(query, responseType);
                    } finally {
                        semaphore.release();
                    }
                }, executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    private <T> T executeQueryWithRetry(String url, Class<T> responseType, int maxRetries, long retryDelayMillis) throws PrometheusException {
        int attempt = 0;
        boolean enableRetry = config.isEnableRetry();
        int max = enableRetry ? maxRetries : 0;
        logger.info("Executing query with URL: {}", url);
        while (true) {
            try {
                return restTemplate.getForObject(url, responseType);
            } catch (Exception e) {
                attempt++;
                if (attempt > max) {
                    logger.error("Prometheus query failed after {} attempts: {}", attempt, e.getMessage());
                    throw new PrometheusException("Query execution failed after retries: " + e.getMessage(), "HTTP_ERROR", url, e);
                }
                logger.warn("Prometheus query failed (attempt {}/{}): {}. Retrying after {}ms...", attempt, max, e.getMessage(), retryDelayMillis);
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new PrometheusException("Retry interrupted", "RETRY_INTERRUPTED", url, ie);
                }
            }
        }
    }
} 