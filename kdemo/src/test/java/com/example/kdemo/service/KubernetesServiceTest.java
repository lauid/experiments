package com.example.kdemo.service;

import com.example.kdemo.dto.*;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.repository.KubernetesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionList;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1PodList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KubernetesServiceTest {

    @Mock
    private KubernetesRepository repository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KubernetesService service;

    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        apiClient = new ApiClient(); // 可根据需要mock或配置
    }

    @Test
    void testCheckConnection() {
        // Given
        String cluster = "test-cluster";
        when(repository.isConnected(apiClient)).thenReturn(true);

        // When
        ClusterInfo result = service.checkConnection(cluster);

        // Then
        assertNotNull(result);
        assertTrue(result.isConnected());
        assertEquals(cluster, result.getCluster());
        verify(repository).isConnected(apiClient);
    }

    @Test
    void testCheckConnectionWithNullCluster() {
        // Given
        when(repository.isConnected(apiClient)).thenReturn(false);

        // When
        ClusterInfo result = service.checkConnection(null);

        // Then
        assertNotNull(result);
        assertFalse(result.isConnected());
        assertEquals("cluster-local", result.getCluster());
        verify(repository).isConnected(apiClient);
    }

    @Test
    void testGetNamespaces() {
        // Given
        String cluster = "test-cluster";
        V1NamespaceList namespaceList = new V1NamespaceList();
        namespaceList.setItems(Arrays.asList(
            createNamespace("default"),
            createNamespace("kube-system")
        ));
        when(repository.getNamespaces(apiClient)).thenReturn(namespaceList);

        // When
        NamespaceInfo result = service.getNamespaces(cluster);

        // Then
        assertNotNull(result);
        assertEquals(cluster, result.getCluster());
        assertEquals(2, result.getCount());
        assertTrue(result.getNamespaces().contains("default"));
        assertTrue(result.getNamespaces().contains("kube-system"));
        verify(repository).getNamespaces(apiClient);
    }

    @Test
    void testGetPodsInNamespace() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        V1PodList podList = new V1PodList();
        podList.setItems(Arrays.asList(
            createPod("pod1"),
            createPod("pod2")
        ));
        when(repository.getPodsInNamespace(apiClient, namespace)).thenReturn(podList);

        // When
        PodInfo result = service.getPodsInNamespace(cluster, namespace);

        // Then
        assertNotNull(result);
        assertEquals(cluster, result.getCluster());
        assertEquals(namespace, result.getNamespace());
        assertEquals(2, result.getCount());
        assertTrue(result.getPods().contains("pod1"));
        assertTrue(result.getPods().contains("pod2"));
        verify(repository).getPodsInNamespace(apiClient, namespace);
    }

    @Test
    void testGetClusterOverview() {
        // Given
        String cluster = "test-cluster";
        V1NamespaceList namespaceList = new V1NamespaceList();
        namespaceList.setItems(Arrays.asList(
            createNamespace("default"),
            createNamespace("kube-system")
        ));
        V1PodList podList1 = new V1PodList();
        podList1.setItems(Arrays.asList(createPod("pod1"), createPod("pod2")));
        V1PodList podList2 = new V1PodList();
        podList2.setItems(Arrays.asList(createPod("pod3")));

        when(repository.getNamespaces(apiClient)).thenReturn(namespaceList);
        when(repository.getPodsInNamespace(apiClient, "default")).thenReturn(podList1);
        when(repository.getPodsInNamespace(apiClient, "kube-system")).thenReturn(podList2);

        // When
        ClusterOverview result = service.getClusterOverview(cluster);

        // Then
        assertNotNull(result);
        assertEquals(cluster, result.getCluster());
        assertEquals(2, result.getNamespaceCount());
        assertEquals(3, result.getTotalPods());
        assertEquals(2, result.getPodsPerNamespace().get("default"));
        assertEquals(1, result.getPodsPerNamespace().get("kube-system"));
    }

    @Test
    void testGetCustomResourceDefinitions() {
        // Given
        String cluster = "test-cluster";
        V1CustomResourceDefinitionList crdList = new V1CustomResourceDefinitionList();
        crdList.setItems(Arrays.asList(
            createCRD("applications.example.com"),
            createCRD("microservices.example.com")
        ));
        when(repository.getCustomResourceDefinitions(apiClient)).thenReturn(crdList);

        // When
        CrdInfo result = service.getCustomResourceDefinitions(cluster);

        // Then
        assertNotNull(result);
        assertEquals(cluster, result.getCluster());
        assertEquals(2, result.getCount());
        assertTrue(result.getCrds().contains("applications.example.com"));
        assertTrue(result.getCrds().contains("microservices.example.com"));
        verify(repository).getCustomResourceDefinitions(apiClient);
    }

    @Test
    void testGetCustomResourceDefinition() {
        // Given
        String cluster = "test-cluster";
        String name = "applications.example.com";
        V1CustomResourceDefinition crd = createCRD(name);
        when(repository.getCustomResourceDefinition(apiClient, name)).thenReturn(crd);

        // When
        Map<String, Object> result = service.getCustomResourceDefinition(cluster, name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.get("name"));
        verify(repository).getCustomResourceDefinition(apiClient, name);
    }

    @Test
    void testCreateCustomResourceDefinition() {
        // Given
        String cluster = "test-cluster";
        String crdYaml = "apiVersion: apiextensions.k8s.io/v1\nkind: CustomResourceDefinition";
        V1CustomResourceDefinition crd = createCRD("test.example.com");
        when(repository.createCustomResourceDefinition(apiClient, crdYaml)).thenReturn(crd);

        // When
        OperationResult result = service.createCustomResourceDefinition(cluster, crdYaml);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("test.example.com", result.getName());
        verify(repository).createCustomResourceDefinition(apiClient, crdYaml);
    }

    @Test
    void testCreateCustomResourceDefinitionFailure() {
        // Given
        String cluster = "test-cluster";
        String crdYaml = "invalid yaml";
        when(repository.createCustomResourceDefinition(apiClient, crdYaml))
            .thenThrow(new RuntimeException("Invalid CRD"));

        // When
        OperationResult result = service.createCustomResourceDefinition(cluster, crdYaml);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("CRD", result.getName());
        assertNotNull(result.getError());
        verify(repository).createCustomResourceDefinition(apiClient, crdYaml);
    }

    // Application tests
    @Test
    void testGetApplications() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        List<Application> applications = Arrays.asList(
            createApplication("app1"),
            createApplication("app2")
        );
        when(repository.getApplications(apiClient, namespace)).thenReturn(applications);

        // When
        ResourceResponse<Application> result = service.getApplications(cluster, namespace);

        // Then
        assertNotNull(result);
        assertEquals(cluster, result.getCluster());
        assertEquals(namespace, result.getNamespace());
        assertEquals(2, result.getCount());
        assertEquals(applications, result.getResources());
        verify(repository).getApplications(apiClient, namespace);
    }

    @Test
    void testGetApplication() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";
        Application application = createApplication(name);
        when(repository.getApplication(apiClient, namespace, name)).thenReturn(application);

        // When
        Application result = service.getApplication(cluster, namespace, name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getMetadata().getName());
        verify(repository).getApplication(apiClient, namespace, name);
    }

    @Test
    void testCreateApplication() throws Exception {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-app\"}}";
        Application application = createApplication("test-app");
        when(objectMapper.readValue(resourceYaml, Application.class)).thenReturn(application);
        when(repository.createApplication(apiClient, namespace, application)).thenReturn(application);

        // When
        OperationResult result = service.createApplication(cluster, namespace, resourceYaml);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("test-app", result.getName());
        verify(objectMapper).readValue(resourceYaml, Application.class);
        verify(repository).createApplication(apiClient, namespace, application);
    }

    @Test
    void testUpdateApplication() throws Exception {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-app\"}}";
        Application application = createApplication(name);
        when(objectMapper.readValue(resourceYaml, Application.class)).thenReturn(application);
        when(repository.updateApplication(apiClient, namespace, name, application)).thenReturn(application);

        // When
        OperationResult result = service.updateApplication(cluster, namespace, name, resourceYaml);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(objectMapper).readValue(resourceYaml, Application.class);
        verify(repository).updateApplication(apiClient, namespace, name, application);
    }

    @Test
    void testDeleteApplication() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";
        doNothing().when(repository).deleteApplication(apiClient, namespace, name);

        // When
        OperationResult result = service.deleteApplication(cluster, namespace, name);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(repository).deleteApplication(apiClient, namespace, name);
    }

    // Microservice tests
    @Test
    void testGetMicroservices() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        List<Microservice> microservices = Arrays.asList(
            createMicroservice("svc1"),
            createMicroservice("svc2")
        );
        when(repository.getMicroservices(apiClient, namespace)).thenReturn(microservices);

        // When
        ResourceResponse<Microservice> result = service.getMicroservices(cluster, namespace);

        // Then
        assertNotNull(result);
        assertEquals(cluster, result.getCluster());
        assertEquals(namespace, result.getNamespace());
        assertEquals(2, result.getCount());
        assertEquals(microservices, result.getResources());
        verify(repository).getMicroservices(apiClient, namespace);
    }

    @Test
    void testGetMicroservice() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";
        Microservice microservice = createMicroservice(name);
        when(repository.getMicroservice(apiClient, namespace, name)).thenReturn(microservice);

        // When
        Microservice result = service.getMicroservice(cluster, namespace, name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getMetadata().getName());
        verify(repository).getMicroservice(apiClient, namespace, name);
    }

    @Test
    void testCreateMicroservice() throws Exception {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-svc\"}}";
        Microservice microservice = createMicroservice("test-svc");
        when(objectMapper.readValue(resourceYaml, Microservice.class)).thenReturn(microservice);
        when(repository.createMicroservice(apiClient, namespace, microservice)).thenReturn(microservice);

        // When
        OperationResult result = service.createMicroservice(cluster, namespace, resourceYaml);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("test-svc", result.getName());
        verify(objectMapper).readValue(resourceYaml, Microservice.class);
        verify(repository).createMicroservice(apiClient, namespace, microservice);
    }

    @Test
    void testUpdateMicroservice() throws Exception {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-svc\"}}";
        Microservice microservice = createMicroservice(name);
        when(objectMapper.readValue(resourceYaml, Microservice.class)).thenReturn(microservice);
        when(repository.updateMicroservice(apiClient, namespace, name, microservice)).thenReturn(microservice);

        // When
        OperationResult result = service.updateMicroservice(cluster, namespace, name, resourceYaml);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(objectMapper).readValue(resourceYaml, Microservice.class);
        verify(repository).updateMicroservice(apiClient, namespace, name, microservice);
    }

    @Test
    void testDeleteMicroservice() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";
        doNothing().when(repository).deleteMicroservice(apiClient, namespace, name);

        // When
        OperationResult result = service.deleteMicroservice(cluster, namespace, name);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(repository).deleteMicroservice(apiClient, namespace, name);
    }

    // GPU tests
    @Test
    void testGetGPUs() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        List<GPU> gpus = Arrays.asList(
            createGPU("gpu1"),
            createGPU("gpu2")
        );
        when(repository.getGPUs(apiClient, namespace)).thenReturn(gpus);

        // When
        ResourceResponse<GPU> result = service.getGPUs(cluster, namespace);

        // Then
        assertNotNull(result);
        assertEquals(cluster, result.getCluster());
        assertEquals(namespace, result.getNamespace());
        assertEquals(2, result.getCount());
        assertEquals(gpus, result.getResources());
        verify(repository).getGPUs(apiClient, namespace);
    }

    @Test
    void testGetGPU() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";
        GPU gpu = createGPU(name);
        when(repository.getGPU(apiClient, namespace, name)).thenReturn(gpu);

        // When
        GPU result = service.getGPU(cluster, namespace, name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getMetadata().getName());
        verify(repository).getGPU(apiClient, namespace, name);
    }

    @Test
    void testCreateGPU() throws Exception {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-gpu\"}}";
        GPU gpu = createGPU("test-gpu");
        when(objectMapper.readValue(resourceYaml, GPU.class)).thenReturn(gpu);
        when(repository.createGPU(apiClient, namespace, gpu)).thenReturn(gpu);

        // When
        OperationResult result = service.createGPU(cluster, namespace, resourceYaml);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("test-gpu", result.getName());
        verify(objectMapper).readValue(resourceYaml, GPU.class);
        verify(repository).createGPU(apiClient, namespace, gpu);
    }

    @Test
    void testUpdateGPU() throws Exception {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-gpu\"}}";
        GPU gpu = createGPU(name);
        when(objectMapper.readValue(resourceYaml, GPU.class)).thenReturn(gpu);
        when(repository.updateGPU(apiClient, namespace, name, gpu)).thenReturn(gpu);

        // When
        OperationResult result = service.updateGPU(cluster, namespace, name, resourceYaml);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(objectMapper).readValue(resourceYaml, GPU.class);
        verify(repository).updateGPU(apiClient, namespace, name, gpu);
    }

    @Test
    void testDeleteGPU() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";
        doNothing().when(repository).deleteGPU(apiClient, namespace, name);

        // When
        OperationResult result = service.deleteGPU(cluster, namespace, name);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(repository).deleteGPU(apiClient, namespace, name);
    }

    // Helper methods
    private io.kubernetes.client.openapi.models.V1Namespace createNamespace(String name) {
        io.kubernetes.client.openapi.models.V1Namespace namespace = new io.kubernetes.client.openapi.models.V1Namespace();
        io.kubernetes.client.openapi.models.V1ObjectMeta metadata = new io.kubernetes.client.openapi.models.V1ObjectMeta();
        metadata.setName(name);
        namespace.setMetadata(metadata);
        return namespace;
    }

    private io.kubernetes.client.openapi.models.V1Pod createPod(String name) {
        io.kubernetes.client.openapi.models.V1Pod pod = new io.kubernetes.client.openapi.models.V1Pod();
        io.kubernetes.client.openapi.models.V1ObjectMeta metadata = new io.kubernetes.client.openapi.models.V1ObjectMeta();
        metadata.setName(name);
        pod.setMetadata(metadata);
        return pod;
    }

    private V1CustomResourceDefinition createCRD(String name) {
        V1CustomResourceDefinition crd = new V1CustomResourceDefinition();
        io.kubernetes.client.openapi.models.V1ObjectMeta metadata = new io.kubernetes.client.openapi.models.V1ObjectMeta();
        metadata.setName(name);
        crd.setMetadata(metadata);
        
        // Add spec to avoid null pointer exceptions
        io.kubernetes.client.openapi.models.V1CustomResourceDefinitionSpec spec = new io.kubernetes.client.openapi.models.V1CustomResourceDefinitionSpec();
        spec.setGroup("example.com");
        
        io.kubernetes.client.openapi.models.V1CustomResourceDefinitionVersion version = new io.kubernetes.client.openapi.models.V1CustomResourceDefinitionVersion();
        version.setName("v1");
        spec.setVersions(List.of(version));
        
        spec.setScope("Namespaced");
        
        io.kubernetes.client.openapi.models.V1CustomResourceDefinitionNames names = new io.kubernetes.client.openapi.models.V1CustomResourceDefinitionNames();
        names.setKind("Application");
        names.setPlural("applications");
        names.setSingular("application");
        spec.setNames(names);
        
        crd.setSpec(spec);
        
        return crd;
    }

    private Application createApplication(String name) {
        Application application = new Application();
        io.kubernetes.client.openapi.models.V1ObjectMeta metadata = new io.kubernetes.client.openapi.models.V1ObjectMeta();
        metadata.setName(name);
        application.setMetadata(metadata);
        return application;
    }

    private Microservice createMicroservice(String name) {
        Microservice microservice = new Microservice();
        io.kubernetes.client.openapi.models.V1ObjectMeta metadata = new io.kubernetes.client.openapi.models.V1ObjectMeta();
        metadata.setName(name);
        microservice.setMetadata(metadata);
        return microservice;
    }

    private GPU createGPU(String name) {
        GPU gpu = new GPU();
        io.kubernetes.client.openapi.models.V1ObjectMeta metadata = new io.kubernetes.client.openapi.models.V1ObjectMeta();
        metadata.setName(name);
        gpu.setMetadata(metadata);
        return gpu;
    }
} 