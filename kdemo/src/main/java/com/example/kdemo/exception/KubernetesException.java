package com.example.kdemo.exception;

public class KubernetesException extends RuntimeException {
    
    private final String cluster;
    private final String operation;
    
    public KubernetesException(String message, String cluster, String operation) {
        super(message);
        this.cluster = cluster;
        this.operation = operation;
    }
    
    public KubernetesException(String message, String cluster, String operation, Throwable cause) {
        super(message, cause);
        this.cluster = cluster;
        this.operation = operation;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public String getOperation() {
        return operation;
    }
} 