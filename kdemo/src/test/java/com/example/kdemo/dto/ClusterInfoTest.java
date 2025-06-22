package com.example.kdemo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClusterInfoTest {

    @Test
    void testConstructor() {
        // When
        ClusterInfo clusterInfo = new ClusterInfo(true, "test-cluster");

        // Then
        assertTrue(clusterInfo.isConnected());
        assertEquals("test-cluster", clusterInfo.getCluster());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        ClusterInfo clusterInfo = new ClusterInfo();

        // When
        clusterInfo.setConnected(false);
        clusterInfo.setCluster("new-cluster");

        // Then
        assertFalse(clusterInfo.isConnected());
        assertEquals("new-cluster", clusterInfo.getCluster());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        ClusterInfo info1 = new ClusterInfo(true, "cluster1");
        ClusterInfo info2 = new ClusterInfo(true, "cluster1");
        ClusterInfo info3 = new ClusterInfo(false, "cluster1");

        // Then
        assertEquals(info1, info2);
        assertNotEquals(info1, info3);
        assertEquals(info1.hashCode(), info2.hashCode());
        assertNotEquals(info1.hashCode(), info3.hashCode());
    }

    @Test
    void testToString() {
        // Given
        ClusterInfo clusterInfo = new ClusterInfo(true, "test-cluster");

        // When
        String toString = clusterInfo.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("test-cluster"));
        assertTrue(toString.contains("true"));
    }
} 