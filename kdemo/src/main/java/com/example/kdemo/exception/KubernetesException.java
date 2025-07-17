package com.example.kdemo.exception;

public class KubernetesException extends RuntimeException {
    
    private final String errorCode;
    private final String operation;
    
    public KubernetesException(String message, String errorCode, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
    }
    
    public KubernetesException(String message, String errorCode, String operation, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getOperation() {
        return operation;
    }
} 