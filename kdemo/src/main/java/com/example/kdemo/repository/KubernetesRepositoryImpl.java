package com.example.kdemo.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.kdemo.exception.KubernetesException;
import com.example.kdemo.exception.ResourceNotFoundException;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.ApplicationList;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.MicroserviceList;
import com.example.kdemo.model.GPU;
import com.example.kdemo.model.GPUList;
import com.example.kdemo.dto.ResourceQuery;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.ApiextensionsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.options.CreateOptions;
import io.kubernetes.client.util.generic.options.UpdateOptions;
import org.springframework.stereotype.Repository;
import com.example.kdemo.config.KubernetesConfig;
import io.kubernetes.client.util.ClientBuilder;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.kubernetes.client.openapi.models.V1Pod;

@Repository
public class KubernetesRepositoryImpl implements KubernetesRepository {

    private final CoreV1Api coreV1Api;
    private final ApiextensionsV1Api crdApi;
    private final GenericKubernetesApi<Application, ApplicationList> applicationApi;
    private final GenericKubernetesApi<Microservice, MicroserviceList> microserviceApi;
    private final GenericKubernetesApi<GPU, GPUList> gpuApi;
    private final ApiClient defaultApiClient;
    private final KubernetesConfig k8sConfig;
    private static final String DEFAULT_CLUSTER = "cluster-local";

    public KubernetesRepositoryImpl(ApiClient apiClient, KubernetesConfig k8sConfig) {
        this.defaultApiClient = apiClient;
        this.k8sConfig = k8sConfig;
        this.coreV1Api = new CoreV1Api(apiClient);
        this.crdApi = new ApiextensionsV1Api(apiClient);
        this.applicationApi = new GenericKubernetesApi<>(
            Application.class, ApplicationList.class, "example.com", "v1", "applications", apiClient);
        this.microserviceApi = new GenericKubernetesApi<>(
            Microservice.class, MicroserviceList.class, "example.com", "v1", "microservices", apiClient);
        this.gpuApi = new GenericKubernetesApi<>(
            GPU.class, GPUList.class, "example.com", "v1", "gpus", apiClient);
    }

    @Override
    public V1NamespaceList getNamespaces() {
        try {
            return coreV1Api.listNamespace().execute();
        } catch (ApiException e) {
            throw new KubernetesException("Failed to get namespaces", DEFAULT_CLUSTER, "listNamespaces", e);
        }
    }

    @Override
    public V1PodList getPodsInNamespace() {
        try {
            return coreV1Api.listNamespacedPod("default").execute();
        } catch (ApiException e) {
            throw new KubernetesException("Failed to get pods in namespace: default", DEFAULT_CLUSTER, "listPods", e);
        }
    }

    @Override
    public boolean isConnected() {
        try {
            coreV1Api.listNamespace().execute();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    @Override
    public V1CustomResourceDefinitionList getCustomResourceDefinitions() {
        try {
            return crdApi.listCustomResourceDefinition().execute();
        } catch (ApiException e) {
            throw new KubernetesException("Failed to get CRDs", DEFAULT_CLUSTER, "listCRDs", e);
        }
    }

    @Override
    public V1CustomResourceDefinition getCustomResourceDefinition(String name) {
        try {
            return crdApi.readCustomResourceDefinition(name).execute();
        } catch (ApiException e) {
            throw new ResourceNotFoundException("CRD", name, DEFAULT_CLUSTER);
        }
    }

    @Override
    public V1CustomResourceDefinition createCustomResourceDefinition(String crdYaml) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode crdJson = mapper.readTree(crdYaml);
            V1CustomResourceDefinition crd = mapper.treeToValue(crdJson, V1CustomResourceDefinition.class);
            return crdApi.createCustomResourceDefinition(crd).execute();
        } catch (Exception e) {
            throw new KubernetesException("Failed to create CRD", DEFAULT_CLUSTER, "createCRD", e);
        }
    }

    @Override
    public List<Application> getApplications() {
        try {
            return applicationApi.list().getObject().getItems();
        } catch (Exception e) {
            throw new KubernetesException("Failed to get applications", DEFAULT_CLUSTER, "listApplications", e);
        }
    }

    @Override
    public Application getApplication(String name) {
        try {
            Application application = applicationApi.get(name).getObject();
            if (application == null) {
                throw new ResourceNotFoundException("Application", name, DEFAULT_CLUSTER);
            }
            return application;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to get application: " + name, DEFAULT_CLUSTER, "getApplication", e);
        }
    }

    @Override
    public Application createApplication(Application application) {
        try {
            return applicationApi.create(application, new CreateOptions()).getObject();
        } catch (Exception e) {
            throw new KubernetesException("Failed to create application", DEFAULT_CLUSTER, "createApplication", e);
        }
    }

    @Override
    public Application updateApplication(String name, Application application) {
        try {
            return applicationApi.update(application, new UpdateOptions()).getObject();
        } catch (Exception e) {
            throw new KubernetesException("Failed to update application: " + name, DEFAULT_CLUSTER, "updateApplication", e);
        }
    }

    @Override
    public void deleteApplication(String name) {
        try {
            applicationApi.delete(name);
        } catch (Exception e) {
            throw new KubernetesException("Failed to delete application: " + name, DEFAULT_CLUSTER, "deleteApplication", e);
        }
    }

    @Override
    public List<Microservice> getMicroservices() {
        try {
            return microserviceApi.list().getObject().getItems();
        } catch (Exception e) {
            throw new KubernetesException("Failed to get microservices", DEFAULT_CLUSTER, "listMicroservices", e);
        }
    }

    @Override
    public Microservice getMicroservice(String name) {
        try {
            Microservice microservice = microserviceApi.get(name).getObject();
            if (microservice == null) {
                throw new ResourceNotFoundException("Microservice", name, DEFAULT_CLUSTER);
            }
            return microservice;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to get microservice: " + name, DEFAULT_CLUSTER, "getMicroservice", e);
        }
    }

    @Override
    public Microservice createMicroservice(Microservice microservice) {
        try {
            return microserviceApi.create(microservice, new CreateOptions()).getObject();
        } catch (Exception e) {
            throw new KubernetesException("Failed to create microservice", DEFAULT_CLUSTER, "createMicroservice", e);
        }
    }

    @Override
    public Microservice updateMicroservice(String name, Microservice microservice) {
        try {
            return microserviceApi.update(microservice, new UpdateOptions()).getObject();
        } catch (Exception e) {
            throw new KubernetesException("Failed to update microservice: " + name, DEFAULT_CLUSTER, "updateMicroservice", e);
        }
    }

    @Override
    public void deleteMicroservice(String name) {
        try {
            microserviceApi.delete(name);
        } catch (Exception e) {
            throw new KubernetesException("Failed to delete microservice: " + name, DEFAULT_CLUSTER, "deleteMicroservice", e);
        }
    }

    @Override
    public List<GPU> getGPUs() {
        try {
            return gpuApi.list().getObject().getItems();
        } catch (Exception e) {
            throw new KubernetesException("Failed to get GPUs", DEFAULT_CLUSTER, "listGPUs", e);
        }
    }

    @Override
    public GPU getGPU(String name) {
        try {
            GPU gpu = gpuApi.get(name).getObject();
            if (gpu == null) {
                throw new ResourceNotFoundException("GPU", name, DEFAULT_CLUSTER);
            }
            return gpu;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to get GPU: " + name, DEFAULT_CLUSTER, "getGPU", e);
        }
    }

    @Override
    public GPU createGPU(GPU gpu) {
        try {
            return gpuApi.create(gpu, new CreateOptions()).getObject();
        } catch (Exception e) {
            throw new KubernetesException("Failed to create GPU", DEFAULT_CLUSTER, "createGPU", e);
        }
    }

    @Override
    public GPU updateGPU(String name, GPU gpu) {
        try {
            if (gpu == null) {
                throw new IllegalArgumentException("GPU object cannot be null");
            }
            
            GPU updated = gpuApi.update(gpu, new UpdateOptions()).getObject();
            
            if (updated == null) {
                throw new KubernetesException("Failed to update GPU: " + name, DEFAULT_CLUSTER, "updateGPU", new Exception("Update returned null"));
            }
            
            return updated;
        } catch (Exception e) {
            throw new KubernetesException("Failed to update GPU: " + name, DEFAULT_CLUSTER, "updateGPU", e);
        }
    }

    @Override
    public void deleteGPU(String name) {
        try {
            gpuApi.delete(name);
        } catch (Exception e) {
            throw new KubernetesException("Failed to delete GPU: " + name, DEFAULT_CLUSTER, "deleteGPU", e);
        }
    }

    @Override
    public List<V1Pod> getPods(ResourceQuery query) {
        String ns = query.getNamespace() != null ? query.getNamespace() : "default";
        String fieldSelector = query.getFieldSelector();
        String labelSelector = query.getLabelSelector();
        Integer limit = query.getLimit();
        try {
            var req = coreV1Api.listNamespacedPod(ns);
            if (fieldSelector != null && !fieldSelector.isEmpty()) req = req.fieldSelector(fieldSelector);
            if (labelSelector != null && !labelSelector.isEmpty()) req = req.labelSelector(labelSelector);
            if (limit != null) req = req.limit(limit);
            V1PodList podList = req.execute();
            return podList.getItems();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list pods: " + e.getMessage(), e);
        }
    }
} 