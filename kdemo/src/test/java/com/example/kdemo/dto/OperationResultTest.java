package com.example.kdemo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationResultTest {

    @Test
    void testSuccess() {
        // When
        OperationResult result = OperationResult.success("test-cluster", "test-resource", "Operation completed");

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("test-cluster", result.getCluster());
        assertEquals("test-resource", result.getName());
        assertEquals("Operation completed", result.getMessage());
        assertNull(result.getError());
    }

    @Test
    void testFailure() {
        // When
        OperationResult result = OperationResult.failure("test-cluster", "test-resource", "Operation failed", "Error details");

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("test-cluster", result.getCluster());
        assertEquals("test-resource", result.getName());
        assertEquals("Operation failed", result.getMessage());
        assertEquals("Error details", result.getError());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        OperationResult result = new OperationResult();

        // When
        result.setSuccess(true);
        result.setCluster("cluster1");
        result.setName("resource1");
        result.setMessage("test message");
        result.setError("test error");

        // Then
        assertTrue(result.isSuccess());
        assertEquals("cluster1", result.getCluster());
        assertEquals("resource1", result.getName());
        assertEquals("test message", result.getMessage());
        assertEquals("test error", result.getError());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        OperationResult result1 = OperationResult.success("cluster1", "resource1", "message1");
        OperationResult result2 = OperationResult.success("cluster1", "resource1", "message1");
        OperationResult result3 = OperationResult.success("cluster2", "resource1", "message1");

        // Then
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }

    @Test
    void testToString() {
        // Given
        OperationResult result = OperationResult.success("test-cluster", "test-resource", "test message");

        // When
        String toString = result.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("test-cluster"));
        assertTrue(toString.contains("test-resource"));
        assertTrue(toString.contains("test message"));
    }
} 