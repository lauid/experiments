package com.example.kdemo.repository;

import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Node;

import java.util.List;
import java.util.Map;

public interface KubernetesRepository {
    
    // 基础Kubernetes资源操作
    V1NamespaceList getNamespaces(String cluster);
    V1PodList getPodsInNamespace(String cluster, String namespace);
    V1CustomResourceDefinitionList getCustomResourceDefinitions(String cluster);
    V1CustomResourceDefinition getCustomResourceDefinition(String cluster, String name);
    V1CustomResourceDefinition createCustomResourceDefinition(String cluster, String crdYaml);
    
    // Application资源操作
    List<Application> getApplications(String cluster, String namespace);
    Application getApplication(String cluster, String namespace, String name);
    Application createApplication(String cluster, String namespace, Application application);
    Application updateApplication(String cluster, String namespace, String name, Application application);
    void deleteApplication(String cluster, String namespace, String name);
    
    // Microservice资源操作
    List<Microservice> getMicroservices(String cluster, String namespace);
    Microservice getMicroservice(String cluster, String namespace, String name);
    Microservice createMicroservice(String cluster, String namespace, Microservice microservice);
    Microservice updateMicroservice(String cluster, String namespace, String name, Microservice microservice);
    void deleteMicroservice(String cluster, String namespace, String name);
    
    // GPU资源操作
    List<GPU> getGPUs(String cluster, String namespace);
    GPU getGPU(String cluster, String namespace, String name);
    GPU createGPU(String cluster, String namespace, GPU gpu);
    GPU updateGPU(String cluster, String namespace, String name, GPU gpu);
    void deleteGPU(String cluster, String namespace, String name);
    
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