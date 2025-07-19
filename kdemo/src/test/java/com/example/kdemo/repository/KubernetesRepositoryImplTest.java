package com.example.kdemo.repository;

import com.example.kdemo.model.Application;
import com.example.kdemo.model.Microservice;
import com.example.kdemo.model.GPU;
import com.example.kdemo.exception.KubernetesException;
import com.example.kdemo.exception.ResourceNotFoundException;
import com.example.kdemo.model.GPUSpec;
import com.example.kdemo.model.Vendor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.kubernetes.client.openapi.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class KubernetesRepositoryImplTest {

    private KubernetesRepositoryImpl repository;
    private ApiClient client;

    @BeforeEach
    void setUp() {
        repository = new KubernetesRepositoryImpl();
        client = new ApiClient();
        client.setBasePath("http://localhost:1234");
    }

    @Test
    void testGetNamespaces() {
        // When & Then
        assertThrows(KubernetesException.class, () -> {
            repository.getNamespaces(client);
        });
    }

    @Test
    void testGetPodsInNamespace() {
        String namespace = "default";
        assertThrows(KubernetesException.class, () -> {
            repository.getPodsInNamespace(client, namespace);
        });
    }

    @Test
    void testGetCustomResourceDefinitions() {
        assertThrows(KubernetesException.class, () -> {
            repository.getCustomResourceDefinitions(client);
        });
    }

    @Test
    void testGetCustomResourceDefinition() {
        String name = "applications.example.com";
        assertThrows(ResourceNotFoundException.class, () -> {
            repository.getCustomResourceDefinition(client, name);
        });
    }

    @Test
    void testCreateCustomResourceDefinition() {
        String crdYaml = "apiVersion: apiextensions.k8s.io/v1\nkind: CustomResourceDefinition";
        assertThrows(KubernetesException.class, () -> {
            repository.createCustomResourceDefinition(client, crdYaml);
        });
    }

    @Test
    void testGetApplications() {
        String namespace = "default";
        assertThrows(KubernetesException.class, () -> {
            repository.getApplications(client, namespace);
        });
    }

    @Test
    void testGetApplication() {
        String namespace = "default";
        String name = "test-app";
        assertThrows(KubernetesException.class, () -> {
            repository.getApplication(client, namespace, name);
        });
    }

    @Test
    void testCreateApplication() {
        String namespace = "default";
        Application application = new Application();
        assertThrows(KubernetesException.class, () -> {
            repository.createApplication(client, namespace, application);
        });
    }

    @Test
    void testUpdateApplication() {
        String namespace = "default";
        String name = "test-app";
        Application application = new Application();
        assertThrows(KubernetesException.class, () -> {
            repository.updateApplication(client, namespace, name, application);
        });
    }

    @Test
    void testDeleteApplication() {
        String namespace = "default";
        String name = "test-app";
        assertThrows(KubernetesException.class, () -> {
            repository.deleteApplication(client, namespace, name);
        });
    }

    @Test
    void testGetMicroservices() {
        String namespace = "default";
        assertThrows(KubernetesException.class, () -> {
            repository.getMicroservices(client, namespace);
        });
    }

    @Test
    void testGetMicroservice() {
        String namespace = "default";
        String name = "test-svc";
        assertThrows(KubernetesException.class, () -> {
            repository.getMicroservice(client, namespace, name);
        });
    }

    @Test
    void testCreateMicroservice() {
        String namespace = "default";
        Microservice microservice = new Microservice();
        assertThrows(KubernetesException.class, () -> {
            repository.createMicroservice(client, namespace, microservice);
        });
    }

    @Test
    void testUpdateMicroservice() {
        String namespace = "default";
        String name = "test-svc";
        Microservice microservice = new Microservice();
        assertThrows(KubernetesException.class, () -> {
            repository.updateMicroservice(client, namespace, name, microservice);
        });
    }

    @Test
    void testDeleteMicroservice() {
        String namespace = "default";
        String name = "test-svc";
        assertThrows(KubernetesException.class, () -> {
            repository.deleteMicroservice(client, namespace, name);
        });
    }

    @Test
    void testGetGPUs() {
        String namespace = "default";
        assertThrows(KubernetesException.class, () -> {
            repository.getGPUs(client, namespace);
        });
    }

    @Test
    void testGetGPU() {
        String namespace = "default";
        String name = "test-gpu";
        assertThrows(KubernetesException.class, () -> {
            repository.getGPU(client, namespace, name);
        });
    }

    @Test
    void testCreateGPU() {
        String namespace = "default";
        GPU gpu = new GPU();
        assertThrows(KubernetesException.class, () -> {
            repository.createGPU(client, namespace, gpu);
        });
    }

    @Test
    void testUpdateGPU() {
        String namespace = "default";
        String name = "test-gpu";
        GPU gpu = new GPU();
        assertThrows(KubernetesException.class, () -> {
            repository.updateGPU(client, namespace, name, gpu);
        });
    }

    @Test
    void testDeleteGPU() {
        String namespace = "default";
        String name = "test-gpu";
        assertThrows(KubernetesException.class, () -> {
            repository.deleteGPU(client, namespace, name);
        });
    }

    @Test
    void testIsConnected() {
        // When & Then
        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:1234");
        assertFalse(repository.isConnected(client));
    }
}