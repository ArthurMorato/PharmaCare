package com.pharmacare.domain.core.common;

/**
 * Exceção base para todos os erros de domínio.
 * Segue o princípio de que exceções de domínio devem ser unchecked (RuntimeException).
 */
public abstract class DomainException extends RuntimeException {
    
    private final String errorCode;
    private final String fieldName;
    
    /**
     * Construtor para exceção de domínio com mensagem de erro.
     */
    protected DomainException(String message) {
        super(message);
        this.errorCode = "DOMAIN_ERROR";
        this.fieldName = null;
    }
    
    /**
     * Construtor para exceção de domínio com código de erro e mensagem.
     */
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fieldName = null;
    }
    
    /**
     * Construtor para exceção de domínio com código, mensagem e nome do campo.
     */
    protected DomainException(String errorCode, String message, String fieldName) {
        super(message);
        this.errorCode = errorCode;
        this.fieldName = fieldName;
    }
    
    /**
     * Construtor para exceção de domínio com mensagem e causa.
     */
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DOMAIN_ERROR";
        this.fieldName = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Retorna uma representação detalhada do erro.
     */
    @Override
    public String toString() {
        return String.format("DomainException{errorCode='%s', fieldName='%s', message='%s'}",
                errorCode, fieldName, getMessage());
    }
}