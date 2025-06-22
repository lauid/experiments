package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Prometheus批量查询响应DTO
 */
public class PrometheusQueryResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("data")
    private QueryData data;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("error_type")
    private String errorType;
    
    public PrometheusQueryResponse() {}
    
    public PrometheusQueryResponse(String status, QueryData data) {
        this.status = status;
        this.data = data;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public QueryData getData() {
        return data;
    }
    
    public void setData(QueryData data) {
        this.data = data;
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
    
    /**
     * 查询数据
     */
    public static class QueryData {
        @JsonProperty("resultType")
        private String resultType;
        
        @JsonProperty("result")
        private List<QueryResult> result;
        
        public QueryData() {}
        
        public QueryData(String resultType, List<QueryResult> result) {
            this.resultType = resultType;
            this.result = result;
        }
        
        // Getters and Setters
        public String getResultType() {
            return resultType;
        }
        
        public void setResultType(String resultType) {
            this.resultType = resultType;
        }
        
        public List<QueryResult> getResult() {
            return result;
        }
        
        public void setResult(List<QueryResult> result) {
            this.result = result;
        }
    }
    
    /**
     * 查询结果
     */
    public static class QueryResult {
        @JsonProperty("metric")
        private Map<String, String> metric;
        
        @JsonProperty("values")
        private List<List<Object>> values;
        
        @JsonProperty("value")
        private List<Object> value;
        
        public QueryResult() {}
        
        public QueryResult(Map<String, String> metric, List<List<Object>> values) {
            this.metric = metric;
            this.values = values;
        }
        
        public QueryResult(Map<String, String> metric, List<Object> value, boolean isInstant) {
            this.metric = metric;
            this.value = value;
        }
        
        // Getters and Setters
        public Map<String, String> getMetric() {
            return metric;
        }
        
        public void setMetric(Map<String, String> metric) {
            this.metric = metric;
        }
        
        public List<List<Object>> getValues() {
            return values;
        }
        
        public void setValues(List<List<Object>> values) {
            this.values = values;
        }
        
        public List<Object> getValue() {
            return value;
        }
        
        public void setValue(List<Object> value) {
            this.value = value;
        }
    }
} 