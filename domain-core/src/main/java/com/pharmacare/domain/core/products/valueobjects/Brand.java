package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.common.ValidationPatterns;
import com.pharmacare.domain.core.exceptions.ValidationException;
import com.pharmacare.domain.core.exceptions.InvalidValueObjectException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Value Object para marca do produto.
 * Imutável e validado.
 * 
 * Requisitos:
 * - 1 a 50 caracteres
 * - Apenas letras, números, espaços e alguns caracteres especiais
 * - Não pode ser nulo ou vazio
 */
public final class Brand extends ValueObject {
    
    private final String value;
    private final String normalizedValue;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private Brand(String value) {
        Objects.requireNonNull(value, "Marca não pode ser nula");
        
        String trimmed = value.trim();
        String normalized = trimmed.replaceAll("\\s+", " ");
        
        this.value = trimmed;
        this.normalizedValue = normalized;
        
        // Validação
        validate();
    }
    
    /**
     * Método fábrica para criar uma Brand.
     */
    public static Brand from(String value) {
        try {
            return new Brand(value);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "Brand",
                value,
                "Marca inválida: " + e.getMessage()
            );
        }
    }
    
    /**
     * Validação da marca.
     */
    @Override
    protected void validate() {
        // Valida comprimento
        if (value.isEmpty() || value.length() > 50) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_LENGTH,
                "Marca deve ter entre 1 e 50 caracteres",
                "brand"
            );
        }
        
        // Valida padrão de caracteres
        if (!ValidationPatterns.BRAND_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_CHARACTERS,
                "Marca contém caracteres inválidos",
                "brand"
            );
        }
        
        // Validações específicas do domínio
        validateDomainRules();
    }
    
    /**
     * Regras de domínio específicas para marca.
     */
    private void validateDomainRules() {
        // Não pode conter apenas números
        if (value.matches("^\\d+$")) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Marca não pode conter apenas números",
                "brand"
            );
        }
        
        // Não pode começar ou terminar com caracteres especiais
        if (value.matches("^[.,\\-&]") || value.matches("[.,\\-&]$")) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Marca não pode começar ou terminar com caracteres especiais",
                "brand"
            );
        }
    }
    
    /**
     * Retorna o valor da marca.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Retorna o valor normalizado.
     */
    public String getNormalizedValue() {
        return normalizedValue;
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
        return "Brand{value='" + value + "'}";
    }
}