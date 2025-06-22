package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cluster",
    "count",
    "crds"
})
public class CrdInfo {
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("count")
    private int count;
    
    @JsonProperty("crds")
    private List<String> crds;
    
    public CrdInfo() {}
    
    public CrdInfo(String cluster, List<String> crds) {
        this.cluster = cluster;
        this.crds = crds;
        this.count = crds != null ? crds.size() : 0;
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
    
    public List<String> getCrds() {
        return crds;
    }
    
    public void setCrds(List<String> crds) {
        this.crds = crds;
        this.count = crds != null ? crds.size() : 0;
    }
} 