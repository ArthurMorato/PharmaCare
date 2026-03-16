package com.pharmacare.domain.core.products.exceptions;

import com.pharmacare.domain.core.common.DomainException;

/**
 * Exceções específicas do domínio de produtos.
 */
public class ProductDomainException extends DomainException {
    
    public enum ProductErrorCode {
        PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND"),
        PRODUCT_ALREADY_EXISTS("PRODUCT_ALREADY_EXISTS"),
        INVALID_PRODUCT_STATE("INVALID_PRODUCT_STATE"),
        INVALID_PRICE_CHANGE("INVALID_PRICE_CHANGE"),
        PRODUCT_DISCONTINUED("PRODUCT_DISCONTINUED"),
        BARCODE_ALREADY_REGISTERED("BARCODE_ALREADY_REGISTERED"),
        INVALID_CATEGORY_CHANGE("INVALID_CATEGORY_CHANGE");
        
        private final String code;
        
        ProductErrorCode(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    private final ProductErrorCode errorCode;
    
    public ProductDomainException(ProductErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
        this.errorCode = errorCode;
    }
    
    public ProductDomainException(ProductErrorCode errorCode, String message, String fieldName) {
        super(errorCode.getCode(), message, fieldName);
        this.errorCode = errorCode;
    }
    
    public ProductDomainException(ProductErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), message, cause);
        this.errorCode = errorCode;
    }
    
    public ProductErrorCode getProductErrorCode() {
        return errorCode;
    }
}