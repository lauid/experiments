package com.example.kdemo.repository;

import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.ApiClient;

import java.util.List;
import java.util.Map;

public interface KubernetesRepository {
    
    // 基础Kubernetes资源操作
    V1NamespaceList getNamespaces(String cluster);
    V1PodList getPodsInNamespace(String cluster, String namespace);
    V1CustomResourceDefinitionList getCustomResourceDefinitions(ApiClient apiClient);
    V1CustomResourceDefinition getCustomResourceDefinition(ApiClient apiClient, String name);
    V1CustomResourceDefinition createCustomResourceDefinition(ApiClient apiClient, String crdYaml);
    
    // Application资源操作
    List<Application> getApplications(ApiClient apiClient, String namespace);
    Application getApplication(ApiClient apiClient, String namespace, String name);
    Application createApplication(ApiClient apiClient, String namespace, Application application);
    Application updateApplication(ApiClient apiClient, String namespace, String name, Application application);
    void deleteApplication(ApiClient apiClient, String namespace, String name);
    
    // Microservice资源操作
    List<Microservice> getMicroservices(ApiClient apiClient, String namespace);
    Microservice getMicroservice(ApiClient apiClient, String namespace, String name);
    Microservice createMicroservice(ApiClient apiClient, String namespace, Microservice microservice);
    Microservice updateMicroservice(ApiClient apiClient, String namespace, String name, Microservice microservice);
    void deleteMicroservice(ApiClient apiClient, String namespace, String name);
    
    // GPU资源操作
    List<GPU> getGPUs(ApiClient apiClient, String namespace);
    GPU getGPU(ApiClient apiClient, String namespace, String name);
    GPU createGPU(ApiClient apiClient, String namespace, GPU gpu);
    GPU updateGPU(ApiClient apiClient, String namespace, String name, GPU gpu);
    void deleteGPU(ApiClient apiClient, String namespace, String name);
    
    /**
     * Patch Node 的 labels 字段
     */
    V1Node patchNodeLabels(String cluster, String nodeName, Map<String, String> labels);

    /**
     * Patch Node 的 spec 字段
     */
    V1Node patchNodeSpec(String cluster, String nodeName, Map<String, Object> specPatch);

    /**
     * Patch Node，支持任意结构化 patch
     */
    V1Node patchNodeRaw(String cluster, String nodeName, Object patchObject);
    
    /**
     * 自动 diff patch node，传入新 node 对象，自动与集群现有对象对比并 patch（metadata/spec）。
     */
    V1Node patchNodeAuto(String cluster, V1Node newNode);

    /**
     * 自动 diff patch node 的 status 子资源，传入新 node 对象，自动与集群现有对象对比并 patch status。
     */
    V1Node patchNodeStatusAuto(String cluster, V1Node newNode);
    
    // 连接检查
    boolean isConnected(String cluster);
} 