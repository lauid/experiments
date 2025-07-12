package com.example.kdemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Prometheus批量查询请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrometheusQueryRequest {
    
    @JsonProperty("cluster")
    private String cluster;
    
    @JsonProperty("queries")
    private List<MetricQuery> queries;
    
    @JsonProperty("start_time")
    private Long startTime; // 时间戳（秒）
    
    @JsonProperty("end_time")
    private Long endTime; // 时间戳（秒）
    
    @JsonProperty("step")
    private String step;
    
    /**
     * 单个指标查询
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricQuery {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("query")
        private String query;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("labels")
        private Map<String, String> labels;
    }
} 