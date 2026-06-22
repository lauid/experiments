package com.example.kdemo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器，接管系统中的所有异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义SystemException
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Map<String, Object>> handleSystemException(SystemException e) {
        logger.error("System exception occurred: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        errorResponse.put("errorCode", e.getErrorCode());
        errorResponse.put("errorType", e.getErrorType());
        errorResponse.put("exceptionClass", e.getClass().getName());

        if (e.getErrorDetail() != null) {
            errorResponse.put("errorDetail", e.getErrorDetail());
        }

        if (e.getOriginalException() != null) {
            errorResponse.put("originalException", e.getOriginalException().getClass().getName());
            errorResponse.put("originalMessage", e.getOriginalException().getMessage());
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理KubernetesException
     */
    @ExceptionHandler(KubernetesException.class)
    public ResponseEntity<Map<String, Object>> handleKubernetesException(KubernetesException e) {
        logger.error("Kubernetes exception occurred: {}", e.getMessage(), e);

        // 转换为SystemException
        SystemException systemException = SystemException.fromSystemException(
                e, "K8S_ERROR", "KUBERNETES_OPERATION_FAILURE"
        );

        return handleSystemException(systemException);
    }

    /**
     * 处理PrometheusException
     */
    @ExceptionHandler(PrometheusException.class)
    public ResponseEntity<Map<String, Object>> handlePrometheusException(PrometheusException e) {
        logger.error("Prometheus exception occurred: {}", e.getMessage(), e);

        // 转换为SystemException
        SystemException systemException = SystemException.fromSystemException(
                e, "PROM_ERROR", "PROMETHEUS_OPERATION_FAILURE"
        );

        return handleSystemException(systemException);
    }

    /**
     * 处理ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.error("Resource not found exception: {}", e.getMessage(), e);

        // 转换为SystemException
        SystemException systemException = SystemException.fromSystemException(
                e, "NOT_FOUND", "RESOURCE_NOT_FOUND"
        );

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        errorResponse.put("errorCode", systemException.getErrorCode());
        errorResponse.put("errorType", systemException.getErrorType());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        logger.error("Unexpected exception occurred: {}", e.getMessage(), e);

        // 转换为SystemException
        SystemException systemException = SystemException.fromSystemException(
                e, "INTERNAL_ERROR", "SYSTEM_INTERNAL_ERROR"
        );

        return handleSystemException(systemException);
    }
}