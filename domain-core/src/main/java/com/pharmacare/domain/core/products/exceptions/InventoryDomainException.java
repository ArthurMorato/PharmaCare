package com.pharmacare.domain.core.inventory.exceptions;

import com.pharmacare.domain.core.common.DomainException;

/**
 * Exceções específicas do domínio de inventário.
 */
public class InventoryDomainException extends DomainException {
    
    public enum InventoryErrorCode {
        INVENTORY_ITEM_NOT_FOUND("INVENTORY_ITEM_NOT_FOUND"),
        INSUFFICIENT_STOCK("INSUFFICIENT_STOCK"),
        EXPIRED_ITEM("EXPIRED_ITEM"),
        INVALID_QUANTITY("INVALID_QUANTITY"),
        BELOW_MINIMUM_STOCK("BELOW_MINIMUM_STOCK"),
        DUPLICATE_BATCH("DUPLICATE_BATCH"),
        TRANSFER_NOT_ALLOWED("TRANSFER_NOT_ALLOWED"),
        RESERVATION_EXCEEDS_STOCK("RESERVATION_EXCEEDS_STOCK");
        
        private final String code;
        
        InventoryErrorCode(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    private final InventoryErrorCode errorCode;
    
    public InventoryDomainException(InventoryErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
        this.errorCode = errorCode;
    }
    
    public InventoryDomainException(InventoryErrorCode errorCode, String message, String fieldName) {
        super(errorCode.getCode(), message, fieldName);
        this.errorCode = errorCode;
    }
    
    public InventoryDomainException(InventoryErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), message, cause);
        this.errorCode = errorCode;
    }
    
    public InventoryErrorCode getInventoryErrorCode() {
        return errorCode;
    }
}