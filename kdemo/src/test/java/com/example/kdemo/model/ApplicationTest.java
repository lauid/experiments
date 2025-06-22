package com.example.kdemo.model;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {

    @Test
    void testDefaultConstructor() {
        // When
        Application application = new Application();

        // Then
        assertNotNull(application);
        assertEquals("example.com/v1", application.getApiVersion());
        assertEquals("Application", application.getKind());
        assertNull(application.getMetadata());
        assertNull(application.getSpec());
        assertNull(application.getStatus());
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName("test-app");
        ApplicationSpec spec = new ApplicationSpec();
        spec.setName("test-app");

        // When
        Application application = new Application(metadata, spec);

        // Then
        assertNotNull(application);
        assertEquals(metadata, application.getMetadata());
        assertEquals(spec, application.getSpec());
        assertEquals("test-app", application.getMetadata().getName());
        assertEquals("test-app", application.getSpec().getName());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Application application = new Application();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName("test-app");
        ApplicationSpec spec = new ApplicationSpec();
        spec.setName("test-app");
        ApplicationStatus status = new ApplicationStatus();
        status.setPhase("Running");

        // When
        application.setApiVersion("custom.com/v2");
        application.setKind("CustomApplication");
        application.setMetadata(metadata);
        application.setSpec(spec);
        application.setStatus(status);

        // Then
        assertEquals("custom.com/v2", application.getApiVersion());
        assertEquals("CustomApplication", application.getKind());
        assertEquals(metadata, application.getMetadata());
        assertEquals(spec, application.getSpec());
        assertEquals(status, application.getStatus());
    }

    @Test
    void testKubernetesObjectInterface() {
        // Given
        Application application = new Application();

        // When & Then
        assertTrue(application instanceof io.kubernetes.client.common.KubernetesObject);
        assertEquals("example.com/v1", application.getApiVersion());
        assertEquals("Application", application.getKind());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        V1ObjectMeta metadata1 = new V1ObjectMeta();
        metadata1.setName("app1");
        ApplicationSpec spec1 = new ApplicationSpec();
        spec1.setName("app1");

        V1ObjectMeta metadata2 = new V1ObjectMeta();
        metadata2.setName("app1");
        ApplicationSpec spec2 = new ApplicationSpec();
        spec2.setName("app1");

        Application app1 = new Application(metadata1, spec1);
        Application app2 = new Application(metadata2, spec2);
        Application app3 = new Application();

        // Then
        assertEquals(app1, app2);
        assertNotEquals(app1, app3);
        assertEquals(app1.hashCode(), app2.hashCode());
        assertNotEquals(app1.hashCode(), app3.hashCode());
    }

    @Test
    void testToString() {
        // Given
        Application application = new Application();
        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName("test-app");
        application.setMetadata(metadata);

        // When
        String toString = application.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("test-app"));
        assertTrue(toString.contains("Application"));
    }
} 