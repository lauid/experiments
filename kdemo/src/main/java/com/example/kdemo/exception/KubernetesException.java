package com.example.kdemo.exception;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.openapi.JSON;

public class KubernetesException extends RuntimeException {
    private final String errorCode;
    private final String operation;
    private final String errorDetail;
    
    public KubernetesException(String message, String errorCode, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
        this.errorDetail = null;
    }
    
    public KubernetesException(String message, String errorCode, String operation, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
        this.errorDetail = null;
    }
    
    // 推荐：从 V1Status 构造，拼装 errorDetail
    public KubernetesException(String message, String operation, V1Status status) {
        super(message + (status != null ? " [" + buildErrorDetail(status) + "]" : ""));
        this.errorCode = status != null && status.getReason() != null ? status.getReason() : (status != null && status.getCode() != null ? String.valueOf(status.getCode()) : null);
        this.operation = operation;
        this.errorDetail = buildErrorDetail(status);
    }

    private static String buildErrorDetail(V1Status status) {
        if (status == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("kind=").append(status.getKind());
        if (status.getDetails() != null) {
            sb.append(", resourceKind=").append(status.getDetails().getKind());
            sb.append(", resourceName=").append(status.getDetails().getName());
        }
        sb.append(", reason=").append(status.getReason());
        sb.append(", code=").append(status.getCode());
        sb.append(", message=").append(status.getMessage());
        return sb.toString();
    }
    
    public String getErrorCode() { return errorCode; }
    public String getOperation() { return operation; }
    public String getErrorDetail() { return errorDetail; }

    // 静态工具方法：从ApiException解析V1Status
    public static V1Status parseStatusFromApiException(ApiException e) {
        String body = e.getResponseBody();
        if (body != null && !body.isEmpty()) {
            try {
                return JSON.getGson().fromJson(body, V1Status.class);
            } catch (Exception ex) {
                // 解析失败，返回null
            }
        }
        return null;
    }
} 