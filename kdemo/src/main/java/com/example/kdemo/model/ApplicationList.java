package com.example.kdemo.model;

import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.models.V1ListMeta;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationList implements KubernetesListObject {
    
    @JsonProperty("apiVersion")
    private String apiVersion = "example.com/v1";
    
    @JsonProperty("kind")
    private String kind = "ApplicationList";
    
    @JsonProperty("metadata")
    private V1ListMeta metadata;
    
    @JsonProperty("items")
    private List<Application> items;
    
    // Constructors
    public ApplicationList() {}
    
    public ApplicationList(List<Application> items) {
        this.items = items;
    }
    
    // Getters and Setters
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
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
    
    public List<Application> getItems() {
        return items;
    }
    
    public void setItems(List<Application> items) {
        this.items = items;
    }
} 