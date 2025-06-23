package com.example.kdemo.service;

import com.example.kdemo.dto.*;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.repository.KubernetesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckConnection() {
        when(repository.isConnected()).thenReturn(true);
        ClusterInfo result = service.checkConnection("test-cluster");
        assertNotNull(result);
        assertTrue(result.isConnected());
        verify(repository).isConnected();
    }

    @Test
    void testCheckConnectionWithNullCluster() {
        when(repository.isConnected()).thenReturn(false);
        ClusterInfo result = service.checkConnection(null);
        assertNotNull(result);
        assertFalse(result.isConnected());
        verify(repository).isConnected();
    }

    @Test
    void testGetNamespaces() {
        V1NamespaceList namespaceList = new V1NamespaceList();
        namespaceList.setItems(Arrays.asList(
            createNamespace("default"),
            createNamespace("kube-system")
        ));
        when(repository.getNamespaces()).thenReturn(namespaceList);
        NamespaceInfo result = service.getNamespaces("test-cluster");
        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertTrue(result.getNamespaces().contains("default"));
        assertTrue(result.getNamespaces().contains("kube-system"));
        verify(repository).getNamespaces();
    }

    @Test
    void testGetPodsInNamespace() {
        V1PodList podList = new V1PodList();
        podList.setItems(Arrays.asList(
            createPod("pod1"),
            createPod("pod2")
        ));
        when(repository.getPodsInNamespace()).thenReturn(podList);
        PodInfo result = service.getPodsInNamespace("test-cluster", "default");
        assertNotNull(result);
        assertEquals("default", result.getNamespace());
        assertEquals(2, result.getCount());
        assertTrue(result.getPods().contains("pod1"));
        assertTrue(result.getPods().contains("pod2"));
        verify(repository).getPodsInNamespace();
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

        when(repository.getNamespaces()).thenReturn(namespaceList);
        when(repository.getPodsInNamespace()).thenReturn(podList1);
        when(repository.getPodsInNamespace()).thenReturn(podList2);

        // When
        ClusterOverview result = service.getClusterOverview("test-cluster");

        // Then
        assertNotNull(result);
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
        when(repository.getCustomResourceDefinitions()).thenReturn(crdList);

        // When
        CrdInfo result = service.getCustomResourceDefinitions("test-cluster");

        // Then
        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertTrue(result.getCrds().contains("applications.example.com"));
        assertTrue(result.getCrds().contains("microservices.example.com"));
        verify(repository).getCustomResourceDefinitions();
    }

    @Test
    void testGetCustomResourceDefinition() {
        String cluster = "test-cluster";
        String name = "applications.example.com";
        V1CustomResourceDefinition crd = createCRD(name);
        when(repository.getCustomResourceDefinition(name)).thenReturn(crd);
        Map<String, Object> result = service.getCustomResourceDefinition(cluster, name);
        assertNotNull(result);
        assertEquals(name, result.get("name"));
        verify(repository).getCustomResourceDefinition(name);
    }

    @Test
    void testCreateCustomResourceDefinition() {
        String cluster = "test-cluster";
        String crdYaml = "apiVersion: apiextensions.k8s.io/v1\nkind: CustomResourceDefinition";
        V1CustomResourceDefinition crd = createCRD("test.example.com");
        when(repository.createCustomResourceDefinition(crdYaml)).thenReturn(crd);
        OperationResult result = service.createCustomResourceDefinition(cluster, crdYaml);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("test.example.com", result.getName());
        verify(repository).createCustomResourceDefinition(crdYaml);
    }

    @Test
    void testCreateCustomResourceDefinitionFailure() {
        String cluster = "test-cluster";
        String crdYaml = "invalid yaml";
        when(repository.createCustomResourceDefinition(crdYaml)).thenThrow(new RuntimeException("Invalid CRD"));
        OperationResult result = service.createCustomResourceDefinition(cluster, crdYaml);
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("CRD", result.getName());
        assertNotNull(result.getError());
        verify(repository).createCustomResourceDefinition(crdYaml);
    }

    // Application tests
    @Test
    void testGetApplications() {
        String cluster = "test-cluster";
        String namespace = "default";
        List<Application> applications = Arrays.asList(
            createApplication("app1"),
            createApplication("app2")
        );
        when(repository.getApplications()).thenReturn(applications);
        ResourceResponse<Application> result = service.getApplications(cluster, namespace);
        assertNotNull(result);
        assertEquals(namespace, result.getNamespace());
        assertEquals(2, result.getCount());
        assertEquals(applications, result.getResources());
        verify(repository).getApplications();
    }

    @Test
    void testGetApplication() {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";
        Application application = createApplication(name);
        when(repository.getApplication(name)).thenReturn(application);
        Application result = service.getApplication(cluster, namespace, name);
        assertNotNull(result);
        assertEquals(name, result.getMetadata().getName());
        verify(repository).getApplication(name);
    }

    @Test
    void testCreateApplication() throws Exception {
        String cluster = "test-cluster";
        String namespace = "default";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-app\"}}";
        Application application = createApplication("test-app");
        when(objectMapper.readValue(resourceYaml, Application.class)).thenReturn(application);
        when(repository.createApplication(application)).thenReturn(application);
        OperationResult result = service.createApplication(cluster, namespace, resourceYaml);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("test-app", result.getName());
        verify(objectMapper).readValue(resourceYaml, Application.class);
        verify(repository).createApplication(application);
    }

    @Test
    void testUpdateApplication() throws Exception {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-app\"}}";
        Application application = createApplication(name);
        when(objectMapper.readValue(resourceYaml, Application.class)).thenReturn(application);
        when(repository.updateApplication(name, application)).thenReturn(application);
        OperationResult result = service.updateApplication(cluster, namespace, name, resourceYaml);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(objectMapper).readValue(resourceYaml, Application.class);
        verify(repository).updateApplication(name, application);
    }

    @Test
    void testDeleteApplication() {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";
        doNothing().when(repository).deleteApplication(name);
        OperationResult result = service.deleteApplication(cluster, namespace, name);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(repository).deleteApplication(name);
    }

    // Microservice tests
    @Test
    void testGetMicroservices() {
        String cluster = "test-cluster";
        String namespace = "default";
        List<Microservice> microservices = Arrays.asList(
            createMicroservice("svc1"),
            createMicroservice("svc2")
        );
        when(repository.getMicroservices()).thenReturn(microservices);
        ResourceResponse<Microservice> result = service.getMicroservices(cluster, namespace);
        assertNotNull(result);
        assertEquals(namespace, result.getNamespace());
        assertEquals(2, result.getCount());
        assertEquals(microservices, result.getResources());
        verify(repository).getMicroservices();
    }

    @Test
    void testGetMicroservice() {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";
        Microservice microservice = createMicroservice(name);
        when(repository.getMicroservice(name)).thenReturn(microservice);
        Microservice result = service.getMicroservice(cluster, namespace, name);
        assertNotNull(result);
        assertEquals(name, result.getMetadata().getName());
        verify(repository).getMicroservice(name);
    }

    @Test
    void testCreateMicroservice() throws Exception {
        String cluster = "test-cluster";
        String namespace = "default";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-svc\"}}";
        Microservice microservice = createMicroservice("test-svc");
        when(objectMapper.readValue(resourceYaml, Microservice.class)).thenReturn(microservice);
        when(repository.createMicroservice(microservice)).thenReturn(microservice);
        OperationResult result = service.createMicroservice(cluster, namespace, resourceYaml);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("test-svc", result.getName());
        verify(objectMapper).readValue(resourceYaml, Microservice.class);
        verify(repository).createMicroservice(microservice);
    }

    @Test
    void testUpdateMicroservice() throws Exception {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-svc\"}}";
        Microservice microservice = createMicroservice(name);
        when(objectMapper.readValue(resourceYaml, Microservice.class)).thenReturn(microservice);
        when(repository.updateMicroservice(name, microservice)).thenReturn(microservice);
        OperationResult result = service.updateMicroservice(cluster, namespace, name, resourceYaml);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(objectMapper).readValue(resourceYaml, Microservice.class);
        verify(repository).updateMicroservice(name, microservice);
    }

    @Test
    void testDeleteMicroservice() {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";
        doNothing().when(repository).deleteMicroservice(name);
        OperationResult result = service.deleteMicroservice(cluster, namespace, name);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(repository).deleteMicroservice(name);
    }

    // GPU tests
    @Test
    void testGetGPUs() {
        String cluster = "test-cluster";
        String namespace = "default";
        List<GPU> gpus = Arrays.asList(
            createGPU("gpu1"),
            createGPU("gpu2")
        );
        when(repository.getGPUs()).thenReturn(gpus);
        ResourceResponse<GPU> result = service.getGPUs(cluster, namespace);
        assertNotNull(result);
        assertEquals(namespace, result.getNamespace());
        assertEquals(2, result.getCount());
        assertEquals(gpus, result.getResources());
        verify(repository).getGPUs();
    }

    @Test
    void testGetGPU() {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";
        GPU gpu = createGPU(name);
        when(repository.getGPU(name)).thenReturn(gpu);
        GPU result = service.getGPU(cluster, namespace, name);
        assertNotNull(result);
        assertEquals(name, result.getMetadata().getName());
        verify(repository).getGPU(name);
    }

    @Test
    void testCreateGPU() throws Exception {
        String cluster = "test-cluster";
        String namespace = "default";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-gpu\"}}";
        GPU gpu = createGPU("test-gpu");
        when(objectMapper.readValue(resourceYaml, GPU.class)).thenReturn(gpu);
        when(repository.createGPU(gpu)).thenReturn(gpu);
        OperationResult result = service.createGPU(cluster, namespace, resourceYaml);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals("test-gpu", result.getName());
        verify(objectMapper).readValue(resourceYaml, GPU.class);
        verify(repository).createGPU(gpu);
    }

    @Test
    void testUpdateGPU() throws Exception {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";
        String resourceYaml = "{\"metadata\":{\"name\":\"test-gpu\"}}";
        GPU gpu = createGPU(name);
        when(objectMapper.readValue(resourceYaml, GPU.class)).thenReturn(gpu);
        when(repository.updateGPU(name, gpu)).thenReturn(gpu);
        OperationResult result = service.updateGPU(cluster, namespace, name, resourceYaml);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(objectMapper).readValue(resourceYaml, GPU.class);
        verify(repository).updateGPU(name, gpu);
    }

    @Test
    void testDeleteGPU() {
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";
        doNothing().when(repository).deleteGPU(name);
        OperationResult result = service.deleteGPU(cluster, namespace, name);
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(cluster, result.getCluster());
        assertEquals(name, result.getName());
        verify(repository).deleteGPU(name);
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