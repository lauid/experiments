package com.example.kdemo.service;

import com.example.kdemo.dto.*;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.repository.KubernetesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.kubernetes.client.openapi.ApiClient;
import java.util.concurrent.ConcurrentHashMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;

@Service
public class KubernetesService {

    private final KubernetesRepository repository;
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_CLUSTER = "cluster-local";
    private final Map<String, ApiClient> apiClientMap = new ConcurrentHashMap<>();
    private final ApiClient defaultApiClient;

    @Autowired
    public KubernetesService(KubernetesRepository repository, ObjectMapper objectMapper, ApiClient defaultApiClient) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.defaultApiClient = defaultApiClient;
        apiClientMap.put("cluster-local", defaultApiClient);
        // 可在此注册其他集群 ApiClient
    }

    private String getClusterName(String cluster) {
        return cluster != null && !cluster.isEmpty() ? cluster : DEFAULT_CLUSTER;
    }

    private ApiClient getApiClient(String cluster) {
        return apiClientMap.getOrDefault(cluster, defaultApiClient);
    }

    /**
     * 检查 Kubernetes 连接
     */
    public ClusterInfo checkConnection(String cluster) {
        String clusterName = getClusterName(cluster);
        boolean connected = repository.isConnected(clusterName);
        return new ClusterInfo(connected, clusterName);
    }

    /**
     * 获取所有命名空间
     */
    public NamespaceInfo getNamespaces(String cluster) {
        String clusterName = getClusterName(cluster);
        V1NamespaceList namespaces = repository.getNamespaces(clusterName);
        List<String> namespaceNames = namespaces.getItems().stream()
                .map(namespace -> namespace.getMetadata().getName())
                .collect(Collectors.toList());
        return new NamespaceInfo(clusterName, namespaceNames);
    }

    /**
     * 获取指定命名空间中的所有 Pod
     */
    public PodInfo getPodsInNamespace(String cluster, String namespace) {
        String clusterName = getClusterName(cluster);
        V1PodList pods = repository.getPodsInNamespace(clusterName, namespace);
        List<String> podNames = pods.getItems().stream()
                .map(pod -> pod.getMetadata().getName())
                .collect(Collectors.toList());
        return new PodInfo(clusterName, namespace, podNames);
    }

    /**
     * 获取集群概览
     */
    public ClusterOverview getClusterOverview(String cluster) {
        String clusterName = getClusterName(cluster);
        NamespaceInfo namespaceInfo = getNamespaces(clusterName);
        
        Map<String, Integer> podsPerNamespace = namespaceInfo.getNamespaces().stream()
                .collect(Collectors.toMap(
                        ns -> ns,
                        ns -> repository.getPodsInNamespace(clusterName, ns).getItems().size()
                ));
        
        int totalPods = podsPerNamespace.values().stream().mapToInt(Integer::intValue).sum();
        
        return new ClusterOverview(
                clusterName,
                namespaceInfo.getCount(),
                totalPods,
                podsPerNamespace,
                namespaceInfo.getNamespaces()
        );
    }

    /**
     * 获取所有CRD
     */
    public CrdInfo getCustomResourceDefinitions(String cluster) {
        ApiClient apiClient = getApiClient(cluster);
        List<String> crds = repository.getCustomResourceDefinitions(apiClient).getItems().stream()
                .map(crd -> crd.getMetadata().getName())
                .collect(Collectors.toList());
        return new CrdInfo(getClusterName(cluster), crds);
    }

    /**
     * 获取指定CRD的详细信息
     */
    public Map<String, Object> getCustomResourceDefinition(String cluster, String name) {
        ApiClient apiClient = getApiClient(cluster);
        var crd = repository.getCustomResourceDefinition(apiClient, name);
        return Map.of(
                "name", crd.getMetadata().getName(),
                "group", crd.getSpec().getGroup(),
                "version", crd.getSpec().getVersions().get(0).getName(),
                "kind", crd.getSpec().getNames().getKind(),
                "plural", crd.getSpec().getNames().getPlural(),
                "singular", crd.getSpec().getNames().getSingular(),
                "scope", crd.getSpec().getScope()
        );
    }

    /**
     * 创建CRD
     */
    public OperationResult createCustomResourceDefinition(String cluster, String crdYaml) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            var createdCrd = repository.createCustomResourceDefinition(apiClient, crdYaml);
            return OperationResult.success(getClusterName(cluster), createdCrd.getMetadata().getName(), "CRD created successfully");
        } catch (Exception e) {
            return OperationResult.failure(getClusterName(cluster), "CRD", "Failed to create CRD", e.getMessage());
        }
    }

    // ========== Application 资源操作 ==========

    /**
     * 获取所有 Application 资源
     */
    public ResourceResponse<Application> getApplications(String cluster, String namespace) {
        ApiClient apiClient = getApiClient(cluster);
        List<Application> applications = repository.getApplications(apiClient, namespace);
        return new ResourceResponse<>(cluster, namespace, applications);
    }

    /**
     * 获取指定的 Application 资源
     */
    public Application getApplication(String cluster, String namespace, String name) {
        ApiClient apiClient = getApiClient(cluster);
        return repository.getApplication(apiClient, namespace, name);
    }

    /**
     * 创建 Application 资源
     */
    public OperationResult createApplication(String cluster, String namespace, String resourceYaml) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            Application application = objectMapper.readValue(resourceYaml, Application.class);
            Application created = repository.createApplication(apiClient, namespace, application);
            return OperationResult.success(cluster, created.getMetadata().getName(), "Application created successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, "Application", "Failed to create application", e.getMessage());
        }
    }

    /**
     * 更新 Application 资源
     */
    public OperationResult updateApplication(String cluster, String namespace, String name, String resourceYaml) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            Application application = objectMapper.readValue(resourceYaml, Application.class);
            Application updated = repository.updateApplication(apiClient, namespace, name, application);
            return OperationResult.success(cluster, updated.getMetadata().getName(), "Application updated successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, name, "Failed to update application", e.getMessage());
        }
    }

    /**
     * 删除 Application 资源
     */
    public OperationResult deleteApplication(String cluster, String namespace, String name) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            repository.deleteApplication(apiClient, namespace, name);
            return OperationResult.success(cluster, name, "Application deleted successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, name, "Failed to delete application", e.getMessage());
        }
    }

    // ========== Microservice 资源操作 ==========

    /**
     * 获取所有 Microservice 资源
     */
    public ResourceResponse<Microservice> getMicroservices(String cluster, String namespace) {
        ApiClient apiClient = getApiClient(cluster);
        List<Microservice> microservices = repository.getMicroservices(apiClient, namespace);
        return new ResourceResponse<>(cluster, namespace, microservices);
    }

    /**
     * 获取指定的 Microservice 资源
     */
    public Microservice getMicroservice(String cluster, String namespace, String name) {
        ApiClient apiClient = getApiClient(cluster);
        return repository.getMicroservice(apiClient, namespace, name);
    }

    /**
     * 创建 Microservice 资源
     */
    public OperationResult createMicroservice(String cluster, String namespace, String resourceYaml) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            Microservice microservice = objectMapper.readValue(resourceYaml, Microservice.class);
            Microservice created = repository.createMicroservice(apiClient, namespace, microservice);
            return OperationResult.success(cluster, created.getMetadata().getName(), "Microservice created successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, "Microservice", "Failed to create microservice", e.getMessage());
        }
    }

    /**
     * 更新 Microservice 资源
     */
    public OperationResult updateMicroservice(String cluster, String namespace, String name, String resourceYaml) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            Microservice microservice = objectMapper.readValue(resourceYaml, Microservice.class);
            Microservice updated = repository.updateMicroservice(apiClient, namespace, name, microservice);
            return OperationResult.success(cluster, updated.getMetadata().getName(), "Microservice updated successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, name, "Failed to update microservice", e.getMessage());
        }
    }

    /**
     * 删除 Microservice 资源
     */
    public OperationResult deleteMicroservice(String cluster, String namespace, String name) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            repository.deleteMicroservice(apiClient, namespace, name);
            return OperationResult.success(cluster, name, "Microservice deleted successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, name, "Failed to delete microservice", e.getMessage());
        }
    }

    // ========== GPU 资源操作 ==========

    /**
     * 获取所有 GPU 资源
     */
    public ResourceResponse<GPU> getGPUs(String cluster, String namespace) {
        ApiClient apiClient = getApiClient(cluster);
        List<GPU> gpus = repository.getGPUs(apiClient, namespace);
        return new ResourceResponse<>(cluster, namespace, gpus);
    }

    /**
     * 获取指定的 GPU 资源
     */
    public GPU getGPU(String cluster, String namespace, String name) {
        ApiClient apiClient = getApiClient(cluster);
        return repository.getGPU(apiClient, namespace, name);
    }

    /**
     * 创建 GPU 资源
     */
    public OperationResult createGPU(String cluster, String namespace, String resourceYaml) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            GPU gpu = objectMapper.readValue(resourceYaml, GPU.class);
            GPU created = repository.createGPU(apiClient, namespace, gpu);
            return OperationResult.success(cluster, created.getMetadata().getName(), "GPU created successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, "GPU", "Failed to create GPU", e.getMessage());
        }
    }

    /**
     * 更新 GPU 资源
     */
    public OperationResult updateGPU(String cluster, String namespace, String name, String resourceYaml) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            repository.getGPU(apiClient, namespace, name);
            
            // 解析JSON
            GPU gpu = objectMapper.readValue(resourceYaml, GPU.class);
            if (gpu == null) {
                return OperationResult.failure(cluster, name, "Failed to update GPU", "Parsed GPU object is null");
            }
            
            // 确保metadata不为null
            if (gpu.getMetadata() == null) {
                return OperationResult.failure(cluster, name, "Failed to update GPU", "GPU metadata is null");
            }
            
            GPU updated = repository.updateGPU(apiClient, namespace, name, gpu);
            if (updated == null) {
                return OperationResult.failure(cluster, name, "Failed to update GPU", "Update operation returned null");
            }
            return OperationResult.success(cluster, updated.getMetadata().getName(), "GPU updated successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, name, "Failed to update GPU", e.getMessage());
        }
    }

    /**
     * 删除 GPU 资源
     */
    public OperationResult deleteGPU(String cluster, String namespace, String name) {
        ApiClient apiClient = getApiClient(cluster);
        try {
            repository.deleteGPU(apiClient, namespace, name);
            return OperationResult.success(cluster, name, "GPU deleted successfully");
        } catch (Exception e) {
            return OperationResult.failure(cluster, name, "Failed to delete GPU", e.getMessage());
        }
    }
} 