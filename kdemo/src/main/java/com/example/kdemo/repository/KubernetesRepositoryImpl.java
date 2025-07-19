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
import io.kubernetes.client.openapi.apis.CoreV1Api.APIpatchNodeRequest;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.options.CreateOptions;
import io.kubernetes.client.util.generic.options.UpdateOptions;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.models.V1Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.flipkart.zjsonpatch.JsonDiff;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.util.PatchUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import io.kubernetes.client.openapi.models.V1Status;

@Repository
public class KubernetesRepositoryImpl implements KubernetesRepository {

    // 通用 API 缓存，key: apiClient+kind
    private final Map<String, GenericKubernetesApi<?, ?>> genericApis = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public KubernetesRepositoryImpl() {}

    private CoreV1Api getApi(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    // 通用 API 缓存，key: apiClient+kind
    @SuppressWarnings("unchecked")
    private <T extends KubernetesObject, L extends KubernetesListObject> GenericKubernetesApi<T, L> getGenericApi(ApiClient apiClient, Class<T> resourceClass, Class<L> listClass, String plural) {
        String key = apiClient.hashCode() + ":" + resourceClass.getSimpleName();
        return (GenericKubernetesApi<T, L>) genericApis.computeIfAbsent(key, k -> new GenericKubernetesApi<>(
            resourceClass, listClass, "example.com", "v1", plural, apiClient));
    }

    /**
     * 统一处理KubernetesApiResponse，抛出带code的KubernetesException（支持资源和列表对象）
     * errorCode优先取response.getStatus().getReason()，否则用http code
     */
    private <T extends io.kubernetes.client.common.KubernetesType> T handleApiResponse(KubernetesApiResponse<T> response, String action, String resourceType, String name) {
        if (!response.isSuccess()) {
            int code = response.getHttpStatusCode();
            String reason = null;
            if (response.getStatus() != null && response.getStatus().getReason() != null && !response.getStatus().getReason().isEmpty()) {
                reason = response.getStatus().getReason();
            }
            String errorCode = reason != null ? reason : String.valueOf(code);
            String msg = "Failed to " + action + " " + resourceType + (name != null ? (": " + name) : "");
            if (code == 404) {
                throw new KubernetesException(msg, errorCode, action, null);
            } else {
                throw new KubernetesException(msg, errorCode, action, null);
            }
        }
        return response.getObject();
    }

    // 通用 list
    @SuppressWarnings("unchecked")
    private <T extends KubernetesObject, L extends KubernetesListObject> List<T> listResources(ApiClient apiClient, String namespace, Class<T> resourceClass, Class<L> listClass, String plural) {
        try {
            GenericKubernetesApi<T, L> api = getGenericApi(apiClient, resourceClass, listClass, plural);
            if (namespace != null && !namespace.isEmpty()) {
                return ((List<T>) handleApiResponse(api.list(namespace), "list", resourceClass.getSimpleName(), null).getItems());
            } else {
                return ((List<T>) handleApiResponse(api.list(), "list", resourceClass.getSimpleName(), null).getItems());
            }
        } catch (Exception e) {
            throw new KubernetesException("Failed to list resources: " + resourceClass.getSimpleName(), "", "listResources", e);
        }
    }

    // 通用 get
    private <T extends KubernetesObject, L extends KubernetesListObject> T getResource(ApiClient apiClient, String namespace, String name, Class<T> resourceClass, Class<L> listClass, String plural) {
        try {
            GenericKubernetesApi<T, L> api = getGenericApi(apiClient, resourceClass, listClass, plural);
            io.kubernetes.client.util.generic.KubernetesApiResponse<T> response;
            if (namespace != null && !namespace.isEmpty()) {
                response = api.get(namespace, name);
            } else {
                response = api.get(name);
            }
            return handleApiResponse(response, "get", resourceClass.getSimpleName(), name);
        } catch (KubernetesException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to get resource: " + resourceClass.getSimpleName() + ": " + name, "", "getResource", e);
        }
    }

    // 通用 create
    private <T extends KubernetesObject, L extends KubernetesListObject> T createResource(ApiClient apiClient, String namespace, T obj, Class<T> resourceClass, Class<L> listClass, String plural) {
        try {
            GenericKubernetesApi<T, L> api = getGenericApi(apiClient, resourceClass, listClass, plural);
            io.kubernetes.client.util.generic.KubernetesApiResponse<T> response;
            if (namespace != null && !namespace.isEmpty()) {
                response = api.create(namespace, obj, new CreateOptions());
            } else {
                response = api.create(obj, new CreateOptions());
            }
            return handleApiResponse(response, "create", resourceClass.getSimpleName(), null);
        } catch (KubernetesException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to create resource: " + resourceClass.getSimpleName(), "", "createResource", e);
        }
    }

    // 通用 update
    private <T extends KubernetesObject, L extends KubernetesListObject> T updateResource(ApiClient apiClient, String namespace, String name, T obj, Class<T> resourceClass, Class<L> listClass, String plural) {
        try {
            GenericKubernetesApi<T, L> api = getGenericApi(apiClient, resourceClass, listClass, plural);
            KubernetesApiResponse<T> response = api.update(obj, new UpdateOptions());
            return handleApiResponse(response, "update", resourceClass.getSimpleName(), name);
        } catch (KubernetesException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to update resource: " + resourceClass.getSimpleName() + ": " + name, "", "updateResource", e);
        }
    }

    // 通用 delete
    private <T extends KubernetesObject, L extends KubernetesListObject> void deleteResource(ApiClient apiClient, String namespace, String name, Class<T> resourceClass, Class<L> listClass, String plural) {
        try {
            GenericKubernetesApi<T, L> api = getGenericApi(apiClient, resourceClass, listClass, plural);
            KubernetesApiResponse<T> response;
            if (namespace != null && !namespace.isEmpty()) {
                response = api.delete(namespace, name);
            } else {
                response = api.delete(name);
            }
            handleApiResponse(response, "delete", resourceClass.getSimpleName(), name);
        } catch (KubernetesException e) {
            throw e;
        } catch (Exception e) {
            throw new KubernetesException("Failed to delete resource: " + resourceClass.getSimpleName() + ": " + name, "", "deleteResource", e);
        }
    }

    // Application
    @Override
    public List<Application> getApplications(ApiClient apiClient, String namespace) {
        return listResources(apiClient, namespace, Application.class, ApplicationList.class, "applications");
    }
    @Override
    public Application getApplication(ApiClient apiClient, String namespace, String name) {
        return getResource(apiClient, namespace, name, Application.class, ApplicationList.class, "applications");
    }
    @Override
    public Application createApplication(ApiClient apiClient, String namespace, Application application) {
        return createResource(apiClient, namespace, application, Application.class, ApplicationList.class, "applications");
    }
    @Override
    public Application updateApplication(ApiClient apiClient, String namespace, String name, Application application) {
        return updateResource(apiClient, namespace, name, application, Application.class, ApplicationList.class, "applications");
    }
    @Override
    public void deleteApplication(ApiClient apiClient, String namespace, String name) {
        deleteResource(apiClient, namespace, name, Application.class, ApplicationList.class, "applications");
    }
    // Microservice
    @Override
    public List<Microservice> getMicroservices(ApiClient apiClient, String namespace) {
        return listResources(apiClient, namespace, Microservice.class, MicroserviceList.class, "microservices");
    }
    @Override
    public Microservice getMicroservice(ApiClient apiClient, String namespace, String name) {
        return getResource(apiClient, namespace, name, Microservice.class, MicroserviceList.class, "microservices");
    }
    @Override
    public Microservice createMicroservice(ApiClient apiClient, String namespace, Microservice microservice) {
        return createResource(apiClient, namespace, microservice, Microservice.class, MicroserviceList.class, "microservices");
    }
    @Override
    public Microservice updateMicroservice(ApiClient apiClient, String namespace, String name, Microservice microservice) {
        return updateResource(apiClient, namespace, name, microservice, Microservice.class, MicroserviceList.class, "microservices");
    }
    @Override
    public void deleteMicroservice(ApiClient apiClient, String namespace, String name) {
        deleteResource(apiClient, namespace, name, Microservice.class, MicroserviceList.class, "microservices");
    }
    // GPU
    @Override
    public List<GPU> getGPUs(ApiClient apiClient, String namespace) {
        return listResources(apiClient, namespace, GPU.class, GPUList.class, "gpus");
    }
    @Override
    public GPU getGPU(ApiClient apiClient, String namespace, String name) {
        return getResource(apiClient, namespace, name, GPU.class, GPUList.class, "gpus");
    }
    @Override
    public GPU createGPU(ApiClient apiClient, String namespace, GPU gpu) {
        return createResource(apiClient, namespace, gpu, GPU.class, GPUList.class, "gpus");
    }
    @Override
    public GPU updateGPU(ApiClient apiClient, String namespace, String name, GPU gpu) {
        return updateResource(apiClient, namespace, name, gpu, GPU.class, GPUList.class, "gpus");
    }
    @Override
    public void deleteGPU(ApiClient apiClient, String namespace, String name) {
        deleteResource(apiClient, namespace, name, GPU.class, GPUList.class, "gpus");
    }

    @Override
    public V1NamespaceList getNamespaces(ApiClient apiClient) {
        try {
            CoreV1Api api = getApi(apiClient);
            return api.listNamespace().execute();
        } catch (ApiException e) {
            V1Status status = KubernetesException.parseStatusFromApiException(e);
            if (status != null) {
                throw new KubernetesException("Failed to get namespaces", null, status);
            } else {
                String errorDetail = "ApiException: code=" + e.getCode() + ", body=" + e.getResponseBody();
                throw new KubernetesException("Failed to get namespaces [" + errorDetail + "]", null, "listNamespaces", e);
            }
        }
    }

    @Override
    public V1PodList getPodsInNamespace(ApiClient apiClient, String namespace) {
        try {
            CoreV1Api api = getApi(apiClient);
            return api.listNamespacedPod(namespace).execute();
        } catch (ApiException e) {
            V1Status status = KubernetesException.parseStatusFromApiException(e);
            if (status != null) {
                throw new KubernetesException("Failed to get pods in namespace: " + namespace, null, status);
            } else {
                String errorDetail = "ApiException: code=" + e.getCode() + ", body=" + e.getResponseBody();
                throw new KubernetesException("Failed to get pods in namespace: " + namespace + " [" + errorDetail + "]", null, "listPods", e);
            }
        }
    }

    @Override
    public boolean isConnected(ApiClient apiClient) {
        try {
            CoreV1Api api = getApi(apiClient);
            api.listNamespace().execute();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    @Override
    public V1CustomResourceDefinitionList getCustomResourceDefinitions(ApiClient apiClient) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(apiClient);
            return crdApi.listCustomResourceDefinition().execute();
        } catch (ApiException e) {
            V1Status status = KubernetesException.parseStatusFromApiException(e);
            if (status != null) {
                throw new KubernetesException("Failed to get CRDs", "", status);
            } else {
                String errorDetail = "ApiException: code=" + e.getCode() + ", body=" + e.getResponseBody();
                throw new KubernetesException("Failed to get CRDs [" + errorDetail + "]", "", "listCRDs", e);
            }
        }
    }

    @Override
    public V1CustomResourceDefinition getCustomResourceDefinition(ApiClient apiClient, String name) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(apiClient);
            return crdApi.readCustomResourceDefinition(name).execute();
        } catch (ApiException e) {
            throw new ResourceNotFoundException("CRD", name, "");
        }
    }

    @Override
    public V1CustomResourceDefinition createCustomResourceDefinition(ApiClient apiClient, String crdYaml) {
        try {
            ApiextensionsV1Api crdApi = getCrdApi(apiClient);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode crdJson = mapper.readTree(crdYaml);
            V1CustomResourceDefinition crd = mapper.treeToValue(crdJson, V1CustomResourceDefinition.class);
            return crdApi.createCustomResourceDefinition(crd).execute();
        } catch (Exception e) {
            throw new KubernetesException("Failed to create CRD", "", "createCRD", e);
        }
    }

    @Override
    public V1Node patchNodeLabels(ApiClient apiClient, String nodeName, Map<String, String> labels) {
        Map<String, Object> patch = Map.of("metadata", Map.of("labels", labels));
        return patchNodeRaw(apiClient, nodeName, patch);
    }

    @Override
    public V1Node patchNodeSpec(ApiClient apiClient, String nodeName, Map<String, Object> specPatch) {
        Map<String, Object> patch = Map.of("spec", specPatch);
        return patchNodeRaw(apiClient, nodeName, patch);
    }

    @Override
    public V1Node patchNodeRaw(ApiClient apiClient, String nodeName, Object patchObject) {
        try {
            String patchJson = objectMapper.writeValueAsString(patchObject);
            V1Patch patch = new V1Patch(patchJson);
            CoreV1Api api = getApi(apiClient);
            return api.patchNode(nodeName, patch).execute();
        } catch (Exception e) {
            throw new KubernetesException("Failed to patch node: " + nodeName, null, "patchNode", e);
        }
    }

    @Override
    public V1Node patchNodeAuto(ApiClient apiClient, V1Node newNode) {
        return patchNodeAutoInternal(apiClient, newNode, null);
    }

    @Override
    public V1Node patchNodeStatusAuto(ApiClient apiClient, V1Node newNode) {
        return patchNodeAutoInternal(apiClient, newNode, "status");
    }

    /**
     * 自动 diff patch node，支持 patch metadata/spec 或 status 子资源
     */
    private V1Node patchNodeAutoInternal(ApiClient apiClient, V1Node newNode, String subresource) {
        String nodeName = newNode.getMetadata().getName();
        try {
            CoreV1Api api = getApi(apiClient);
            // 获取 oldNode
            V1Node oldNode = api.readNode(nodeName).execute();
            // 转为 JsonNode
            JsonNode oldJson = objectMapper.valueToTree(oldNode);
            JsonNode newJson = objectMapper.valueToTree(newNode);
            // 生成 diff patch (zjsonpatch)
            JsonNode patchNode = JsonDiff.asJson(oldJson, newJson);
            if (patchNode.isEmpty()) {
                return oldNode;
            }
            // 序列化 patch
            String patchString = objectMapper.writeValueAsString(patchNode);
            V1Patch patch = new V1Patch(patchString);
            if (subresource == null) {
                return api.patchNode(nodeName, patch).execute();
            } else if ("status".equals(subresource)) {
                return api.patchNodeStatus(nodeName, patch).execute();
            } else {
                throw new KubernetesException("Unsupported subresource for patchNode: " + subresource, null, "patchNodeAuto", null);
            }
        } catch (io.kubernetes.client.openapi.ApiException e) {
            V1Status status = KubernetesException.parseStatusFromApiException(e);
            if (status != null) {
                throw new KubernetesException("Kubernetes API error during patchNodeAuto", null, status);
            } else {
                String errorDetail = "ApiException: code=" + e.getCode() + ", body=" + e.getResponseBody();
                throw new KubernetesException("Kubernetes API error during patchNodeAuto [" + errorDetail + "]", null, "patchNodeAuto", e);
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new KubernetesException("JSON processing error during patchNodeAuto: " + e.getMessage(), null, "patchNodeAuto", e);
        } catch (Exception e) {
            throw new KubernetesException("Failed to auto patch node (subresource=" + subresource + "): " + nodeName, null, "patchNodeAuto", e);
        }
    }

    private ApiextensionsV1Api getCrdApi(ApiClient apiClient) {
        return new ApiextensionsV1Api(apiClient);
    }
} 