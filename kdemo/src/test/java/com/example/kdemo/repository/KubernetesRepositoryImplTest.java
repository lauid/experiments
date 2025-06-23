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
import static org.mockito.Mockito.*;
import com.example.kdemo.config.KubernetesConfig;
import com.example.kdemo.service.ClusterService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KubernetesRepositoryImplTest {

    private KubernetesRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:1234");
        KubernetesConfig k8sConfig = mock(KubernetesConfig.class);
        repository = new KubernetesRepositoryImpl(client, k8sConfig);
    }

    @Test
    void testGetNamespaces() {
        assertThrows(KubernetesException.class, () -> {
            repository.getNamespaces();
        });
    }

    @Test
    void testGetPodsInNamespace() {
        assertThrows(KubernetesException.class, () -> {
            repository.getPodsInNamespace();
        });
    }

    @Test
    void testGetCustomResourceDefinitions() {
        assertThrows(KubernetesException.class, () -> {
            repository.getCustomResourceDefinitions();
        });
    }

    @Test
    void testGetCustomResourceDefinition() {
        String name = "applications.example.com";
        assertThrows(ResourceNotFoundException.class, () -> {
            repository.getCustomResourceDefinition(name);
        });
    }

    @Test
    void testCreateCustomResourceDefinition() {
        String crdYaml = "apiVersion: apiextensions.k8s.io/v1\nkind: CustomResourceDefinition";
        assertThrows(KubernetesException.class, () -> {
            repository.createCustomResourceDefinition(crdYaml);
        });
    }

    @Test
    void testGetApplications() {
        assertThrows(KubernetesException.class, () -> {
            repository.getApplications();
        });
    }

    @Test
    void testGetApplication() {
        String name = "test-app";
        assertThrows(KubernetesException.class, () -> {
            repository.getApplication(name);
        });
    }

    @Test
    void testCreateApplication() {
        Application application = new Application();
        assertThrows(KubernetesException.class, () -> {
            repository.createApplication(application);
        });
    }

    @Test
    void testUpdateApplication() {
        String name = "test-app";
        Application application = new Application();
        assertThrows(KubernetesException.class, () -> {
            repository.updateApplication(name, application);
        });
    }

    @Test
    void testDeleteApplication() {
        String name = "test-app";
        assertThrows(KubernetesException.class, () -> {
            repository.deleteApplication(name);
        });
    }

    @Test
    void testGetMicroservices() {
        assertThrows(KubernetesException.class, () -> {
            repository.getMicroservices();
        });
    }

    @Test
    void testGetMicroservice() {
        String name = "test-svc";
        assertThrows(KubernetesException.class, () -> {
            repository.getMicroservice(name);
        });
    }

    @Test
    void testCreateMicroservice() {
        Microservice microservice = new Microservice();
        assertThrows(KubernetesException.class, () -> {
            repository.createMicroservice(microservice);
        });
    }

    @Test
    void testUpdateMicroservice() {
        String name = "test-svc";
        Microservice microservice = new Microservice();
        assertThrows(KubernetesException.class, () -> {
            repository.updateMicroservice(name, microservice);
        });
    }

    @Test
    void testDeleteMicroservice() {
        String name = "test-svc";
        assertThrows(KubernetesException.class, () -> {
            repository.deleteMicroservice(name);
        });
    }

    @Test
    void testGetGPUs() {
        assertThrows(KubernetesException.class, () -> {
            repository.getGPUs();
        });
    }

    @Test
    void testGetGPU() {
        String name = "test-gpu";
        assertThrows(KubernetesException.class, () -> {
            repository.getGPU(name);
        });
    }

    @Test
    void testCreateGPU() {
        GPU gpu = new GPU();
        assertThrows(KubernetesException.class, () -> {
            repository.createGPU(gpu);
        });
    }

    @Test
    void testUpdateGPU() {
        String name = "test-gpu";
        GPU gpu = new GPU();
        assertThrows(KubernetesException.class, () -> {
            repository.updateGPU(name, gpu);
        });
    }

    @Test
    void testDeleteGPU() {
        String name = "test-gpu";
        assertThrows(KubernetesException.class, () -> {
            repository.deleteGPU(name);
        });
    }

    @Test
    void testIsConnected() {
        assertThrows(Exception.class, () -> {
            repository.isConnected();
        });
    }
}