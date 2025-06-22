package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "connected",
    "cluster"
})
public class ClusterInfo {
    
    @JsonProperty("connected")
    private boolean connected;
    
    @JsonProperty("cluster")
    private String cluster;
    
    public ClusterInfo() {}
    
    public ClusterInfo(boolean connected, String cluster) {
        this.connected = connected;
        this.cluster = cluster;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
} 