package com.example.kdemo.repository;

import com.example.kdemo.config.PrometheusConfig;
import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prometheus数据访问实现类
 */
@Repository
public class PrometheusRepositoryImpl implements PrometheusRepository {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusRepositoryImpl.class);
    private final PrometheusConfig config;
    private final ObjectMapper objectMapper;
    // 缓存每个集群的WebClient
    private final Map<String, WebClient> webClientCache = new ConcurrentHashMap<>();

    @Autowired
    public PrometheusRepositoryImpl(PrometheusConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    private WebClient getWebClient(String cluster) {
        return webClientCache.computeIfAbsent(cluster == null ? "cluster-local" : cluster, c ->
                WebClient.builder()
                        .baseUrl(config.getPrometheusBaseUrl(c))
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build()
        );
    }

    @Override
    public Mono<PrometheusQueryResponse> queryRange(String cluster, String query, String startTime, String endTime, String step)
            throws PrometheusException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("/api/v1/query_range?query=%s&start=%s&end=%s&step=%s",
                encodedQuery, startTime, endTime, step);
        return executeQuery(cluster, url, "query_range", query);
    }

    @Override
    public Mono<PrometheusQueryResponse> query(String cluster, String query, String time) throws PrometheusException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "/api/v1/query?query=" + encodedQuery;
        if (time != null && !time.isEmpty()) {
            url += "&time=" + time;
        }
        return executeQuery(cluster, url, "query", query);
    }

    @Override
    public Mono<Boolean> checkConnection(String cluster) throws PrometheusException {
        return getWebClient(cluster).get()
                .uri("/api/v1/status/config")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    logger.info("Prometheus connection check successful for cluster: {}", cluster);
                    return true;
                })
                .timeout(Duration.ofMillis(config.getTimeout()))
                .onErrorResume(throwable -> {
                    logger.error("Prometheus connection check failed for cluster {}: {}", cluster, throwable.getMessage());
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<String> getVersion(String cluster) throws PrometheusException {
        return getWebClient(cluster).get()
                .uri("/api/v1/status/buildinfo")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .onErrorMap(throwable -> {
                    logger.error("Failed to get Prometheus version for cluster {}: {}", cluster, throwable.getMessage());
                    return new PrometheusException("Failed to get Prometheus version", "VERSION_ERROR", null, throwable);
                });
    }

    /**
     * 执行查询请求
     */
    private Mono<PrometheusQueryResponse> executeQuery(String cluster, String url, String queryType, String query) {
        return getWebClient(cluster).get()
                .uri(url)
                .retrieve()
                .bodyToMono(PrometheusQueryResponse.class)
                .timeout(Duration.ofMillis(config.getTimeout()))
                .doOnSuccess(response -> logger.debug("Prometheus {} query successful for cluster {}: {}", queryType, cluster, query))
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("Prometheus HTTP error for {} query in cluster {}: {} - {}", queryType, cluster, e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.error(new PrometheusException(
                            "Prometheus HTTP error: " + e.getStatusCode(),
                            "HTTP_ERROR",
                            query,
                            e));
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof PrometheusException) {
                        return Mono.error(throwable);
                    }
                    // This will now catch WebClient's decoding errors
                    logger.error("Prometheus {} query failed for cluster {}: {} - {}", queryType, cluster, query, throwable.getMessage());
                    return Mono.error(new PrometheusException(
                            "Query execution failed: " + throwable.getMessage(),
                            "PARSE_ERROR", // More specific error type
                            query,
                            throwable));
                });
    }
} 