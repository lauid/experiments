package com.example.kdemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.ApiextensionsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.options.CreateOptions;
import io.kubernetes.client.util.generic.options.UpdateOptions;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.ApplicationList;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.MicroserviceList;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KubernetesService {

    private final Map<String, CoreV1Api> apiClients;
    private final Map<String, ApiextensionsV1Api> crdApiClients;
    private final Map<String, GenericKubernetesApi<Application, ApplicationList>> applicationApis;
    private final Map<String, GenericKubernetesApi<Microservice, MicroserviceList>> microserviceApis;
    private static final String DEFAULT_CLUSTER = "cluster-local";

    public KubernetesService() {
        this.apiClients = new ConcurrentHashMap<>();
        this.crdApiClients = new ConcurrentHashMap<>();
        this.applicationApis = new ConcurrentHashMap<>();
        this.microserviceApis = new ConcurrentHashMap<>();
        
        // 初始化默认集群
        initializeCluster(DEFAULT_CLUSTER);
    }

    private void initializeCluster(String clusterName) {
        try {
            ApiClient apiClient = ClientBuilder.standard().build();
            Configuration.setDefaultApiClient(apiClient);
            
            CoreV1Api api = new CoreV1Api();
            ApiextensionsV1Api crdApi = new ApiextensionsV1Api();
            
            apiClients.put(clusterName, api);
            crdApiClients.put(clusterName, crdApi);
            
            applicationApis.put(clusterName, new GenericKubernetesApi<>(
                    Application.class,
                    ApplicationList.class,
                    "example.com",
                    "v1",
                    "applications",
                    apiClient
            ));
            
            microserviceApis.put(clusterName, new GenericKubernetesApi<>(
                    Microservice.class,
                    MicroserviceList.class,
                    "example.com",
                    "v1",
                    "microservices",
                    apiClient
            ));
        } catch (Exception e) {
            try {
                ApiClient apiClient = ClientBuilder.cluster().build();
                Configuration.setDefaultApiClient(apiClient);
                
                CoreV1Api api = new CoreV1Api();
                ApiextensionsV1Api crdApi = new ApiextensionsV1Api();
                
                apiClients.put(clusterName, api);
                crdApiClients.put(clusterName, crdApi);
                
                applicationApis.put(clusterName, new GenericKubernetesApi<>(
                        Application.class,
                        ApplicationList.class,
                        "example.com",
                        "v1",
                        "applications",
                        apiClient
                ));
                
                microserviceApis.put(clusterName, new GenericKubernetesApi<>(
                        Microservice.class,
                        MicroserviceList.class,
                        "example.com",
                        "v1",
                        "microservices",
                        apiClient
                ));
            } catch (IOException ioException) {
                ApiClient apiClient = new ApiClient();
                
                CoreV1Api api = new CoreV1Api();
                ApiextensionsV1Api crdApi = new ApiextensionsV1Api();
                
                apiClients.put(clusterName, api);
                crdApiClients.put(clusterName, crdApi);
                
                applicationApis.put(clusterName, new GenericKubernetesApi<>(
                        Application.class,
                        ApplicationList.class,
                        "example.com",
                        "v1",
                        "applications",
                        apiClient
                ));
                
                microserviceApis.put(clusterName, new GenericKubernetesApi<>(
                        Microservice.class,
                        MicroserviceList.class,
                        "example.com",
                        "v1",
                        "microservices",
                        apiClient
                ));
            }
        }
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

    /**
     * 获取所有命名空间
     */
    public List<String> getNamespaces(String cluster) {
        try {
            CoreV1Api api = getApi(cluster);
            V1NamespaceList namespaceList = api.listNamespace().execute();
            return namespaceList.getItems().stream()
                    .map(namespace -> namespace.getMetadata().getName())
                    .toList();
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get namespaces from cluster: " + getClusterName(cluster), e);
        }
    }

    /**
     * 获取指定命名空间中的所有 Pod
     */
    public List<String> getPodsInNamespace(String cluster, String namespace) {
        try {
            CoreV1Api api = getApi(cluster);
            V1PodList podList = api.listNamespacedPod(namespace).execute();
            return podList.getItems().stream()
                    .map(pod -> pod.getMetadata().getName())
                    .toList();
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get pods in namespace: " + namespace + " from cluster: " + getClusterName(cluster), e);
        }
    }

    /**
     * 检查 Kubernetes 连接
     */
    public boolean isConnected(String cluster) {
        try {
            CoreV1Api api = getApi(cluster);
            api.listNamespace().execute();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    /**
     * 获取所有CRD
     */
    public List<String> getCustomResourceDefinitions(String cluster) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(cluster);
            V1CustomResourceDefinitionList crdList = crdApi.listCustomResourceDefinition().execute();
            return crdList.getItems().stream()
                    .map(crd -> crd.getMetadata().getName())
                    .toList();
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get CRDs from cluster: " + getClusterName(cluster), e);
        }
    }

    /**
     * 获取指定CRD的详细信息
     */
    public Map<String, Object> getCustomResourceDefinition(String cluster, String name) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(cluster);
            V1CustomResourceDefinition crd = crdApi.readCustomResourceDefinition(name).execute();
            return Map.of(
                    "name", crd.getMetadata().getName(),
                    "group", crd.getSpec().getGroup(),
                    "version", crd.getSpec().getVersions().get(0).getName(),
                    "kind", crd.getSpec().getNames().getKind(),
                    "plural", crd.getSpec().getNames().getPlural(),
                    "singular", crd.getSpec().getNames().getSingular(),
                    "scope", crd.getSpec().getScope()
            );
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get CRD: " + name + " from cluster: " + getClusterName(cluster), e);
        }
    }

    /**
     * 创建CRD
     */
    public Map<String, Object> createCustomResourceDefinition(String cluster, String crdYaml) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(cluster);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode crdJson = mapper.readTree(crdYaml);
            
            V1CustomResourceDefinition crd = mapper.treeToValue(crdJson, V1CustomResourceDefinition.class);
            V1CustomResourceDefinition createdCrd = crdApi.createCustomResourceDefinition(crd).execute();
            
            return Map.of(
                    "success", true,
                    "name", createdCrd.getMetadata().getName(),
                    "cluster", getClusterName(cluster),
                    "message", "CRD created successfully"
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Failed to create CRD",
                    "cluster", getClusterName(cluster),
                    "message", e.getMessage()
            );
        }
    }

    // ========== 类型安全的 Application 资源操作 ==========

    /**
     * 获取所有 Application 资源
     */
    public List<Application> getApplications(String cluster, String namespace) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.list(namespace).getObject().getItems();
            } else {
                return api.list().getObject().getItems();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get applications from cluster: " + getClusterName(cluster), e);
        }
    }

    /**
     * 获取指定的 Application 资源
     */
    public Application getApplication(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.get(namespace, name).getObject();
            } else {
                return api.get(name).getObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get application: " + name + " from cluster: " + getClusterName(cluster), e);
        }
    }

    /**
     * 创建 Application 资源
     */
    public Map<String, Object> createApplication(String cluster, String namespace, String resourceYaml) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            ObjectMapper mapper = new ObjectMapper();
            Application application = mapper.readValue(resourceYaml, Application.class);
            
            Application created;
            if (namespace != null && !namespace.isEmpty()) {
                created = api.create(namespace, application, new CreateOptions()).getObject();
            } else {
                created = api.create(application, new CreateOptions()).getObject();
            }
            
            return Map.of(
                    "success", true,
                    "name", created.getMetadata().getName(),
                    "cluster", getClusterName(cluster),
                    "message", "Application created successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Failed to create application",
                    "cluster", getClusterName(cluster),
                    "message", e.getMessage()
            );
        }
    }

    /**
     * 更新 Application 资源
     */
    public Map<String, Object> updateApplication(String cluster, String namespace, String name, String resourceYaml) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            ObjectMapper mapper = new ObjectMapper();
            Application application = mapper.readValue(resourceYaml, Application.class);
            
            Application updated = api.update(application, new UpdateOptions()).getObject();
            
            return Map.of(
                    "success", true,
                    "name", updated.getMetadata().getName(),
                    "cluster", getClusterName(cluster),
                    "message", "Application updated successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Failed to update application",
                    "cluster", getClusterName(cluster),
                    "message", e.getMessage()
            );
        }
    }

    /**
     * 删除 Application 资源
     */
    public Map<String, Object> deleteApplication(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<Application, ApplicationList> api = getApplicationApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                api.delete(namespace, name);
            } else {
                api.delete(name);
            }
            
            return Map.of(
                    "success", true,
                    "name", name,
                    "cluster", getClusterName(cluster),
                    "message", "Application deleted successfully"
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Failed to delete application",
                    "cluster", getClusterName(cluster),
                    "message", e.getMessage()
            );
        }
    }

    // ========== Microservice 资源操作 ==========

    public List<Microservice> getMicroservices(String cluster, String namespace) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.list(namespace).getObject().getItems();
            } else {
                return api.list().getObject().getItems();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get microservices from cluster: " + getClusterName(cluster), e);
        }
    }

    public Microservice getMicroservice(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                return api.get(namespace, name).getObject();
            } else {
                return api.get(name).getObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get microservice: " + name + " from cluster: " + getClusterName(cluster), e);
        }
    }

    public Map<String, Object> createMicroservice(String cluster, String namespace, String resourceYaml) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            ObjectMapper mapper = new ObjectMapper();
            Microservice microservice = mapper.readValue(resourceYaml, Microservice.class);
            
            Microservice created;
            if (namespace != null && !namespace.isEmpty()) {
                created = api.create(namespace, microservice, new CreateOptions()).getObject();
            } else {
                created = api.create(microservice, new CreateOptions()).getObject();
            }
            
            return Map.of(
                    "success", true,
                    "name", created.getMetadata().getName(),
                    "cluster", getClusterName(cluster),
                    "message", "Microservice created successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Failed to create microservice",
                    "cluster", getClusterName(cluster),
                    "message", e.getMessage()
            );
        }
    }

    public Map<String, Object> updateMicroservice(String cluster, String namespace, String name, String resourceYaml) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            ObjectMapper mapper = new ObjectMapper();
            Microservice microservice = mapper.readValue(resourceYaml, Microservice.class);
            
            Microservice updated = api.update(microservice, new UpdateOptions()).getObject();
            
            return Map.of(
                    "success", true,
                    "name", updated.getMetadata().getName(),
                    "cluster", getClusterName(cluster),
                    "message", "Microservice updated successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Failed to update microservice",
                    "cluster", getClusterName(cluster),
                    "message", e.getMessage()
            );
        }
    }

    public Map<String, Object> deleteMicroservice(String cluster, String namespace, String name) {
        try {
            GenericKubernetesApi<Microservice, MicroserviceList> api = getMicroserviceApi(cluster);
            if (namespace != null && !namespace.isEmpty()) {
                api.delete(namespace, name);
            } else {
                api.delete(name);
            }
            
            return Map.of(
                    "success", true,
                    "name", name,
                    "cluster", getClusterName(cluster),
                    "message", "Microservice deleted successfully"
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Failed to delete microservice",
                    "cluster", getClusterName(cluster),
                    "message", e.getMessage()
            );
        }
    }
} 