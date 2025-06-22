package com.example.kdemo.model;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.common.KubernetesObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "spec",
    "status"
})
public class GPU implements KubernetesObject {
    
    @JsonProperty("apiVersion")
    private String apiVersion = "example.com/v1";
    
    @JsonProperty("kind")
    private String kind = "GPU";
    
    @JsonProperty("metadata")
    private V1ObjectMeta metadata;
    
    @JsonProperty("spec")
    private GPUSpec spec;
    
    @JsonProperty("status")
    private GPUStatus status;
    
    // Constructors
    public GPU() {}
    
    public GPU(V1ObjectMeta metadata, GPUSpec spec) {
        this.metadata = metadata;
        this.spec = spec;
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
    public V1ObjectMeta getMetadata() {
        return metadata;
    }
    
    public void setMetadata(V1ObjectMeta metadata) {
        this.metadata = metadata;
    }
    
    public GPUSpec getSpec() {
        return spec;
    }
    
    public void setSpec(GPUSpec spec) {
        this.spec = spec;
    }
    
    public GPUStatus getStatus() {
        return status;
    }
    
    public void setStatus(GPUStatus status) {
        this.status = status;
    }
} 