package com.example.kdemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MicroserviceStatus {
    
    @JsonProperty("phase")
    private String phase;
    
    @JsonProperty("replicas")
    private Integer replicas;
    
    @JsonProperty("availableReplicas")
    private Integer availableReplicas;
    
    @JsonProperty("readyReplicas")
    private Integer readyReplicas;
    
    @JsonProperty("conditions")
    private List<Map<String, Object>> conditions;
    
    @JsonProperty("endpoints")
    private List<Map<String, Object>> endpoints;
    
    // Constructors
    public MicroserviceStatus() {}
    
    public MicroserviceStatus(String phase) {
        this.phase = phase;
    }
    
    // Getters and Setters
    public String getPhase() {
        return phase;
    }
    
    public void setPhase(String phase) {
        this.phase = phase;
    }
    
    public Integer getReplicas() {
        return replicas;
    }
    
    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }
    
    public Integer getAvailableReplicas() {
        return availableReplicas;
    }
    
    public void setAvailableReplicas(Integer availableReplicas) {
        this.availableReplicas = availableReplicas;
    }
    
    public Integer getReadyReplicas() {
        return readyReplicas;
    }
    
    public void setReadyReplicas(Integer readyReplicas) {
        this.readyReplicas = readyReplicas;
    }
    
    public List<Map<String, Object>> getConditions() {
        return conditions;
    }
    
    public void setConditions(List<Map<String, Object>> conditions) {
        this.conditions = conditions;
    }
    
    public List<Map<String, Object>> getEndpoints() {
        return endpoints;
    }
    
    public void setEndpoints(List<Map<String, Object>> endpoints) {
        this.endpoints = endpoints;
    }
} 