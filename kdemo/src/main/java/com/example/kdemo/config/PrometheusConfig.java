package com.example.kdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
    
    // 集群TLS配置 - 从Secret获取证书
    private Map<String, SecretTlsConfig> secretTlsConfigs = new HashMap<>();
    
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

    /**
     * 获取指定集群的Secret TLS配置
     */
    public SecretTlsConfig getSecretTlsConfig(String cluster) {
        if (cluster == null || cluster.isEmpty()) {
            cluster = "cluster-local";
        }
        return secretTlsConfigs.get(cluster);
    }

    // Getters and Setters
    public Map<String, String> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, String> clusters) {
        this.clusters = clusters;
    }

    public Map<String, SecretTlsConfig> getSecretTlsConfigs() {
        return secretTlsConfigs;
    }

    public void setSecretTlsConfigs(Map<String, SecretTlsConfig> secretTlsConfigs) {
        this.secretTlsConfigs = secretTlsConfigs;
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
     * Secret TLS配置类
     */
    public static class SecretTlsConfig {
        private String namespace = "default";
        private String secretName;
        private boolean skipSslVerification = false;
        
        public String getNamespace() {
            return namespace;
        }
        
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
        
        public String getSecretName() {
            return secretName;
        }
        
        public void setSecretName(String secretName) {
            this.secretName = secretName;
        }
        
        public boolean isSkipSslVerification() {
            return skipSslVerification;
        }
        
        public void setSkipSslVerification(boolean skipSslVerification) {
            this.skipSslVerification = skipSslVerification;
        }
        
        /**
         * 检查是否有有效的配置
         */
        public boolean isValid() {
            return secretName != null && !secretName.isEmpty();
        }
    }
} 