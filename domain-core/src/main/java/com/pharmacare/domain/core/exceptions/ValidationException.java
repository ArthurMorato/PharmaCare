package com.pharmacare.domain.core.exceptions;

import com.pharmacare.domain.core.common.DomainException;

/**
 * Exceção lançada quando um valor não passa na validação.
 * Contém detalhes sobre qual regra de validação foi violada.
 */
public class ValidationException extends DomainException {
    
    private final ValidationErrorCode errorCode;
    
    /**
     * Códigos de erro específicos para validação.
     */
    public enum ValidationErrorCode {
        INVALID_LENGTH("INVALID_LENGTH"),
        INVALID_FORMAT("INVALID_FORMAT"),
        INVALID_CHARACTERS("INVALID_CHARACTERS"),
        REQUIRED_VALUE("REQUIRED_VALUE"),
        INVALID_RANGE("INVALID_RANGE"),
        CHECK_DIGIT_FAILED("CHECK_DIGIT_FAILED"),
        INVALID_UUID("INVALID_UUID");
        
        private final String code;
        
        ValidationErrorCode(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    public ValidationException(ValidationErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
        this.errorCode = errorCode;
    }
    
    public ValidationException(ValidationErrorCode errorCode, String message, String fieldName) {
        super(errorCode.getCode(), message, fieldName);
        this.errorCode = errorCode;
    }
    
    public ValidationErrorCode getValidationErrorCode() {
        return errorCode;
    }
}