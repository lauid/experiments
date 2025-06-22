package com.example.kdemo.controller;

import com.example.kdemo.dto.*;
import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.service.KubernetesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesControllerTest {

    @Mock
    private KubernetesService kubernetesService;

    @InjectMocks
    private KubernetesController kubernetesController;

    private ClusterInfo clusterInfo;
    private NamespaceInfo namespaceInfo;
    private PodInfo podInfo;
    private ClusterOverview clusterOverview;
    private Application application;
    private Microservice microservice;
    private GPU gpu;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        clusterInfo = new ClusterInfo(true, "cluster-local");
        
        namespaceInfo = new NamespaceInfo("cluster-local", Arrays.asList("default", "kube-system"));
        
        podInfo = new PodInfo("cluster-local", "default", Arrays.asList("pod1", "pod2"));
        
        clusterOverview = new ClusterOverview(
            "cluster-local", 
            2, 
            5, 
            Map.of("default", 3, "kube-system", 2),
            Arrays.asList("default", "kube-system")
        );

        // 设置Application测试数据
        application = new Application();
        V1ObjectMeta appMeta = new V1ObjectMeta();
        appMeta.setName("test-app");
        application.setMetadata(appMeta);

        // 设置Microservice测试数据
        microservice = new Microservice();
        V1ObjectMeta msMeta = new V1ObjectMeta();
        msMeta.setName("test-ms");
        microservice.setMetadata(msMeta);

        // 设置GPU测试数据
        gpu = new GPU();
        V1ObjectMeta gpuMeta = new V1ObjectMeta();
        gpuMeta.setName("test-gpu");
        gpu.setMetadata(gpuMeta);
    }

    @Test
    void testCheckConnection() {
        // Given
        when(kubernetesService.checkConnection("cluster-local")).thenReturn(clusterInfo);

        // When
        ResponseEntity<ClusterInfo> response = kubernetesController.checkConnection("cluster-local");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isConnected());
        assertEquals("cluster-local", response.getBody().getCluster());
        verify(kubernetesService).checkConnection("cluster-local");
    }

    @Test
    void testGetNamespaces() {
        // Given
        when(kubernetesService.getNamespaces("cluster-local")).thenReturn(namespaceInfo);

        // When
        ResponseEntity<NamespaceInfo> response = kubernetesController.getNamespaces("cluster-local");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getCount());
        assertTrue(response.getBody().getNamespaces().contains("default"));
        verify(kubernetesService).getNamespaces("cluster-local");
    }

    @Test
    void testGetPodsInNamespace() {
        // Given
        when(kubernetesService.getPodsInNamespace("cluster-local", "default")).thenReturn(podInfo);

        // When
        ResponseEntity<PodInfo> response = kubernetesController.getPodsInNamespace("cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getCount());
        assertTrue(response.getBody().getPods().contains("pod1"));
        verify(kubernetesService).getPodsInNamespace("cluster-local", "default");
    }

    @Test
    void testGetClusterOverview() {
        // Given
        when(kubernetesService.getClusterOverview("cluster-local")).thenReturn(clusterOverview);

        // When
        ResponseEntity<ClusterOverview> response = kubernetesController.getClusterOverview("cluster-local");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getNamespaceCount());
        assertEquals(5, response.getBody().getTotalPods());
        verify(kubernetesService).getClusterOverview("cluster-local");
    }

    @Test
    void testGetApplications() {
        // Given
        ResourceResponse<Application> appResponse = new ResourceResponse<>("cluster-local", "default", Arrays.asList(application));
        when(kubernetesService.getApplications("cluster-local", "default")).thenReturn(appResponse);

        // When
        ResponseEntity<ResourceResponse<Application>> response = kubernetesController.getApplications("cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getResources().size());
        assertEquals("test-app", response.getBody().getResources().get(0).getMetadata().getName());
        verify(kubernetesService).getApplications("cluster-local", "default");
    }

    @Test
    void testGetApplication() {
        // Given
        when(kubernetesService.getApplication("cluster-local", "default", "test-app")).thenReturn(application);

        // When
        ResponseEntity<Application> response = kubernetesController.getApplication("test-app", "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-app", response.getBody().getMetadata().getName());
        verify(kubernetesService).getApplication("cluster-local", "default", "test-app");
    }

    @Test
    void testCreateApplication() {
        // Given
        String resourceYaml = "{\"metadata\":{\"name\":\"test-app\"}}";
        OperationResult result = OperationResult.success("cluster-local", "test-app", "Application created successfully");
        when(kubernetesService.createApplication("cluster-local", "default", resourceYaml)).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.createApplication(resourceYaml, "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-app", response.getBody().getName());
        verify(kubernetesService).createApplication("cluster-local", "default", resourceYaml);
    }

    @Test
    void testUpdateApplication() {
        // Given
        String resourceYaml = "{\"metadata\":{\"name\":\"test-app\"}}";
        OperationResult result = OperationResult.success("cluster-local", "test-app", "Application updated successfully");
        when(kubernetesService.updateApplication("cluster-local", "default", "test-app", resourceYaml)).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.updateApplication("test-app", resourceYaml, "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-app", response.getBody().getName());
        verify(kubernetesService).updateApplication("cluster-local", "default", "test-app", resourceYaml);
    }

    @Test
    void testDeleteApplication() {
        // Given
        OperationResult result = OperationResult.success("cluster-local", "test-app", "Application deleted successfully");
        when(kubernetesService.deleteApplication("cluster-local", "default", "test-app")).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.deleteApplication("test-app", "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-app", response.getBody().getName());
        verify(kubernetesService).deleteApplication("cluster-local", "default", "test-app");
    }

    @Test
    void testGetMicroservices() {
        // Given
        ResourceResponse<Microservice> msResponse = new ResourceResponse<>("cluster-local", "default", Arrays.asList(microservice));
        when(kubernetesService.getMicroservices("cluster-local", "default")).thenReturn(msResponse);

        // When
        ResponseEntity<ResourceResponse<Microservice>> response = kubernetesController.getMicroservices("cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getResources().size());
        assertEquals("test-ms", response.getBody().getResources().get(0).getMetadata().getName());
        verify(kubernetesService).getMicroservices("cluster-local", "default");
    }

    @Test
    void testGetMicroservice() {
        // Given
        when(kubernetesService.getMicroservice("cluster-local", "default", "test-ms")).thenReturn(microservice);

        // When
        ResponseEntity<Microservice> response = kubernetesController.getMicroservice("test-ms", "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-ms", response.getBody().getMetadata().getName());
        verify(kubernetesService).getMicroservice("cluster-local", "default", "test-ms");
    }

    @Test
    void testCreateMicroservice() {
        // Given
        String resourceYaml = "{\"metadata\":{\"name\":\"test-ms\"}}";
        OperationResult result = OperationResult.success("cluster-local", "test-ms", "Microservice created successfully");
        when(kubernetesService.createMicroservice("cluster-local", "default", resourceYaml)).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.createMicroservice(resourceYaml, "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-ms", response.getBody().getName());
        verify(kubernetesService).createMicroservice("cluster-local", "default", resourceYaml);
    }

    @Test
    void testUpdateMicroservice() {
        // Given
        String resourceYaml = "{\"metadata\":{\"name\":\"test-ms\"}}";
        OperationResult result = OperationResult.success("cluster-local", "test-ms", "Microservice updated successfully");
        when(kubernetesService.updateMicroservice("cluster-local", "default", "test-ms", resourceYaml)).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.updateMicroservice("test-ms", resourceYaml, "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-ms", response.getBody().getName());
        verify(kubernetesService).updateMicroservice("cluster-local", "default", "test-ms", resourceYaml);
    }

    @Test
    void testDeleteMicroservice() {
        // Given
        OperationResult result = OperationResult.success("cluster-local", "test-ms", "Microservice deleted successfully");
        when(kubernetesService.deleteMicroservice("cluster-local", "default", "test-ms")).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.deleteMicroservice("test-ms", "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-ms", response.getBody().getName());
        verify(kubernetesService).deleteMicroservice("cluster-local", "default", "test-ms");
    }

    @Test
    void testGetGPUs() {
        // Given
        ResourceResponse<GPU> gpuResponse = new ResourceResponse<>("cluster-local", "default", Arrays.asList(gpu));
        when(kubernetesService.getGPUs("cluster-local", "default")).thenReturn(gpuResponse);

        // When
        ResponseEntity<ResourceResponse<GPU>> response = kubernetesController.getGPUs("cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getResources().size());
        assertEquals("test-gpu", response.getBody().getResources().get(0).getMetadata().getName());
        verify(kubernetesService).getGPUs("cluster-local", "default");
    }

    @Test
    void testGetGPU() {
        // Given
        when(kubernetesService.getGPU("cluster-local", "default", "test-gpu")).thenReturn(gpu);

        // When
        ResponseEntity<GPU> response = kubernetesController.getGPU("test-gpu", "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-gpu", response.getBody().getMetadata().getName());
        verify(kubernetesService).getGPU("cluster-local", "default", "test-gpu");
    }

    @Test
    void testCreateGPU() {
        // Given
        String resourceYaml = "{\"metadata\":{\"name\":\"test-gpu\"}}";
        OperationResult result = OperationResult.success("cluster-local", "test-gpu", "GPU created successfully");
        when(kubernetesService.createGPU("cluster-local", "default", resourceYaml)).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.createGPU(resourceYaml, "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-gpu", response.getBody().getName());
        verify(kubernetesService).createGPU("cluster-local", "default", resourceYaml);
    }

    @Test
    void testUpdateGPU() {
        // Given
        String resourceYaml = "{\"metadata\":{\"name\":\"test-gpu\"}}";
        OperationResult result = OperationResult.success("cluster-local", "test-gpu", "GPU updated successfully");
        when(kubernetesService.updateGPU("cluster-local", "default", "test-gpu", resourceYaml)).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.updateGPU("test-gpu", resourceYaml, "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-gpu", response.getBody().getName());
        verify(kubernetesService).updateGPU("cluster-local", "default", "test-gpu", resourceYaml);
    }

    @Test
    void testDeleteGPU() {
        // Given
        OperationResult result = OperationResult.success("cluster-local", "test-gpu", "GPU deleted successfully");
        when(kubernetesService.deleteGPU("cluster-local", "default", "test-gpu")).thenReturn(result);

        // When
        ResponseEntity<OperationResult> response = kubernetesController.deleteGPU("test-gpu", "cluster-local", "default");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("test-gpu", response.getBody().getName());
        verify(kubernetesService).deleteGPU("cluster-local", "default", "test-gpu");
    }
} 