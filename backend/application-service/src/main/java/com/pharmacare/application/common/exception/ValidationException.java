package com.pharmacare.application.common.exception;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Exceção para erros de validação na camada de aplicação.
 */
public class ValidationException extends ApplicationException {
    
    public static class ValidationError {
        private final String field;
        private final String code;
        private final String message;
        
        public ValidationError(String field, String code, String message) {
            this.field = field;
            this.code = code;
            this.message = message;
        }
        
        public String getField() {
            return field;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private final List<ValidationError> errors;
    
    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
        this.errors = new ArrayList<>();
    }
    
    public ValidationException(String errorCode, String message, String fieldName) {
        super(errorCode, message, fieldName);
        this.errors = new ArrayList<>();
        this.errors.add(new ValidationError(fieldName, errorCode, message));
    }
    
    public ValidationException(String errorCode, List<ValidationError> errors) {
        super(errorCode, "Erros de validação encontrados");
        this.errors = new ArrayList<>(errors);
    }
    
    public ValidationException(String errorCode, String message, List<ValidationError> errors) {
        super(errorCode, message);
        this.errors = new ArrayList<>(errors);
    }
    
    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public void addError(String field, String code, String message) {
        this.errors.add(new ValidationError(field, code, message));
    }
    
    public void addError(ValidationError error) {
        this.errors.add(error);
    }
}