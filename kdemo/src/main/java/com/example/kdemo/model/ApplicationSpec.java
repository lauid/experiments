package com.example.kdemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationSpec {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("replicas")
    private Integer replicas;
    
    // Constructors
    public ApplicationSpec() {}
    
    public ApplicationSpec(String name, String version, Integer replicas) {
        this.name = name;
        this.version = version;
        this.replicas = replicas;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Integer getReplicas() {
        return replicas;
    }
    
    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }
} 