package com.example.kdemo.service;

import com.example.kdemo.dto.*;
import com.example.kdemo.exception.KubernetesException;
import com.example.kdemo.exception.ResourceNotFoundException;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.repository.KubernetesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KubernetesService {

    private final KubernetesRepository repository;
    private final ObjectMapper objectMapper;
    private static final String DEFAULT_CLUSTER = "cluster-local";

    @Autowired
    public KubernetesService(KubernetesRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
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
        boolean connected = repository.isConnected(clusterName);
        return new ClusterInfo(connected, clusterName);
    }

    /**
     * 获取所有命名空间
     */
    public NamespaceInfo getNamespaces(String cluster) {
        String clusterName = getClusterName(cluster);
        List<String> namespaces = repository.getNamespaces(clusterName).getItems().stream()
                .map(namespace -> namespace.getMetadata().getName())
                .collect(Collectors.toList());
        return new NamespaceInfo(clusterName, namespaces);
    }

    /**
     * 获取指定命名空间中的所有 Pod
     */
    public PodInfo getPodsInNamespace(String cluster, String namespace) {
        String clusterName = getClusterName(cluster);
        List<String> pods = repository.getPodsInNamespace(clusterName, namespace).getItems().stream()
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
        String clusterName = getClusterName(cluster);
        List<String> crds = repository.getCustomResourceDefinitions(clusterName).getItems().stream()
                .map(crd -> crd.getMetadata().getName())
                .collect(Collectors.toList());
        return new CrdInfo(clusterName, crds);
    }

    /**
     * 获取指定CRD的详细信息
     */
    public Map<String, Object> getCustomResourceDefinition(String cluster, String name) {
        String clusterName = getClusterName(cluster);
        var crd = repository.getCustomResourceDefinition(clusterName, name);
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
        try {
            var createdCrd = repository.createCustomResourceDefinition(clusterName, crdYaml);
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
        List<Application> applications = repository.getApplications(clusterName, namespace);
        return new ResourceResponse<>(clusterName, namespace, applications);
    }

    /**
     * 获取指定的 Application 资源
     */
    public Application getApplication(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        return repository.getApplication(clusterName, namespace, name);
    }

    /**
     * 创建 Application 资源
     */
    public OperationResult createApplication(String cluster, String namespace, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        try {
            Application application = objectMapper.readValue(resourceYaml, Application.class);
            Application created = repository.createApplication(clusterName, namespace, application);
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
        try {
            Application application = objectMapper.readValue(resourceYaml, Application.class);
            Application updated = repository.updateApplication(clusterName, namespace, name, application);
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
        try {
            repository.deleteApplication(clusterName, namespace, name);
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
        List<Microservice> microservices = repository.getMicroservices(clusterName, namespace);
        return new ResourceResponse<>(clusterName, namespace, microservices);
    }

    /**
     * 获取指定的 Microservice 资源
     */
    public Microservice getMicroservice(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        return repository.getMicroservice(clusterName, namespace, name);
    }

    /**
     * 创建 Microservice 资源
     */
    public OperationResult createMicroservice(String cluster, String namespace, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        try {
            Microservice microservice = objectMapper.readValue(resourceYaml, Microservice.class);
            Microservice created = repository.createMicroservice(clusterName, namespace, microservice);
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
        try {
            Microservice microservice = objectMapper.readValue(resourceYaml, Microservice.class);
            Microservice updated = repository.updateMicroservice(clusterName, namespace, name, microservice);
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
        try {
            repository.deleteMicroservice(clusterName, namespace, name);
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
        List<GPU> gpus = repository.getGPUs(clusterName, namespace);
        return new ResourceResponse<>(clusterName, namespace, gpus);
    }

    /**
     * 获取指定的 GPU 资源
     */
    public GPU getGPU(String cluster, String namespace, String name) {
        String clusterName = getClusterName(cluster);
        return repository.getGPU(clusterName, namespace, name);
    }

    /**
     * 创建 GPU 资源
     */
    public OperationResult createGPU(String cluster, String namespace, String resourceYaml) {
        String clusterName = getClusterName(cluster);
        try {
            GPU gpu = objectMapper.readValue(resourceYaml, GPU.class);
            GPU created = repository.createGPU(clusterName, namespace, gpu);
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
        try {
            GPU gpu = objectMapper.readValue(resourceYaml, GPU.class);
            GPU updated = repository.updateGPU(clusterName, namespace, name, gpu);
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
        try {
            repository.deleteGPU(clusterName, namespace, name);
            return OperationResult.success(clusterName, name, "GPU deleted successfully");
        } catch (Exception e) {
            return OperationResult.failure(clusterName, name, "Failed to delete GPU", e.getMessage());
        }
    }
} 