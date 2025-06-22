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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class KubernetesRepositoryImpl implements KubernetesRepository {

    private final Map<String, CoreV1Api> apiClients;
    private final Map<String, ApiextensionsV1Api> crdApiClients;
    private final Map<String, GenericKubernetesApi<Application, ApplicationList>> applicationApis;
    private final Map<String, GenericKubernetesApi<Microservice, MicroserviceList>> microserviceApis;
    private final Map<String, GenericKubernetesApi<GPU, GPUList>> gpuApis;
    private final ApiClient defaultApiClient;
    private static final String DEFAULT_CLUSTER = "cluster-local";

    @Autowired
    public KubernetesRepositoryImpl(ApiClient apiClient) {
        this.defaultApiClient = apiClient;
        this.apiClients = new ConcurrentHashMap<>();
        this.crdApiClients = new ConcurrentHashMap<>();
        this.applicationApis = new ConcurrentHashMap<>();
        this.microserviceApis = new ConcurrentHashMap<>();
        this.gpuApis = new ConcurrentHashMap<>();
        
        // 初始化默认集群
        initializeCluster(DEFAULT_CLUSTER);
    }

    private void initializeCluster(String clusterName) {
        CoreV1Api api = new CoreV1Api(defaultApiClient);
        ApiextensionsV1Api crdApi = new ApiextensionsV1Api(defaultApiClient);
        
        apiClients.put(clusterName, api);
        crdApiClients.put(clusterName, crdApi);
        
        applicationApis.put(clusterName, new GenericKubernetesApi<>(
                Application.class,
                ApplicationList.class,
                "example.com",
                "v1",
                "applications",
                defaultApiClient
        ));
        
        microserviceApis.put(clusterName, new GenericKubernetesApi<>(
                Microservice.class,
                MicroserviceList.class,
                "example.com",
                "v1",
                "microservices",
                defaultApiClient
        ));
        
        gpuApis.put(clusterName, new GenericKubernetesApi<>(
                GPU.class,
                GPUList.class,
                "example.com",
                "v1",
                "gpus",
                defaultApiClient
        ));
    }

    private String getClusterName(String cluster) {
        return cluster != null && !cluster.isEmpty() ? cluster : DEFAULT_CLUSTER;
    }

    private CoreV1Api getApi(String cluster) {
        String clusterName = getClusterName(cluster);
        if (!apiClients.containsKey(clusterName)) {
            initializeCluster(clusterName);
        }
        return apiClients.get(clusterName);
    }

    private ApiextensionsV1Api getCrdApi(String cluster) {
        String clusterName = getClusterName(cluster);
        if (!crdApiClients.containsKey(clusterName)) {
            initializeCluster(clusterName);
        }
        return crdApiClients.get(clusterName);
    }

    private GenericKubernetesApi<Application, ApplicationList> getApplicationApi(String cluster) {
        String clusterName = getClusterName(cluster);
        if (!applicationApis.containsKey(clusterName)) {
            initializeCluster(clusterName);
        }
        return applicationApis.get(clusterName);
    }

    private GenericKubernetesApi<Microservice, MicroserviceList> getMicroserviceApi(String cluster) {
        String clusterName = getClusterName(cluster);
        if (!microserviceApis.containsKey(clusterName)) {
            initializeCluster(clusterName);
        }
        return microserviceApis.get(clusterName);
    }

    private GenericKubernetesApi<GPU, GPUList> getGPUApi(String cluster) {
        String clusterName = getClusterName(cluster);
        if (!gpuApis.containsKey(clusterName)) {
            initializeCluster(clusterName);
        }
        return gpuApis.get(clusterName);
    }

    @Override
    public V1NamespaceList getNamespaces(String cluster) {
        try {
            CoreV1Api api = getApi(cluster);
            return api.listNamespace().execute();
        } catch (ApiException e) {
            throw new KubernetesException("Failed to get namespaces", getClusterName(cluster), "listNamespaces", e);
        }
    }

    @Override
    public V1PodList getPodsInNamespace(String cluster, String namespace) {
        try {
            CoreV1Api api = getApi(cluster);
            return api.listNamespacedPod(namespace).execute();
        } catch (ApiException e) {
            throw new KubernetesException("Failed to get pods in namespace: " + namespace, 
                                        getClusterName(cluster), "listPods", e);
        }
    }

    @Override
    public boolean isConnected(String cluster) {
        try {
            CoreV1Api api = getApi(cluster);
            api.listNamespace().execute();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    @Override
    public V1CustomResourceDefinitionList getCustomResourceDefinitions(String cluster) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(cluster);
            return crdApi.listCustomResourceDefinition().execute();
        } catch (ApiException e) {
            throw new KubernetesException("Failed to get CRDs", getClusterName(cluster), "listCRDs", e);
        }
    }

    @Override
    public V1CustomResourceDefinition getCustomResourceDefinition(String cluster, String name) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(cluster);
            return crdApi.readCustomResourceDefinition(name).execute();
        } catch (ApiException e) {
            throw new ResourceNotFoundException("CRD", name, getClusterName(cluster));
        }
    }

    @Override
    public V1CustomResourceDefinition createCustomResourceDefinition(String cluster, String crdYaml) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(cluster);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode crdJson = mapper.readTree(crdYaml);
            V1CustomResourceDefinition crd = mapper.treeToValue(crdJson, V1CustomResourceDefinition.class);
            return crdApi.createCustomResourceDefinition(crd).execute();
        } catch (Exception e) {
            throw new KubernetesException("Failed to create CRD", getClusterName(cluster), "createCRD", e);
        }
    }

    @Override
    public List<Application> getApplications(String cluster, String namespace) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.list(namespace).getObject().getItems();
            } else {
                return api.list().getObject().getItems();
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to get applications", getClusterName(cluster), "listApplications", e);
        }
    }

    @Override
    public Application getApplication(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            Application application;
            if (namespace != null && !namespace.isEmpty()) {
                application = api.get(namespace, name).getObject();
            } else {
                application = api.get(name).getObject();
            }
            if (application == null) {
                throw new ResourceNotFoundException("Application", name, namespace, getClusterName(cluster));
            }
            return application;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to get application: " + name, 
                                        getClusterName(cluster), "getApplication", e);
        }
    }

    @Override
    public Application createApplication(String cluster, String namespace, Application application) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.create(namespace, application, new CreateOptions()).getObject();
            } else {
                return api.create(application, new CreateOptions()).getObject();
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to create application", getClusterName(cluster), "createApplication", e);
        }
    }

    @Override
    public Application updateApplication(String cluster, String namespace, String name, Application application) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            return api.update(application, new UpdateOptions()).getObject();
        } catch (Exception e) {
            throw new KubernetesException("Failed to update application: " + name, 
                                        getClusterName(cluster), "updateApplication", e);
        }
    }

    @Override
    public void deleteApplication(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                api.delete(namespace, name);
            } else {
                api.delete(name);
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to delete application: " + name, 
                                        getClusterName(cluster), "deleteApplication", e);
        }
    }

    @Override
    public List<Microservice> getMicroservices(String cluster, String namespace) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.list(namespace).getObject().getItems();
            } else {
                return api.list().getObject().getItems();
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to get microservices", getClusterName(cluster), "listMicroservices", e);
        }
    }

    @Override
    public Microservice getMicroservice(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            Microservice microservice;
            if (namespace != null && !namespace.isEmpty()) {
                microservice = api.get(namespace, name).getObject();
            } else {
                microservice = api.get(name).getObject();
            }
            if (microservice == null) {
                throw new ResourceNotFoundException("Microservice", name, namespace, getClusterName(cluster));
            }
            return microservice;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to get microservice: " + name, 
                                        getClusterName(cluster), "getMicroservice", e);
        }
    }

    @Override
    public Microservice createMicroservice(String cluster, String namespace, Microservice microservice) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.create(namespace, microservice, new CreateOptions()).getObject();
            } else {
                return api.create(microservice, new CreateOptions()).getObject();
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to create microservice", getClusterName(cluster), "createMicroservice", e);
        }
    }

    @Override
    public Microservice updateMicroservice(String cluster, String namespace, String name, Microservice microservice) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            return api.update(microservice, new UpdateOptions()).getObject();
        } catch (Exception e) {
            throw new KubernetesException("Failed to update microservice: " + name, 
                                        getClusterName(cluster), "updateMicroservice", e);
        }
    }

    @Override
    public void deleteMicroservice(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                api.delete(namespace, name);
            } else {
                api.delete(name);
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to delete microservice: " + name, 
                                        getClusterName(cluster), "deleteMicroservice", e);
        }
    }

    @Override
    public List<GPU> getGPUs(String cluster, String namespace) {
        try {
            GenericKubernetesApi<GPU, GPUList> api = getGPUApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.list(namespace).getObject().getItems();
            } else {
                return api.list().getObject().getItems();
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to get GPUs", getClusterName(cluster), "listGPUs", e);
        }
    }

    @Override
    public GPU getGPU(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<GPU, GPUList> api = getGPUApi(cluster);
            GPU gpu;
            if (namespace != null && !namespace.isEmpty()) {
                gpu = api.get(namespace, name).getObject();
            } else {
                gpu = api.get(name).getObject();
            }
            if (gpu == null) {
                throw new ResourceNotFoundException("GPU", name, namespace, getClusterName(cluster));
            }
            return gpu;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to get GPU: " + name, 
                                        getClusterName(cluster), "getGPU", e);
        }
    }

    @Override
    public GPU createGPU(String cluster, String namespace, GPU gpu) {
        try {
            GenericKubernetesApi<GPU, GPUList> api = getGPUApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.create(namespace, gpu, new CreateOptions()).getObject();
            } else {
                return api.create(gpu, new CreateOptions()).getObject();
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to create GPU", getClusterName(cluster), "createGPU", e);
        }
    }

    @Override
    public GPU updateGPU(String cluster, String namespace, String name, GPU gpu) {
        try {
            GenericKubernetesApi<GPU, GPUList> api = getGPUApi(cluster);
            return api.update(gpu, new UpdateOptions()).getObject();
        } catch (Exception e) {
            throw new KubernetesException("Failed to update GPU: " + name, 
                                        getClusterName(cluster), "updateGPU", e);
        }
    }

    @Override
    public void deleteGPU(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<GPU, GPUList> api = getGPUApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                api.delete(namespace, name);
            } else {
                api.delete(name);
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to delete GPU: " + name, 
                                        getClusterName(cluster), "deleteGPU", e);
        }
    }
} 