package com.example.kdemo.model;

import io.kubernetes.client.openapi.models.V1ListMeta;
import io.kubernetes.client.common.KubernetesListObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "items"
})
public class GPUList implements KubernetesListObject {
    
    @JsonProperty("apiVersion")
    private String apiVersion = "example.com/v1";
    
    @JsonProperty("kind")
    private String kind = "GPUList";
    
    @JsonProperty("metadata")
    private V1ListMeta metadata;
    
    @JsonProperty("items")
    private List<GPU> items;
    
    // Constructors
    public GPUList() {}
    
    public GPUList(List<GPU> items) {
        this.items = items;
    }
    
    // Getters and Setters
    @Override
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    @Override
    public String getKind() {
        return kind;
    }
    
    public void setKind(String kind) {
        this.kind = kind;
    }
    
    @Override
    public V1ListMeta getMetadata() {
        return metadata;
    }
    
    public void setMetadata(V1ListMeta metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public List<GPU> getItems() {
        return items;
    }
    
    public void setItems(List<GPU> items) {
        this.items = items;
    }
} 