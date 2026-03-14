package com.pharmacare.domain.core.exceptions;

import com.pharmacare.domain.core.common.DomainException;

/**
 * Exceção lançada quando um Value Object não pode ser criado devido a valores inválidos.
 */
public class InvalidValueObjectException extends DomainException {
    
    private final String valueObjectType;
    private final String invalidValue;
    
    public InvalidValueObjectException(String valueObjectType, String invalidValue, String message) {
        super("INVALID_VALUE_OBJECT", message);
        this.valueObjectType = valueObjectType;
        this.invalidValue = invalidValue;
    }
    
    public InvalidValueObjectException(String valueObjectType, String invalidValue, String message, Throwable cause) {
        super(message, cause);
        this.valueObjectType = valueObjectType;
        this.invalidValue = invalidValue;
    }
    
    public String getValueObjectType() {
        return valueObjectType;
    }
    
    public String getInvalidValue() {
        return invalidValue;
    }
    
    @Override
    public String toString() {
        return String.format("InvalidValueObjectException{valueObjectType='%s', invalidValue='%s', message='%s'}",
                valueObjectType, invalidValue, getMessage());
    }
}