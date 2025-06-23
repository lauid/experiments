package com.example.kdemo.repository;

import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import com.example.kdemo.dto.ResourceQuery;

import java.util.List;

public interface KubernetesRepository {
    
    // 基础Kubernetes资源操作
    V1NamespaceList getNamespaces();
    V1PodList getPodsInNamespace();
    V1CustomResourceDefinitionList getCustomResourceDefinitions();
    V1CustomResourceDefinition getCustomResourceDefinition(String name);
    V1CustomResourceDefinition createCustomResourceDefinition(String crdYaml);
    
    // Application资源操作
    List<Application> getApplications();
    Application getApplication(String name);
    Application createApplication(Application application);
    Application updateApplication(String name, Application application);
    void deleteApplication(String name);
    
    // Microservice资源操作
    List<Microservice> getMicroservices();
    Microservice getMicroservice(String name);
    Microservice createMicroservice(Microservice microservice);
    Microservice updateMicroservice(String name, Microservice microservice);
    void deleteMicroservice(String name);
    
    // GPU资源操作
    List<GPU> getGPUs();
    GPU getGPU(String name);
    GPU createGPU(GPU gpu);
    GPU updateGPU(String name, GPU gpu);
    void deleteGPU(String name);
    
    // 连接检查
    boolean isConnected();

    // 新增：支持复杂查询参数的 Pod 查询
    List<io.kubernetes.client.openapi.models.V1Pod> getPods(ResourceQuery query);
} 