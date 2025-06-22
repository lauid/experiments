package com.example.kdemo.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    private final String cluster;
    private final String resourceType;
    private final String resourceName;
    private final String namespace;
    
    public ResourceNotFoundException(String resourceType, String resourceName, String cluster) {
        super(String.format("Resource %s '%s' not found in cluster '%s'", resourceType, resourceName, cluster));
        this.cluster = cluster;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.namespace = null;
    }
    
    public ResourceNotFoundException(String resourceType, String resourceName, String namespace, String cluster) {
        super(String.format("Resource %s '%s' not found in namespace '%s' of cluster '%s'", 
                           resourceType, resourceName, namespace, cluster));
        this.cluster = cluster;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.namespace = namespace;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public String getNamespace() {
        return namespace;
    }
} 