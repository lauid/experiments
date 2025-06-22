package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "success",
    "cluster",
    "name",
    "message",
    "error"
})
public class OperationResult {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("error")
    private String error;
    
    public OperationResult() {}
    
    public OperationResult(boolean success, String cluster, String name, String message) {
        this.success = success;
        this.cluster = cluster;
        this.name = name;
        this.message = message;
    }
    
    public OperationResult(boolean success, String cluster, String name, String message, String error) {
        this.success = success;
        this.cluster = cluster;
        this.name = name;
        this.message = message;
        this.error = error;
    }
    
    public static OperationResult success(String cluster, String name, String message) {
        return new OperationResult(true, cluster, name, message);
    }
    
    public static OperationResult failure(String cluster, String name, String message, String error) {
        return new OperationResult(false, cluster, name, message, error);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
} 