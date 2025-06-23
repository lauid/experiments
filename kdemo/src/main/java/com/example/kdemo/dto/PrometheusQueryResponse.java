package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Prometheus查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrometheusQueryResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("data")
    private QueryData data;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("error_type")
    private String errorType;
    
    /**
     * 查询数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryData {
        @JsonProperty("resultType")
        private String resultType;
        
        @JsonProperty("result")
        private List<QueryResult> result;
    }
    
    /**
     * 查询结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryResult {
        @JsonProperty("metric")
        private Object metric;
        
        @JsonProperty("value")
        private List<Object> value;
        
        @JsonProperty("values")
        private List<List<Object>> values;
    }

    public void setStatus(String status) { this.status = status; }
    public void setError(String error) { this.error = error; }
    public void setErrorType(String errorType) { this.errorType = errorType; }
} 