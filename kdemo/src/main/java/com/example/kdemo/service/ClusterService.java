package com.example.kdemo.service;

import com.example.kdemo.config.K8sClusterConfig;
import io.kubernetes.client.openapi.ApiClient;

public interface ClusterService {
    ApiClient getApiClient(String clusterName);
    void registerCluster(String clusterName, K8sClusterConfig config);
} 