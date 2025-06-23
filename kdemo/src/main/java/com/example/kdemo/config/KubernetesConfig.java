package com.example.kdemo.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.example.kdemo.config.K8sClusterConfig;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "k8s")
public class KubernetesConfig {
    
    public static final String DEFAULT_CLUSTER = "cluster-local";
    
    private Map<String, K8sClusterConfig> clustersConfig = new HashMap<>();
    public Map<String, K8sClusterConfig> getClustersConfig() { return clustersConfig; }
    public void setClustersConfig(Map<String, K8sClusterConfig> clustersConfig) { this.clustersConfig = clustersConfig; }
    
    @Bean
    public ApiClient kubernetesApiClient() {
        try {
            return ClientBuilder.standard().build();
        } catch (IOException e) {
            try {
                return ClientBuilder.cluster().build();
            } catch (IOException ioException) {
                return new ApiClient();
            }
        }
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}

public class K8sClusterConfig {
    private String name;
    private String apiServer;
    private String token;
    private String caCert;
    // getter/setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getApiServer() { return apiServer; }
    public void setApiServer(String apiServer) { this.apiServer = apiServer; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getCaCert() { return caCert; }
    public void setCaCert(String caCert) { this.caCert = caCert; }
} 