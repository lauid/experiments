package com.example.kdemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationStatus {
    
    @JsonProperty("phase")
    private String phase;
    
    // Constructors
    public ApplicationStatus() {}
    
    public ApplicationStatus(String phase) {
        this.phase = phase;
    }
    
    // Getters and Setters
    public String getPhase() {
        return phase;
    }
    
    public void setPhase(String phase) {
        this.phase = phase;
    }
} 