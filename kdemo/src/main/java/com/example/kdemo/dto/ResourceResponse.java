package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cluster",
    "namespace",
    "count",
    "resources"
})
public class ResourceResponse<T> {
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("namespace")
    private String namespace;
    
    @JsonProperty("count")
    private int count;
    
    @JsonProperty("resources")
    private List<T> resources;
    
    public ResourceResponse() {}
    
    public ResourceResponse(String cluster, String namespace, List<T> resources) {
        this.cluster = cluster;
        this.namespace = namespace;
        this.resources = resources;
        this.count = resources != null ? resources.size() : 0;
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
    
    public List<T> getResources() {
        return resources;
    }
    
    public void setResources(List<T> resources) {
        this.resources = resources;
        this.count = resources != null ? resources.size() : 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceResponse<?> that = (ResourceResponse<?>) o;
        return count == that.count &&
                Objects.equals(cluster, that.cluster) &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(resources, that.resources);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cluster, namespace, count, resources);
    }
    
    @Override
    public String toString() {
        return "ResourceResponse{" +
                "cluster='" + cluster + '\'' +
                ", namespace='" + namespace + '\'' +
                ", count=" + count +
                ", resources=" + resources +
                '}';
    }
} 