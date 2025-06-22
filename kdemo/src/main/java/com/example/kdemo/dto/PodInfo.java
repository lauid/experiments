package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cluster",
    "namespace",
    "count",
    "pods"
})
public class PodInfo {
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("count")
    private int count;
    
    @JsonProperty("pods")
    private List<String> pods;
    
    public PodInfo() {}
    
    public PodInfo(String cluster, String namespace, List<String> pods) {
        this.cluster = cluster;
        this.namespace = namespace;
        this.pods = pods;
        this.count = pods != null ? pods.size() : 0;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public List<String> getPods() {
        return pods;
    }
    
    public void setPods(List<String> pods) {
        this.pods = pods;
        this.count = pods != null ? pods.size() : 0;
    }
} 