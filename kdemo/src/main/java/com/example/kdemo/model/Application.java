package com.example.kdemo.model;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.common.KubernetesObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "spec",
    "status"
})
public class Application implements KubernetesObject {
    
    @JsonProperty("apiVersion")
    private String apiVersion = "example.com/v1";
    
    @JsonProperty("kind")
    private String kind = "Application";
    
    @JsonProperty("metadata")
    private V1ObjectMeta metadata;
    
    @JsonProperty("spec")
    private ApplicationSpec spec;
    
    @JsonProperty("status")
    private ApplicationStatus status;
    
    // Constructors
    public Application() {}
    
    public Application(V1ObjectMeta metadata, ApplicationSpec spec) {
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
    
    public ApplicationSpec getSpec() {
        return spec;
    }
    
    public void setSpec(ApplicationSpec spec) {
        this.spec = spec;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(kind, that.kind) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(spec, that.spec) &&
                Objects.equals(status, that.status);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(apiVersion, kind, metadata, spec, status);
    }
    
    @Override
    public String toString() {
        return "Application{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                ", status=" + status +
                '}';
    }
} 