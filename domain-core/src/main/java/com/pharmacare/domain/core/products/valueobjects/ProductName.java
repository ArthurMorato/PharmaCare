package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.common.ValidationPatterns;
import com.pharmacare.domain.core.exceptions.ValidationException;
import com.pharmacare.domain.core.exceptions.InvalidValueObjectException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Value Object para nome de produto.
 * Imutável e validado.
 * 
 * Requisitos:
 * - 3 a 100 caracteres
 * - Apenas letras, números e espaços
 * - Não pode ser nulo ou vazio
 * - Trim automático de espaços extras
 */
public final class ProductName extends ValueObject {
    
    private final String value;
    private final String normalizedValue;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private ProductName(String value) {
        Objects.requireNonNull(value, "Nome do produto não pode ser nulo");
        
        // Trim e normaliza espaços
        String trimmed = value.trim();
        String normalized = trimmed.replaceAll("\\s+", " ");
        
        this.value = trimmed;
        this.normalizedValue = normalized;
        
        // Validação
        validate();
    }
    
    /**
     * Método fábrica para criar um ProductName.
     * Lança InvalidValueObjectException se o valor for inválido.
     */
    public static ProductName from(String value) {
        try {
            return new ProductName(value);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "ProductName", 
                value, 
                "Nome do produto inválido: " + e.getMessage()
            );
        }
    }
    
    /**
     * Validação do Value Object.
     */
    @Override
    protected void validate() {
        // Valida comprimento
        if (value.length() < 3 || value.length() > 100) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_LENGTH,
                "Nome do produto deve ter entre 3 e 100 caracteres",
                "productName"
            );
        }
        
        // Valida padrão de caracteres
        if (!ValidationPatterns.PRODUCT_NAME_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_CHARACTERS,
                "Nome do produto contém caracteres inválidos. Apenas letras, números, espaços e alguns caracteres especiais são permitidos",
                "productName"
            );
        }
        
        // Validações adicionais específicas do domínio
        validateDomainRules();
    }
    
    /**
     * Regras de domínio específicas para nome de produto.
     */
    private void validateDomainRules() {
        // Não pode ser apenas números
        if (value.matches("^\\d+$")) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Nome do produto não pode conter apenas números",
                "productName"
            );
        }
        
        // Não pode ter espaços múltiplos consecutivos (já normalizado, mas valida original)
        if (value.contains("  ")) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Nome do produto não pode conter múltiplos espaços consecutivos",
                "productName"
            );
        }
    }
    
    /**
     * Retorna o valor do nome do produto.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Retorna o valor normalizado (sem espaços múltiplos).
     */
    public String getNormalizedValue() {
        return normalizedValue;
    }
    
    /**
     * Retorna o nome em letras maiúsculas.
     */
    public String toUpperCase() {
        return value.toUpperCase();
    }
    
    /**
     * Retorna o nome em letras minúsculas.
     */
    public String toLowerCase() {
        return value.toLowerCase();
    }
    
    /**
     * Verifica se o nome contém o texto especificado (case-insensitive).
     */
    public boolean containsIgnoreCase(String text) {
        return normalizedValue.toLowerCase().contains(text.toLowerCase());
    }
    
    /**
     * Retorna os componentes para igualdade.
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{normalizedValue.toLowerCase()};
    }
    
    /**
     * Representação em string.
     */
    @Override
    public String toString() {
        return "ProductName{value='" + value + "'}";
    }
}