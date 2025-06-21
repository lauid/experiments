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
public class Microservice implements KubernetesObject {
    
    @JsonProperty("apiVersion")
    private String apiVersion = "example.com/v1";
    
    @JsonProperty("kind")
    private String kind = "Microservice";
    
    @JsonProperty("metadata")
    private V1ObjectMeta metadata;
    
    @JsonProperty("spec")
    private MicroserviceSpec spec;
    
    @JsonProperty("status")
    private MicroserviceStatus status;
    
    // Constructors
    public Microservice() {}
    
    public Microservice(V1ObjectMeta metadata, MicroserviceSpec spec) {
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
    
    public MicroserviceSpec getSpec() {
        return spec;
    }
    
    public void setSpec(MicroserviceSpec spec) {
        this.spec = spec;
    }
    
    public MicroserviceStatus getStatus() {
        return status;
    }
    
    public void setStatus(MicroserviceStatus status) {
        this.status = status;
    }
} 