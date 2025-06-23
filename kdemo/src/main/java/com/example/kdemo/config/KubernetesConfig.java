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
import com.example.kdemo.service.ClusterService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "k8s")
public class KubernetesConfig {
    
    public static final String DEFAULT_CLUSTER = "cluster-local";
    
    private Map<String, K8sClusterConfig> clustersConfig = new HashMap<>();
    public Map<String, K8sClusterConfig> getClustersConfig() { return clustersConfig; }
    public void setClustersConfig(Map<String, K8sClusterConfig> clustersConfig) { this.clustersConfig = clustersConfig; }
    
    @Autowired
    private ClusterService clusterService;

    @PostConstruct
    public void initClusters() {
        for (Map.Entry<String, K8sClusterConfig> entry : clustersConfig.entrySet()) {
            clusterService.registerCluster(entry.getKey(), entry.getValue());
        }
    }

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