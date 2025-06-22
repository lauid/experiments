package com.example.kdemo.exception;

/**
 * Prometheus操作异常
 */
public class PrometheusException extends RuntimeException {
    
    private final String errorType;
    private final String query;
    
    public PrometheusException(String message) {
        super(message);
        this.errorType = "PROMETHEUS_ERROR";
        this.query = null;
    }
    
    public PrometheusException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = "PROMETHEUS_ERROR";
        this.query = null;
    }
    
    public PrometheusException(String message, String errorType, String query) {
        super(message);
        this.errorType = errorType;
        this.query = query;
    }
    
    public PrometheusException(String message, String errorType, String query, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.query = query;
    }
    
    public String getErrorType() {
        return errorType;
    }
    
    public String getQuery() {
        return query;
    }
} 