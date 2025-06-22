package com.example.kdemo.util;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Kubernetes Secret工具类，用于从Secret中获取证书
 */
@Component
public class KubernetesSecretUtils {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesSecretUtils.class);
    
    private final ApiClient apiClient;
    private final CoreV1Api coreV1Api;
    
    // 缓存Secret数据，避免频繁查询
    private final Map<String, Map<String, String>> secretCache = new HashMap<>();
    
    @Autowired
    public KubernetesSecretUtils(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.coreV1Api = new CoreV1Api(apiClient);
    }
    
    /**
     * 获取集群的TLS证书配置
     * @param cluster 集群名称
     * @param namespace Secret所在的命名空间
     * @param secretName Secret名称
     * @return TLS证书配置
     */
    public TlsConfig getTlsConfigFromSecret(String cluster, String namespace, String secretName) {
        if ("cluster-local".equals(cluster)) {
            logger.debug("Skipping TLS config for cluster-local");
            return null;
        }
        
        String cacheKey = cluster + ":" + namespace + ":" + secretName;
        
        try {
            Map<String, String> secretData = secretCache.computeIfAbsent(cacheKey, k -> {
                try {
                    return getSecretData(cluster, namespace, secretName);
                } catch (Exception e) {
                    logger.error("Failed to get secret data for {}: {}", cacheKey, e.getMessage());
                    return new HashMap<>();
                }
            });
            
            if (secretData.isEmpty()) {
                logger.warn("No secret data found for cluster: {}", cluster);
                return null;
            }
            
            return buildTlsConfig(secretData);
            
        } catch (Exception e) {
            logger.error("Failed to get TLS config from secret for cluster {}: {}", cluster, e.getMessage());
            return null;
        }
    }
    
    /**
     * 从Kubernetes Secret获取数据
     */
    private Map<String, String> getSecretData(String cluster, String namespace, String secretName) throws ApiException {
        logger.debug("Fetching secret {} from namespace {} for cluster {}", secretName, namespace, cluster);
        
        V1Secret secret = coreV1Api.readNamespacedSecret(secretName, namespace).execute();
        
        if (secret == null || secret.getData() == null) {
            logger.warn("Secret {} not found or empty in namespace {}", secretName, namespace);
            return new HashMap<>();
        }
        
        Map<String, String> decodedData = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : secret.getData().entrySet()) {
            String key = entry.getKey();
            byte[] value = entry.getValue();
            if (value != null) {
                decodedData.put(key, new String(value, StandardCharsets.UTF_8));
            }
        }
        
        logger.debug("Successfully fetched secret data with {} keys", decodedData.size());
        return decodedData;
    }
    
    /**
     * 构建TLS配置
     */
    private TlsConfig buildTlsConfig(Map<String, String> secretData) {
        TlsConfig tlsConfig = new TlsConfig();
        
        // 常见的证书密钥名称
        String caCert = secretData.get("ca.crt") != null ? secretData.get("ca.crt") :
                       secretData.get("ca-cert.pem") != null ? secretData.get("ca-cert.pem") :
                       secretData.get("ca.pem") != null ? secretData.get("ca.pem") : null;
        
        String clientCert = secretData.get("tls.crt") != null ? secretData.get("tls.crt") :
                           secretData.get("client-cert.pem") != null ? secretData.get("client-cert.pem") :
                           secretData.get("cert.pem") != null ? secretData.get("cert.pem") : null;
        
        String clientKey = secretData.get("tls.key") != null ? secretData.get("tls.key") :
                          secretData.get("client-key.pem") != null ? secretData.get("client-key.pem") :
                          secretData.get("key.pem") != null ? secretData.get("key.pem") : null;
        
        tlsConfig.setCaCertificateContent(caCert);
        tlsConfig.setClientCertificateContent(clientCert);
        tlsConfig.setClientKeyContent(clientKey);
        
        // 检查是否有有效的TLS配置
        if (tlsConfig.hasTlsConfig()) {
            logger.debug("TLS config built successfully with CA: {}, Client Cert: {}, Client Key: {}", 
                caCert != null, clientCert != null, clientKey != null);
        } else {
            logger.warn("No valid TLS configuration found in secret data");
        }
        
        return tlsConfig;
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        secretCache.clear();
        logger.debug("Secret cache cleared");
    }
    
    /**
     * 清除指定集群的缓存
     */
    public void clearCacheForCluster(String cluster) {
        secretCache.entrySet().removeIf(entry -> entry.getKey().startsWith(cluster + ":"));
        logger.debug("Secret cache cleared for cluster: {}", cluster);
    }
    
    /**
     * TLS配置类
     */
    public static class TlsConfig {
        private String caCertificateContent;
        private String clientCertificateContent;
        private String clientKeyContent;
        private boolean skipSslVerification = false;
        
        public String getCaCertificateContent() {
            return caCertificateContent;
        }
        
        public void setCaCertificateContent(String caCertificateContent) {
            this.caCertificateContent = caCertificateContent;
        }
        
        public String getClientCertificateContent() {
            return clientCertificateContent;
        }
        
        public void setClientCertificateContent(String clientCertificateContent) {
            this.clientCertificateContent = clientCertificateContent;
        }
        
        public String getClientKeyContent() {
            return clientKeyContent;
        }
        
        public void setClientKeyContent(String clientKeyContent) {
            this.clientKeyContent = clientKeyContent;
        }
        
        public boolean isSkipSslVerification() {
            return skipSslVerification;
        }
        
        public void setSkipSslVerification(boolean skipSslVerification) {
            this.skipSslVerification = skipSslVerification;
        }
        
        /**
         * 检查是否有TLS配置
         */
        public boolean hasTlsConfig() {
            return (caCertificateContent != null && !caCertificateContent.isEmpty()) ||
                   (clientCertificateContent != null && !clientCertificateContent.isEmpty()) ||
                   (clientKeyContent != null && !clientKeyContent.isEmpty()) ||
                   skipSslVerification;
        }
    }
} 