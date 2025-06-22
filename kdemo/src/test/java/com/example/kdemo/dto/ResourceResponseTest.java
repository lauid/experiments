package com.example.kdemo.dto;

import com.example.kdemo.model.Application;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceResponseTest {

    @Test
    void testConstructor() {
        // Given
        Application app1 = new Application();
        V1ObjectMeta meta1 = new V1ObjectMeta();
        meta1.setName("app1");
        app1.setMetadata(meta1);

        Application app2 = new Application();
        V1ObjectMeta meta2 = new V1ObjectMeta();
        meta2.setName("app2");
        app2.setMetadata(meta2);

        List<Application> resources = Arrays.asList(app1, app2);

        // When
        ResourceResponse<Application> response = new ResourceResponse<>("test-cluster", "default", resources);

        // Then
        assertEquals("test-cluster", response.getCluster());
        assertEquals("default", response.getNamespace());
        assertEquals(2, response.getCount());
        assertEquals(resources, response.getResources());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        ResourceResponse<Application> response = new ResourceResponse<>();

        // When
        response.setCluster("new-cluster");
        response.setNamespace("new-namespace");
        response.setResources(Arrays.asList(new Application()));

        // Then
        assertEquals("new-cluster", response.getCluster());
        assertEquals("new-namespace", response.getNamespace());
        assertEquals(1, response.getCount());
        assertEquals(1, response.getResources().size());
    }

    @Test
    void testEmptyResources() {
        // When
        ResourceResponse<Application> response = new ResourceResponse<>("test-cluster", "default", Arrays.asList());

        // Then
        assertEquals(0, response.getCount());
        assertTrue(response.getResources().isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        ResourceResponse<Application> response1 = new ResourceResponse<>("cluster1", "ns1", Arrays.asList());
        ResourceResponse<Application> response2 = new ResourceResponse<>("cluster1", "ns1", Arrays.asList());
        ResourceResponse<Application> response3 = new ResourceResponse<>("cluster2", "ns1", Arrays.asList());

        // Then
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testToString() {
        // Given
        ResourceResponse<Application> response = new ResourceResponse<>("test-cluster", "default", Arrays.asList());

        // When
        String toString = response.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("test-cluster"));
        assertTrue(toString.contains("default"));
    }
} 