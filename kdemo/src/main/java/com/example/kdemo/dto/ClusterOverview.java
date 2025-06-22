package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cluster",
    "namespaceCount",
    "totalPods",
    "podsPerNamespace",
    "namespaces"
})
public class ClusterOverview {
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("namespaceCount")
    private int namespaceCount;
    
    @JsonProperty("totalPods")
    private int totalPods;
    
    @JsonProperty("podsPerNamespace")
    private Map<String, Integer> podsPerNamespace;
    
    @JsonProperty("namespaces")
    private List<String> namespaces;
    
    public ClusterOverview() {}
    
    public ClusterOverview(String cluster, int namespaceCount, int totalPods, 
                          Map<String, Integer> podsPerNamespace, List<String> namespaces) {
        this.cluster = cluster;
        this.namespaceCount = namespaceCount;
        this.totalPods = totalPods;
        this.podsPerNamespace = podsPerNamespace;
        this.namespaces = namespaces;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    public int getNamespaceCount() {
        return namespaceCount;
    }
    
    public void setNamespaceCount(int namespaceCount) {
        this.namespaceCount = namespaceCount;
    }
    
    public int getTotalPods() {
        return totalPods;
    }
    
    public void setTotalPods(int totalPods) {
        this.totalPods = totalPods;
    }
    
    public Map<String, Integer> getPodsPerNamespace() {
        return podsPerNamespace;
    }
    
    public void setPodsPerNamespace(Map<String, Integer> podsPerNamespace) {
        this.podsPerNamespace = podsPerNamespace;
    }
    
    public List<String> getNamespaces() {
        return namespaces;
    }
    
    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }
} 