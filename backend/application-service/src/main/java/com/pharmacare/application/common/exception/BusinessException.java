package com.pharmacare.application.common.exception;

/**
 * Exceção para erros de negócio na camada de aplicação.
 */
public class BusinessException extends ApplicationException {
    
    public enum BusinessErrorCode {
        PRODUCT_ALREADY_EXISTS("PRODUCT_ALREADY_EXISTS"),
        BARCODE_ALREADY_REGISTERED("BARCODE_ALREADY_REGISTERED"),
        INSUFFICIENT_PERMISSION("INSUFFICIENT_PERMISSION"),
        OPERATION_NOT_ALLOWED("OPERATION_NOT_ALLOWED"),
        RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND");
        
        private final String code;
        
        BusinessErrorCode(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    private final BusinessErrorCode businessErrorCode;
    
    public BusinessException(BusinessErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
        this.businessErrorCode = errorCode;
    }
    
    public BusinessException(BusinessErrorCode errorCode, String message, String fieldName) {
        super(errorCode.getCode(), message, fieldName);
        this.businessErrorCode = errorCode;
    }
    
    public BusinessException(BusinessErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), message, cause);
        this.businessErrorCode = errorCode;
    }
    
    public BusinessErrorCode getBusinessErrorCode() {
        return businessErrorCode;
    }
}