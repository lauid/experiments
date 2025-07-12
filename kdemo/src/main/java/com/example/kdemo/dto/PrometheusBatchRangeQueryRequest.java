package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Prometheus批量范围查询请求DTO
 */
public class PrometheusBatchRangeQueryRequest {
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("queries")
    private List<RangeMetricQuery> queries;
    
    @JsonProperty("start_time")
    private Long startTime; // 时间戳（秒）
    
    @JsonProperty("end_time")
    private Long endTime; // 时间戳（秒）
    
    @JsonProperty("step")
    private String step;
    
    public PrometheusBatchRangeQueryRequest() {}
    
    public PrometheusBatchRangeQueryRequest(String cluster, List<RangeMetricQuery> queries, Long startTime, Long endTime, String step) {
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
    
    public List<RangeMetricQuery> getQueries() {
        return queries;
    }
    
    public void setQueries(List<RangeMetricQuery> queries) {
        this.queries = queries;
    }
    
    public Long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    
    public Long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    
    public String getStep() {
        return step;
    }
    
    public void setStep(String step) {
        this.step = step;
    }
    
    /**
     * 单个范围指标查询
     */
    public static class RangeMetricQuery {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("query")
        private String query;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("labels")
        private Map<String, String> labels;
        
        @JsonProperty("start_time")
        private Long startTime; // 时间戳（秒）
        
        @JsonProperty("end_time")
        private Long endTime; // 时间戳（秒）
        
        @JsonProperty("step")
        private String step;
        
        public RangeMetricQuery() {}
        
        public RangeMetricQuery(String name, String query, String description, Map<String, String> labels) {
            this.name = name;
            this.query = query;
            this.description = description;
            this.labels = labels;
        }
        
        public RangeMetricQuery(String name, String query, String description, Map<String, String> labels, 
                               Long startTime, Long endTime, String step) {
            this.name = name;
            this.query = query;
            this.description = description;
            this.labels = labels;
            this.startTime = startTime;
            this.endTime = endTime;
            this.step = step;
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
        
        public Long getStartTime() {
            return startTime;
        }
        
        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }
        
        public Long getEndTime() {
            return endTime;
        }
        
        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }
        
        public String getStep() {
            return step;
        }
        
        public void setStep(String step) {
            this.step = step;
        }
    }
} 