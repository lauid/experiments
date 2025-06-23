package com.example.kdemo.service;

import com.example.kdemo.dto.*;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.repository.KubernetesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.kdemo.service.ClusterService;
import io.kubernetes.client.openapi.ApiClient;
import com.example.kdemo.config.KubernetesConfig;
import com.example.kdemo.repository.KubernetesRepositoryImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KubernetesService {

    private final ClusterService clusterService;
    private final KubernetesConfig k8sConfig;
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_CLUSTER = "cluster-local";

    @Autowired
    public KubernetesService(ClusterService clusterService, KubernetesConfig k8sConfig, ObjectMapper objectMapper) {
        this.clusterService = clusterService;
        this.k8sConfig = k8sConfig;
        this.objectMapper = objectMapper;
    }

    private String getClusterName(String cluster) {
        return cluster != null && !cluster.isEmpty() ? cluster : DEFAULT_CLUSTER;
    }

    /**
     * 检查 Kubernetes 连接
     */
    public ClusterInfo checkConnection(String cluster) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        boolean connected = repo.isConnected();
        return new ClusterInfo(connected, clusterName);
    }

    /**
     * 获取所有命名空间
     */
    public NamespaceInfo getNamespaces(String cluster) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        List<String> namespaces = repo.getNamespaces().getItems().stream()
                .map(namespace -> namespace.getMetadata().getName())
                .collect(Collectors.toList());
        return new NamespaceInfo(clusterName, namespaces);
    }

    /**
     * 获取指定命名空间中的所有 Pod
     */
    public PodInfo getPodsInNamespace(String cluster, String namespace) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        List<String> pods = repo.getPodsInNamespace().getItems().stream()
                .map(pod -> pod.getMetadata().getName())
                .collect(Collectors.toList());
        return new PodInfo(clusterName, namespace, pods);
    }

    /**
     * 获取集群概览
     */
    public ClusterOverview getClusterOverview(String cluster) {
        String clusterName = getClusterName(cluster);
        NamespaceInfo namespaceInfo = getNamespaces(clusterName);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        Map<String, Integer> podsPerNamespace = namespaceInfo.getNamespaces().stream()
                .collect(Collectors.toMap(
                        ns -> ns,
                        ns -> repo.getPodsInNamespace().getItems().size()
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
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        List<String> crds = repo.getCustomResourceDefinitions().getItems().stream()
                .map(crd -> crd.getMetadata().getName())
                .collect(Collectors.toList());
        return new CrdInfo(clusterName, crds);
    }

    /**
     * 获取指定CRD的详细信息
     */
    public Map<String, Object> getCustomResourceDefinition(String cluster, String name) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        var crd = repo.getCustomResourceDefinition(name);
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
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            var createdCrd = repo.createCustomResourceDefinition(crdYaml);
            return OperationResult.success(clusterName, createdCrd.getMetadata().getName(), "CRD created successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, "CRD", "Failed to create CRD", e.getMessage());
        }
    }

    // ========== Application 资源操作 ==========

    /**
     * 获取所有 Application 资源
     */
    public ResourceResponse<Application> getApplications(String cluster, String namespace) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        List<Application> applications = repo.getApplications();
        return new ResourceResponse<>(clusterName, namespace, applications);
    }

    /**
     * 获取指定的 Application 资源
     */
    public Application getApplication(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        return repo.getApplication(name);
    }

    /**
     * 创建 Application 资源
     */
    public OperationResult createApplication(String cluster, String namespace, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            Application application = objectMapper.readValue(resourceYaml, Application.class);
            Application created = repo.createApplication(application);
            return OperationResult.success(clusterName, created.getMetadata().getName(), "Application created successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, "Application", "Failed to create application", e.getMessage());
        }
    }

    /**
     * 更新 Application 资源
     */
    public OperationResult updateApplication(String cluster, String namespace, String name, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            Application application = objectMapper.readValue(resourceYaml, Application.class);
            Application updated = repo.updateApplication(name, application);
            return OperationResult.success(clusterName, updated.getMetadata().getName(), "Application updated successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, name, "Failed to update application", e.getMessage());
        }
    }

    /**
     * 删除 Application 资源
     */
    public OperationResult deleteApplication(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            repo.deleteApplication(name);
            return OperationResult.success(clusterName, name, "Application deleted successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, name, "Failed to delete application", e.getMessage());
        }
    }

    // ========== Microservice 资源操作 ==========

    /**
     * 获取所有 Microservice 资源
     */
    public ResourceResponse<Microservice> getMicroservices(String cluster, String namespace) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        List<Microservice> microservices = repo.getMicroservices();
        return new ResourceResponse<>(clusterName, namespace, microservices);
    }

    /**
     * 获取指定的 Microservice 资源
     */
    public Microservice getMicroservice(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        return repo.getMicroservice(name);
    }

    /**
     * 创建 Microservice 资源
     */
    public OperationResult createMicroservice(String cluster, String namespace, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            Microservice microservice = objectMapper.readValue(resourceYaml, Microservice.class);
            Microservice created = repo.createMicroservice(microservice);
            return OperationResult.success(clusterName, created.getMetadata().getName(), "Microservice created successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, "Microservice", "Failed to create microservice", e.getMessage());
        }
    }

    /**
     * 更新 Microservice 资源
     */
    public OperationResult updateMicroservice(String cluster, String namespace, String name, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            Microservice microservice = objectMapper.readValue(resourceYaml, Microservice.class);
            Microservice updated = repo.updateMicroservice(name, microservice);
            return OperationResult.success(clusterName, updated.getMetadata().getName(), "Microservice updated successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, name, "Failed to update microservice", e.getMessage());
        }
    }

    /**
     * 删除 Microservice 资源
     */
    public OperationResult deleteMicroservice(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            repo.deleteMicroservice(name);
            return OperationResult.success(clusterName, name, "Microservice deleted successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, name, "Failed to delete microservice", e.getMessage());
        }
    }

    // ========== GPU 资源操作 ==========

    /**
     * 获取所有 GPU 资源
     */
    public ResourceResponse<GPU> getGPUs(String cluster, String namespace) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        List<GPU> gpus = repo.getGPUs();
        return new ResourceResponse<>(clusterName, namespace, gpus);
    }

    /**
     * 获取指定的 GPU 资源
     */
    public GPU getGPU(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        return repo.getGPU(name);
    }

    /**
     * 创建 GPU 资源
     */
    public OperationResult createGPU(String cluster, String namespace, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            GPU gpu = objectMapper.readValue(resourceYaml, GPU.class);
            GPU created = repo.createGPU(gpu);
            return OperationResult.success(clusterName, created.getMetadata().getName(), "GPU created successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, "GPU", "Failed to create GPU", e.getMessage());
        }
    }

    /**
     * 更新 GPU 资源
     */
    public OperationResult updateGPU(String cluster, String namespace, String name, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            repo.getGPU(name);
            GPU gpu = objectMapper.readValue(resourceYaml, GPU.class);
            if (gpu == null) {
                return OperationResult.failure(clusterName, name, "Failed to update GPU", "Parsed GPU object is null");
            }
            if (gpu.getMetadata() == null) {
                return OperationResult.failure(clusterName, name, "Failed to update GPU", "GPU metadata is null");
            }
            GPU updated = repo.updateGPU(name, gpu);
            if (updated == null) {
                return OperationResult.failure(clusterName, name, "Failed to update GPU", "Update operation returned null");
            }
            return OperationResult.success(clusterName, updated.getMetadata().getName(), "GPU updated successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, name, "Failed to update GPU", e.getMessage());
        }
    }

    /**
     * 删除 GPU 资源
     */
    public OperationResult deleteGPU(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        try {
            repo.deleteGPU(name);
            return OperationResult.success(clusterName, name, "GPU deleted successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, name, "Failed to delete GPU", e.getMessage());
        }
    }

    // 优化后的 Pod 查询方法
    List<String> getPods(String cluster, ResourceQuery query) {
        String clusterName = getClusterName(cluster);
        ApiClient client = clusterService.getApiClient(clusterName);
        KubernetesRepository repo = new KubernetesRepositoryImpl(client, k8sConfig);
        return repo.getPods(query).stream()
                .map(pod -> pod.getMetadata().getName())
                .collect(Collectors.toList());
    }
} 