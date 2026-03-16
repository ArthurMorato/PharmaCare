package com.pharmacare.application.common.exception;

/**
 * Exceção base para todas as exceções da camada de aplicação.
 */
public abstract class ApplicationException extends RuntimeException {
    
    private final String errorCode;
    private final String fieldName;
    
    protected ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fieldName = null;
    }
    
    protected ApplicationException(String errorCode, String message, String fieldName) {
        super(message);
        this.errorCode = errorCode;
        this.fieldName = fieldName;
    }
    
    protected ApplicationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.fieldName = null;
    }
    
    protected ApplicationException(String errorCode, String message, String fieldName, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.fieldName = fieldName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public boolean hasField() {
        return fieldName != null;
    }
}