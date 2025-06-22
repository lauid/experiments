package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;

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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterInfo that = (ClusterInfo) o;
        return connected == that.connected &&
                Objects.equals(cluster, that.cluster);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(connected, cluster);
    }
    
    @Override
    public String toString() {
        return "ClusterInfo{" +
                "connected=" + connected +
                ", cluster='" + cluster + '\'' +
                '}';
    }
} 