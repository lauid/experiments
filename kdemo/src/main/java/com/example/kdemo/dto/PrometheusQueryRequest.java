package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Prometheus批量查询请求DTO
 */
public class PrometheusQueryRequest {
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("queries")
    private List<MetricQuery> queries;
    
    @JsonProperty("start_time")
    private String startTime;
    
    @JsonProperty("end_time")
    private String endTime;
    
    @JsonProperty("step")
    private String step;
    
    public PrometheusQueryRequest() {}
    
    public PrometheusQueryRequest(String cluster, List<MetricQuery> queries, String startTime, String endTime, String step) {
        this.cluster = cluster;
        this.queries = queries;
        this.startTime = startTime;
        this.endTime = endTime;
        this.step = step;
    }
    
    // Getters and Setters
    public String getCluster() {
        return cluster;
    }
    
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    public List<MetricQuery> getQueries() {
        return queries;
    }
    
    public void setQueries(List<MetricQuery> queries) {
        this.queries = queries;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public String getStep() {
        return step;
    }
    
    public void setStep(String step) {
        this.step = step;
    }
    
    /**
     * 单个指标查询
     */
    public static class MetricQuery {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("query")
        private String query;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("labels")
        private Map<String, String> labels;
        
        public MetricQuery() {}
        
        public MetricQuery(String name, String query, String description, Map<String, String> labels) {
            this.name = name;
            this.query = query;
            this.description = description;
            this.labels = labels;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getQuery() {
            return query;
        }
        
        public void setQuery(String query) {
            this.query = query;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public Map<String, String> getLabels() {
            return labels;
        }
        
        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }
    }
} 