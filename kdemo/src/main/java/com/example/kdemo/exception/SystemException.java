package com.example.kdemo.exception;

/**
 * 系统异常类，用于接管和封装系统中的各种异常
 */
public class SystemException extends RuntimeException {
    private final String errorCode;
    private final String errorType;
    private final String errorDetail;
    private final Throwable originalException;

    /**
     * 基本构造函数
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorType 错误类型
     */
    public SystemException(String message, String errorCode, String errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.errorDetail = null;
        this.originalException = null;
    }

    /**
     * 包含原始异常的构造函数
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorType 错误类型
     * @param cause 原始异常
     */
    public SystemException(String message, String errorCode, String errorType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.errorDetail = cause != null ? cause.getMessage() : null;
        this.originalException = cause;
    }

    /**
     * 带有详细信息的构造函数
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param errorType 错误类型
     * @param errorDetail 错误详情
     * @param cause 原始异常
     */
    public SystemException(String message, String errorCode, String errorType, String errorDetail, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.errorDetail = errorDetail;
        this.originalException = cause;
    }

    /**
     * 静态工厂方法，从系统异常创建SystemException
     * @param e 系统异常
     * @param errorCode 错误代码
     * @param errorType 错误类型
     * @return SystemException实例
     */
    public static SystemException fromSystemException(Exception e, String errorCode, String errorType) {
        return new SystemException(
                e.getMessage() != null ? e.getMessage() : "System error occurred",
                errorCode,
                errorType,
                e
        );
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public Throwable getOriginalException() {
        return originalException;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SystemException: [")
                .append("errorCode='").append(errorCode).append("', ")
                .append("errorType='").append(errorType).append("', ")
                .append("message='").append(getMessage()).append("'");
        if (errorDetail != null) {
            sb.append(", errorDetail='").append(errorDetail).append("'");
        }
        if (originalException != null) {
            sb.append(", originalException='").append(originalException.getClass().getSimpleName()).append("'");
        }
        sb.append("]");
        return sb.toString();
    }
}