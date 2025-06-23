package com.example.kdemo.service;

import com.example.kdemo.config.K8sClusterConfig;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClusterServiceImpl implements ClusterService {
    private final Map<String, ApiClient> apiClientMap = new ConcurrentHashMap<>();
    private final Map<String, K8sClusterConfig> configMap = new ConcurrentHashMap<>();

    @Override
    public ApiClient getApiClient(String clusterName) {
        return apiClientMap.get(clusterName);
    }

    @Override
    public void registerCluster(String clusterName, K8sClusterConfig config) {
        ApiClient client = buildApiClient(config);
        apiClientMap.put(clusterName, client);
        configMap.put(clusterName, config);
    }

    private ApiClient buildApiClient(K8sClusterConfig conf) {
        try {
            ApiClient client = ClientBuilder.standard()
                .setBasePath(conf.getApiServer())
                .setCertificateAuthority(conf.getCaCert().getBytes(StandardCharsets.UTF_8))
                .build();
            client.setApiKey("Bearer " + conf.getToken());
            return client;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build ApiClient for cluster " + conf.getName(), e);
        }
    }
} 