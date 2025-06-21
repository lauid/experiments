package com.example.kdemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MicroserviceSpec {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("image")
    private String image;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("replicas")
    private Integer replicas = 1;
    
    @JsonProperty("resources")
    private Map<String, Object> resources;
    
    @JsonProperty("ports")
    private List<Map<String, Object>> ports;
    
    @JsonProperty("environment")
    private List<Map<String, Object>> environment;
    
    @JsonProperty("configMaps")
    private List<Map<String, Object>> configMaps;
    
    @JsonProperty("secrets")
    private List<Map<String, Object>> secrets;
    
    @JsonProperty("healthCheck")
    private Map<String, Object> healthCheck;
    
    @JsonProperty("networking")
    private Map<String, Object> networking;
    
    // Constructors
    public MicroserviceSpec() {}
    
    public MicroserviceSpec(String name, String image) {
        this.name = name;
        this.image = image;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
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
    
    public Map<String, Object> getResources() {
        return resources;
    }
    
    public void setResources(Map<String, Object> resources) {
        this.resources = resources;
    }
    
    public List<Map<String, Object>> getPorts() {
        return ports;
    }
    
    public void setPorts(List<Map<String, Object>> ports) {
        this.ports = ports;
    }
    
    public List<Map<String, Object>> getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(List<Map<String, Object>> environment) {
        this.environment = environment;
    }
    
    public List<Map<String, Object>> getConfigMaps() {
        return configMaps;
    }
    
    public void setConfigMaps(List<Map<String, Object>> configMaps) {
        this.configMaps = configMaps;
    }
    
    public List<Map<String, Object>> getSecrets() {
        return secrets;
    }
    
    public void setSecrets(List<Map<String, Object>> secrets) {
        this.secrets = secrets;
    }
    
    public Map<String, Object> getHealthCheck() {
        return healthCheck;
    }
    
    public void setHealthCheck(Map<String, Object> healthCheck) {
        this.healthCheck = healthCheck;
    }
    
    public Map<String, Object> getNetworking() {
        return networking;
    }
    
    public void setNetworking(Map<String, Object> networking) {
        this.networking = networking;
    }
} 