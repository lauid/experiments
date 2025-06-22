package com.example.kdemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GPUSpec {
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("memory")
    private Map<String, String> memory;
    
    @JsonProperty("computeCapability")
    private String computeCapability;
    
    @JsonProperty("architecture")
    private String architecture;
    
    @JsonProperty("powerLimit")
    private Map<String, Integer> powerLimit;
    
    @JsonProperty("temperature")
    private Map<String, Integer> temperature;
    
    @JsonProperty("utilization")
    private Map<String, Integer> utilization;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("nodeName")
    private String nodeName;
    
    @JsonProperty("driverVersion")
    private String driverVersion;
    
    @JsonProperty("cudaVersion")
    private String cudaVersion;
    
    @JsonProperty("processes")
    private List<Map<String, Object>> processes;
    
    // Constructors
    public GPUSpec() {}
    
    public GPUSpec(String model, String status, String nodeName) {
        this.model = model;
        this.status = status;
        this.nodeName = nodeName;
    }
    
    // Getters and Setters
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Map<String, String> getMemory() {
        return memory;
    }
    
    public void setMemory(Map<String, String> memory) {
        this.memory = memory;
    }
    
    public String getComputeCapability() {
        return computeCapability;
    }
    
    public void setComputeCapability(String computeCapability) {
        this.computeCapability = computeCapability;
    }
    
    public String getArchitecture() {
        return architecture;
    }
    
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }
    
    public Map<String, Integer> getPowerLimit() {
        return powerLimit;
    }
    
    public void setPowerLimit(Map<String, Integer> powerLimit) {
        this.powerLimit = powerLimit;
    }
    
    public Map<String, Integer> getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Map<String, Integer> temperature) {
        this.temperature = temperature;
    }
    
    public Map<String, Integer> getUtilization() {
        return utilization;
    }
    
    public void setUtilization(Map<String, Integer> utilization) {
        this.utilization = utilization;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public String getDriverVersion() {
        return driverVersion;
    }
    
    public void setDriverVersion(String driverVersion) {
        this.driverVersion = driverVersion;
    }
    
    public String getCudaVersion() {
        return cudaVersion;
    }
    
    public void setCudaVersion(String cudaVersion) {
        this.cudaVersion = cudaVersion;
    }
    
    public List<Map<String, Object>> getProcesses() {
        return processes;
    }
    
    public void setProcesses(List<Map<String, Object>> processes) {
        this.processes = processes;
    }
} 