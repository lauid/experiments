package com.example.kdemo.repository;

import com.example.kdemo.config.PrometheusConfig;
import com.example.kdemo.dto.PrometheusQueryResponse;
import com.example.kdemo.exception.PrometheusException;
import com.example.kdemo.util.KubernetesSecretUtils;
import com.example.kdemo.util.TlsUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    // 缓存每个集群的WebClient
    private final ConcurrentMap<String, WebClient> webClientCache = new ConcurrentHashMap<>();

    @Autowired
    public PrometheusRepositoryImpl(PrometheusConfig config, ObjectMapper objectMapper, 
                                   KubernetesSecretUtils kubernetesSecretUtils, TlsUtils tlsUtils) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.kubernetesSecretUtils = kubernetesSecretUtils;
        this.tlsUtils = tlsUtils;
    }

    private WebClient getWebClient(String cluster) {
        return webClientCache.computeIfAbsent(cluster == null ? "cluster-local" : cluster, c -> {
            try {
                return createWebClientWithTls(c);
            } catch (Exception e) {
                logger.error("Failed to create WebClient for cluster {}: {}", c, e.getMessage());
                // 如果TLS配置失败，创建默认的WebClient
                return createDefaultWebClient(c);
            }
        });
    }

    /**
     * 创建支持TLS的WebClient
     */
    private WebClient createWebClientWithTls(String cluster) throws Exception {
        // 如果是cluster-local，直接使用默认WebClient
        if ("cluster-local".equals(cluster)) {
            logger.debug("Using default WebClient for cluster-local");
            return createDefaultWebClient(cluster);
        }
        
        // 获取Secret TLS配置
        PrometheusConfig.SecretTlsConfig secretTlsConfig = config.getSecretTlsConfig(cluster);
        if (secretTlsConfig == null || !secretTlsConfig.isValid()) {
            logger.debug("No valid Secret TLS configuration for cluster {}, using default WebClient", cluster);
            return createDefaultWebClient(cluster);
        }
        
        // 从Kubernetes Secret获取TLS证书
        KubernetesSecretUtils.TlsConfig tlsConfig = kubernetesSecretUtils.getTlsConfigFromSecret(
            cluster, secretTlsConfig.getNamespace(), secretTlsConfig.getSecretName()
        );
        
        if (tlsConfig == null || !tlsConfig.hasTlsConfig()) {
            logger.warn("No TLS configuration found in secret for cluster {}, using default WebClient", cluster);
            return createDefaultWebClient(cluster);
        }
        
        logger.debug("Creating WebClient with TLS configuration for cluster {}", cluster);
        
        // 创建HttpClient
        HttpClient httpClient = HttpClient.create()
                .secure(spec -> {
                    try {
                        if (tlsConfig.isSkipSslVerification() || secretTlsConfig.isSkipSslVerification()) {
                            spec.sslContext(SslContextBuilder.forClient()
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                    .build());
                        } else {
                            // 使用默认的SSL上下文
                            spec.sslContext(SslContextBuilder.forClient()
                                    .build());
                        }
                    } catch (Exception e) {
                        logger.error("Failed to configure SSL context for cluster {}: {}", cluster, e.getMessage());
                        throw new RuntimeException("SSL configuration failed", e);
                    }
                })
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getTimeout())
                .responseTimeout(Duration.ofMillis(config.getTimeout()));
        
        return WebClient.builder()
                .baseUrl(config.getPrometheusBaseUrl(cluster))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
    
    /**
     * 创建默认的WebClient（无TLS）
     */
    private WebClient createDefaultWebClient(String cluster) {
        return WebClient.builder()
                .baseUrl(config.getPrometheusBaseUrl(cluster))
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    private <T> Mono<T> executeWithRetry(Mono<T> mono, String operation) {
        if (config.isEnableRetry() && config.getMaxRetries() > 0) {
            return mono.retryWhen(
                reactor.util.retry.Retry.backoff(config.getMaxRetries(), java.time.Duration.ofMillis(config.getRetryDelay()))
                    .onRetryExhaustedThrow((spec, signal) -> signal.failure())
            );
        } else {
            return mono;
        }
    }

    @Override
    public Mono<PrometheusQueryResponse> queryRange(String cluster, String query, String startTime, String endTime, String step)
            throws PrometheusException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("/api/v1/query_range?query=%s&start=%s&end=%s&step=%s",
                encodedQuery, startTime, endTime, step);
        return executeWithRetry(executeQuery(cluster, url, "query_range", query), "query_range");
    }

    @Override
    public Mono<PrometheusQueryResponse> query(String cluster, String query, String time) throws PrometheusException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "/api/v1/query?query=" + encodedQuery;
        if (time != null && !time.isEmpty()) {
            url += "&time=" + time;
        }
        return executeWithRetry(executeQuery(cluster, url, "query", query), "query");
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