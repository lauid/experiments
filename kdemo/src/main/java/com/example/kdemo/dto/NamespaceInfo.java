package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cluster",
    "count",
    "namespaces"
})
public class NamespaceInfo {
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("count")
    private int count;
    
    @JsonProperty("namespaces")
    private List<String> namespaces;
    
    public NamespaceInfo() {}
    
    public NamespaceInfo(String cluster, List<String> namespaces) {
        this.cluster = cluster;
        this.namespaces = namespaces;
        this.count = namespaces != null ? namespaces.size() : 0;
    }
    
    public String getCluster() {
        return cluster;
    }
    
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public List<String> getNamespaces() {
        return namespaces;
    }
    
    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
        this.count = namespaces != null ? namespaces.size() : 0;
    }
} 