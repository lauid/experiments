package com.example.kdemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GPUStatus {
    
    @JsonProperty("phase")
    private String phase;
    
    @JsonProperty("lastUpdated")
    private String lastUpdated;
    
    // Constructors
    public GPUStatus() {}
    
    public GPUStatus(String phase) {
        this.phase = phase;
    }
    
    // Getters and Setters
    public String getPhase() {
        return phase;
    }
    
    public void setPhase(String phase) {
        this.phase = phase;
    }
    
    public String getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
} 