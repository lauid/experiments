package com.example.kdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Prometheus配置类
 */
@Component
@ConfigurationProperties(prefix = "prometheus")
public class PrometheusConfig {
    // 多集群Prometheus地址
    private Map<String, String> clusters = new HashMap<>();
    private int timeout = 30000;
    private int maxConcurrency = 50; // 增加默认并发数
    private boolean enableRetry = true;
    private int maxRetries = 3;
    private long retryDelay = 1000;
    
    // 新增线程池配置
    private int corePoolSize = 20;
    private int maximumPoolSize = 100;
    private int keepAliveTime = 60;
    private int queueCapacity = 1000;
    private int batchSize = 100; // 批量处理大小
    
    // 新增超时配置
    private int batchTimeout = 30000; // 批量查询超时时间
    private int individualQueryTimeout = 10000; // 单个查询超时时间

    /**
     * 获取指定集群的Prometheus地址，未配置时返回cluster-local
     */
    public String getPrometheusBaseUrl(String cluster) {
        if (cluster == null || cluster.isEmpty()) {
            cluster = "cluster-local";
        }
        return clusters.getOrDefault(cluster, clusters.getOrDefault("cluster-local", "http://localhost:9090"));
    }

    /**
     * 创建专用的Prometheus查询线程池
     */
    @Bean("prometheusQueryExecutor")
    public ExecutorService prometheusQueryExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy() // 使用CallerRunsPolicy防止任务丢失
        );
        
        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);
        
        return executor;
    }

    /**
     * RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        
        // 明确禁用代理
        factory.setProxy(null);
        
        return new RestTemplate(factory);
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

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchTimeout() {
        return batchTimeout;
    }

    public void setBatchTimeout(int batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    public int getIndividualQueryTimeout() {
        return individualQueryTimeout;
    }

    public void setIndividualQueryTimeout(int individualQueryTimeout) {
        this.individualQueryTimeout = individualQueryTimeout;
    }
} 