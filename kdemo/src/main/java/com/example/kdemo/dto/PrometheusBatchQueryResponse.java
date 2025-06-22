package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Prometheus批量查询响应DTO
 */
public class PrometheusBatchQueryResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("data")
    private List<MetricResult> data;
    
    @JsonProperty("errors")
    private List<QueryError> errors;
    
    @JsonProperty("total_queries")
    private int totalQueries;
    
    @JsonProperty("successful_queries")
    private int successfulQueries;
    
    @JsonProperty("failed_queries")
    private int failedQueries;
    
    public PrometheusBatchQueryResponse() {}
    
    public PrometheusBatchQueryResponse(String status, List<MetricResult> data, List<QueryError> errors) {
        this.status = status;
        this.data = data;
        this.errors = errors;
        this.totalQueries = (data != null ? data.size() : 0) + (errors != null ? errors.size() : 0);
        this.successfulQueries = data != null ? data.size() : 0;
        this.failedQueries = errors != null ? errors.size() : 0;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<MetricResult> getData() {
        return data;
    }
    
    public void setData(List<MetricResult> data) {
        this.data = data;
    }
    
    public List<QueryError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<QueryError> errors) {
        this.errors = errors;
    }
    
    public int getTotalQueries() {
        return totalQueries;
    }
    
    public void setTotalQueries(int totalQueries) {
        this.totalQueries = totalQueries;
    }
    
    public int getSuccessfulQueries() {
        return successfulQueries;
    }
    
    public void setSuccessfulQueries(int successfulQueries) {
        this.successfulQueries = successfulQueries;
    }
    
    public int getFailedQueries() {
        return failedQueries;
    }
    
    public void setFailedQueries(int failedQueries) {
        this.failedQueries = failedQueries;
    }
    
    /**
     * 指标查询结果
     */
    public static class MetricResult {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("query")
        private String query;
        
        @JsonProperty("result")
        private PrometheusQueryResponse result;
        
        @JsonProperty("execution_time_ms")
        private long executionTimeMs;
        
        public MetricResult() {}
        
        public MetricResult(String name, String description, String query, PrometheusQueryResponse result, long executionTimeMs) {
            this.name = name;
            this.description = description;
            this.query = query;
            this.result = result;
            this.executionTimeMs = executionTimeMs;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getQuery() {
            return query;
        }
        
        public void setQuery(String query) {
            this.query = query;
        }
        
        public PrometheusQueryResponse getResult() {
            return result;
        }
        
        public void setResult(PrometheusQueryResponse result) {
            this.result = result;
        }
        
        public long getExecutionTimeMs() {
            return executionTimeMs;
        }
        
        public void setExecutionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
        }
    }
    
    /**
     * 查询错误
     */
    public static class QueryError {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("query")
        private String query;
        
        @JsonProperty("error")
        private String error;
        
        @JsonProperty("error_type")
        private String errorType;
        
        public QueryError() {}
        
        public QueryError(String name, String query, String error, String errorType) {
            this.name = name;
            this.query = query;
            this.error = error;
            this.errorType = errorType;
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
        
        public String getError() {
            return error;
        }
        
        public void setError(String error) {
            this.error = error;
        }
        
        public String getErrorType() {
            return errorType;
        }
        
        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }
    }
} 