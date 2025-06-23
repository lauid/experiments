package com.example.kdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Prometheus配置类
 */
@Component
@ConfigurationProperties(prefix = "prometheus")
public class PrometheusConfig {
    // 多集群Prometheus地址
    private Map<String, String> clusters = new HashMap<>();
    private int timeout = 30000;
    private int maxConcurrency = 10;
    private boolean enableRetry = true;
    private int maxRetries = 3;
    private long retryDelay = 1000;

    /**
     * 获取指定集群的Prometheus地址，未配置时返回cluster-local
     */
    public String getPrometheusBaseUrl(String cluster) {
        if (cluster == null || cluster.isEmpty()) {
            cluster = "cluster-local";
        }
        return clusters.getOrDefault(cluster, clusters.getOrDefault("cluster-local", "http://localhost:9090"));
    }

    // Getters and Setters
    public Map<String, String> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, String> clusters) {
        this.clusters = clusters;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }

    public boolean isEnableRetry() {
        return enableRetry;
    }

    public void setEnableRetry(boolean enableRetry) {
        this.enableRetry = enableRetry;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }
} 