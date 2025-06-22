package com.example.kdemo.repository;

import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.exception.KubernetesException;
import com.example.kdemo.exception.ResourceNotFoundException;
import io.kubernetes.client.openapi.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KubernetesRepositoryImplTest {

    private KubernetesRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:1234");
        repository = new KubernetesRepositoryImpl(client);
    }

    @Test
    void testGetNamespaces() {
        // Given
        String cluster = "test-cluster";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getNamespaces(cluster);
        });
    }

    @Test
    void testGetPodsInNamespace() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getPodsInNamespace(cluster, namespace);
        });
    }

    @Test
    void testGetCustomResourceDefinitions() {
        // Given
        String cluster = "test-cluster";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getCustomResourceDefinitions(cluster);
        });
    }

    @Test
    void testGetCustomResourceDefinition() {
        // Given
        String cluster = "test-cluster";
        String name = "applications.example.com";

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            repository.getCustomResourceDefinition(cluster, name);
        });
    }

    @Test
    void testCreateCustomResourceDefinition() {
        // Given
        String cluster = "test-cluster";
        String crdYaml = "apiVersion: apiextensions.k8s.io/v1\nkind: CustomResourceDefinition";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.createCustomResourceDefinition(cluster, crdYaml);
        });
    }

    @Test
    void testGetApplications() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getApplications(cluster, namespace);
        });
    }

    @Test
    void testGetApplication() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getApplication(cluster, namespace, name);
        });
    }

    @Test
    void testCreateApplication() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        Application application = new Application();

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.createApplication(cluster, namespace, application);
        });
    }

    @Test
    void testUpdateApplication() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";
        Application application = new Application();

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.updateApplication(cluster, namespace, name, application);
        });
    }

    @Test
    void testDeleteApplication() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-app";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.deleteApplication(cluster, namespace, name);
        });
    }

    @Test
    void testGetMicroservices() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getMicroservices(cluster, namespace);
        });
    }

    @Test
    void testGetMicroservice() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getMicroservice(cluster, namespace, name);
        });
    }

    @Test
    void testCreateMicroservice() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        Microservice microservice = new Microservice();

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.createMicroservice(cluster, namespace, microservice);
        });
    }

    @Test
    void testUpdateMicroservice() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";
        Microservice microservice = new Microservice();

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.updateMicroservice(cluster, namespace, name, microservice);
        });
    }

    @Test
    void testDeleteMicroservice() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-svc";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.deleteMicroservice(cluster, namespace, name);
        });
    }

    @Test
    void testGetGPUs() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getGPUs(cluster, namespace);
        });
    }

    @Test
    void testGetGPU() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getGPU(cluster, namespace, name);
        });
    }

    @Test
    void testCreateGPU() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        GPU gpu = new GPU();

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.createGPU(cluster, namespace, gpu);
        });
    }

    @Test
    void testUpdateGPU() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";
        GPU gpu = new GPU();

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.updateGPU(cluster, namespace, name, gpu);
        });
    }

    @Test
    void testDeleteGPU() {
        // Given
        String cluster = "test-cluster";
        String namespace = "default";
        String name = "test-gpu";

        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.deleteGPU(cluster, namespace, name);
        });
    }

    @Test
    void testIsConnected() {
        // Given
        String cluster = "test-cluster";

        // When & Then
        assertFalse(repository.isConnected(cluster));
    }
}