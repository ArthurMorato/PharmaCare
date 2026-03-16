package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.common.validation.ValidationPatterns;
import com.pharmacare.domain.core.inventory.exceptions.ValidationException;
import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object para identificador único de produto.
 * Imutável e validado.
 * 
 * Requisitos:
 * - UUID válido
 * - Não pode ser nulo
 */
public final class ProductId extends ValueObject {
    
    private final UUID value;
    private final String stringValue;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private ProductId(UUID value) {
        Objects.requireNonNull(value, "ID do produto não pode ser nulo");
        
        this.value = value;
        this.stringValue = value.toString().toLowerCase();
        
        // Validação
        validate();
    }
    
    /**
     * Construtor privado a partir de string.
     */
    private ProductId(String value) {
        Objects.requireNonNull(value, "ID do produto não pode ser nulo");
        
        try {
            this.value = UUID.fromString(value);
            this.stringValue = this.value.toString().toLowerCase();
        } catch (IllegalArgumentException e) {
            throw new InvalidValueObjectException(
                "ProductId",
                value,
                "ID do produto inválido: " + e.getMessage()
            );
        }
        
        // Validação
        validate();
    }
    
    /**
     * Método fábrica para criar um ProductId a partir de UUID.
     */
    public static ProductId from(UUID value) {
        try {
            return new ProductId(value);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "ProductId",
                value.toString(),
                "ID do produto inválido: " + e.getMessage()
            );
        }
    }
    
    /**
     * Método fábrica para criar um ProductId a partir de string.
     */
    public static ProductId from(String value) {
        try {
            return new ProductId(value);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "ProductId",
                value,
                "ID do produto inválido: " + e.getMessage()
            );
        }
    }
    
    /**
     * Gera um novo ProductId aleatório.
     */
    public static ProductId generate() {
        return new ProductId(UUID.randomUUID());
    }
    
    /**
     * Validação do ID.
     */
    @Override
    protected void validate() {
        // Valida formato UUID
        if (!ValidationPatterns.isValidUUID(stringValue)) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_UUID,
                "ID do produto deve ser um UUID válido",
                "productId"
            );
        }
        
        // Valida versão do UUID (deve ser versão 4 para UUIDs aleatórios)
        if (value.version() != 4) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "ID do produto deve ser um UUID versão 4",
                "productId"
            );
        }
    }
    
    /**
     * Retorna o UUID do produto.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Retorna o ID como string.
     */
    public String getStringValue() {
        return stringValue;
    }
    
    /**
     * Retorna o ID como string sem hífens.
     */
    public String getCompactString() {
        return stringValue.replace("-", "");
    }
    
    /**
     * Retorna a versão do UUID.
     */
    public int getVersion() {
        return value.version();
    }
    
    /**
     * Retorna a variante do UUID.
     */
    public int getVariant() {
        return value.variant();
    }
    
    /**
     * Retorna o timestamp para UUIDs baseados em tempo (versão 1).
     * Para UUIDs versão 4, retorna null.
     */
    public Long getTimestamp() {
        if (value.version() == 1) {
            return value.timestamp();
        }
        return null;
    }
    
    /**
     * Verifica se o ID é nulo (UUID com todos os bits zero).
     */
    public boolean isNull() {
        return value.equals(new UUID(0L, 0L));
    }
    
    /**
     * Retorna os componentes para igualdade.
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{value};
    }
    
    /**
     * Representação em string.
     */
    @Override
    public String toString() {
        return "ProductId{value='" + stringValue + "'}";
    }
}