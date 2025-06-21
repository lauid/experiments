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

@Service
public class KubernetesService {

    private final CoreV1Api api;
    private final ApiextensionsV1Api crdApi;
    private final ApiClient apiClient;
    private final GenericKubernetesApi<Application, ApplicationList> applicationApi;
    private final GenericKubernetesApi<Microservice, MicroserviceList> microserviceApi;

    public KubernetesService() {
        CoreV1Api tempApi;
        ApiextensionsV1Api tempCrdApi;
        ApiClient tempApiClient;
        
        try {
            tempApiClient = ClientBuilder.standard().build();
            Configuration.setDefaultApiClient(tempApiClient);
            tempApi = new CoreV1Api();
            tempCrdApi = new ApiextensionsV1Api();
        } catch (Exception e) {
            try {
                tempApiClient = ClientBuilder.cluster().build();
                Configuration.setDefaultApiClient(tempApiClient);
                tempApi = new CoreV1Api();
                tempCrdApi = new ApiextensionsV1Api();
            } catch (IOException ioException) {
                tempApiClient = new ApiClient();
                tempApi = new CoreV1Api();
                tempCrdApi = new ApiextensionsV1Api();
            }
        }
        this.api = tempApi;
        this.crdApi = tempCrdApi;
        this.apiClient = tempApiClient;
        
        this.applicationApi = new GenericKubernetesApi<>(
                Application.class,
                ApplicationList.class,
                "example.com",
                "v1",
                "applications",
                tempApiClient
        );
        
        this.microserviceApi = new GenericKubernetesApi<>(
                Microservice.class,
                MicroserviceList.class,
                "example.com",
                "v1",
                "microservices",
                tempApiClient
        );
    }

    /**
     * 获取所有命名空间
     */
    public List<String> getNamespaces() {
        try {
            V1NamespaceList namespaceList = api.listNamespace().execute();
            return namespaceList.getItems().stream()
                    .map(namespace -> namespace.getMetadata().getName())
                    .toList();
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get namespaces", e);
        }
    }

    /**
     * 获取指定命名空间中的所有 Pod
     */
    public List<String> getPodsInNamespace(String namespace) {
        try {
            V1PodList podList = api.listNamespacedPod(namespace).execute();
            return podList.getItems().stream()
                    .map(pod -> pod.getMetadata().getName())
                    .toList();
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get pods in namespace: " + namespace, e);
        }
    }

    /**
     * 检查 Kubernetes 连接
     */
    public boolean isConnected() {
        try {
            api.listNamespace().execute();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    /**
     * 获取所有CRD
     */
    public List<String> getCustomResourceDefinitions() {
        try {
            V1CustomResourceDefinitionList crdList = crdApi.listCustomResourceDefinition().execute();
            return crdList.getItems().stream()
                    .map(crd -> crd.getMetadata().getName())
                    .toList();
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get CRDs", e);
        }
    }

    /**
     * 获取指定CRD的详细信息
     */
    public Map<String, Object> getCustomResourceDefinition(String name) {
        try {
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
            throw new RuntimeException("Failed to get CRD: " + name, e);
        }
    }

    /**
     * 创建CRD
     */
    public Map<String, Object> createCustomResourceDefinition(String crdYaml) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode crdJson = mapper.readTree(crdYaml);
            
            // 从JSON创建CRD对象
            V1CustomResourceDefinition crd = mapper.treeToValue(crdJson, V1CustomResourceDefinition.class);
            
            V1CustomResourceDefinition createdCrd = crdApi.createCustomResourceDefinition(crd).execute();
            
            return Map.of(
                    "success", true,
                    "name", createdCrd.getMetadata().getName(),
                    "message", "CRD created successfully"
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Failed to create CRD",
                    "message", e.getMessage()
            );
        }
    }

    // ========== 类型安全的 Application 资源操作 ==========

    /**
     * 获取所有 Application 资源
     */
    public List<Application> getApplications(String namespace) {
        try {
            if (namespace != null && !namespace.isEmpty()) {
                return applicationApi.list(namespace).getObject().getItems();
            } else {
                return applicationApi.list().getObject().getItems();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get applications", e);
        }
    }

    /**
     * 获取指定的 Application 资源
     */
    public Application getApplication(String namespace, String name) {
        try {
            if (namespace != null && !namespace.isEmpty()) {
                return applicationApi.get(namespace, name).getObject();
            } else {
                return applicationApi.get(name).getObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get application: " + name, e);
        }
    }

    /**
     * 创建 Application 资源
     */
    public Map<String, Object> createApplication(String namespace, String resourceYaml) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Application application = mapper.readValue(resourceYaml, Application.class);
            
            Application created;
            if (namespace != null && !namespace.isEmpty()) {
                created = applicationApi.create(namespace, application, new CreateOptions()).getObject();
            } else {
                created = applicationApi.create(application, new CreateOptions()).getObject();
            }
            
            return Map.of(
                    "success", true,
                    "name", created.getMetadata().getName(),
                    "message", "Application created successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Failed to create application",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * 更新 Application 资源
     */
    public Map<String, Object> updateApplication(String namespace, String name, String resourceYaml) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Application application = mapper.readValue(resourceYaml, Application.class);
            
            Application updated = applicationApi.update(application, new UpdateOptions()).getObject();
            
            return Map.of(
                    "success", true,
                    "name", updated.getMetadata().getName(),
                    "message", "Application updated successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Failed to update application",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * 删除 Application 资源
     */
    public Map<String, Object> deleteApplication(String namespace, String name) {
        try {
            if (namespace != null && !namespace.isEmpty()) {
                applicationApi.delete(namespace, name);
            } else {
                applicationApi.delete(name);
            }
            
            return Map.of(
                    "success", true,
                    "name", name,
                    "message", "Application deleted successfully"
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Failed to delete application",
                    "message", e.getMessage()
            );
        }
    }

    // ========== Microservice 资源操作 ==========

    public List<Microservice> getMicroservices(String namespace) {
        try {
            if (namespace != null && !namespace.isEmpty()) {
                return microserviceApi.list(namespace).getObject().getItems();
            } else {
                return microserviceApi.list().getObject().getItems();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get microservices", e);
        }
    }

    public Microservice getMicroservice(String namespace, String name) {
        try {
            if (namespace != null && !namespace.isEmpty()) {
                return microserviceApi.get(namespace, name).getObject();
            } else {
                return microserviceApi.get(name).getObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get microservice: " + name, e);
        }
    }

    public Map<String, Object> createMicroservice(String namespace, String resourceYaml) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Microservice microservice = mapper.readValue(resourceYaml, Microservice.class);
            
            Microservice created;
            if (namespace != null && !namespace.isEmpty()) {
                created = microserviceApi.create(namespace, microservice, new CreateOptions()).getObject();
            } else {
                created = microserviceApi.create(microservice, new CreateOptions()).getObject();
            }
            
            return Map.of(
                    "success", true,
                    "name", created.getMetadata().getName(),
                    "message", "Microservice created successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Failed to create microservice",
                    "message", e.getMessage()
            );
        }
    }

    public Map<String, Object> updateMicroservice(String namespace, String name, String resourceYaml) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Microservice microservice = mapper.readValue(resourceYaml, Microservice.class);
            
            Microservice updated = microserviceApi.update(microservice, new UpdateOptions()).getObject();
            
            return Map.of(
                    "success", true,
                    "name", updated.getMetadata().getName(),
                    "message", "Microservice updated successfully"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "error", "Failed to update microservice",
                    "message", e.getMessage()
            );
        }
    }

    public Map<String, Object> deleteMicroservice(String namespace, String name) {
        try {
            if (namespace != null && !namespace.isEmpty()) {
                microserviceApi.delete(namespace, name);
            } else {
                microserviceApi.delete(name);
            }
            
            return Map.of(
                    "success", true,
                    "name", name,
                    "message", "Microservice deleted successfully"
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "error", "Failed to delete microservice",
                    "message", e.getMessage()
            );
        }
    }
} 