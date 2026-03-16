package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.common.validation.ValidationPatterns;
import com.pharmacare.domain.core.inventory.exceptions.ValidationException;
import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;

import java.util.Objects;

/**
 * Value Object para código de barras EAN-13.
 * Imutável e validado.
 * 
 * Requisitos:
 * - Exatamente 13 dígitos
 * - Dígito verificador válido (algoritmo EAN-13)
 * - Não pode ser nulo
 */
public final class Barcode extends ValueObject {
    
    private final String value;
    private final String formattedValue;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private Barcode(String value) {
        Objects.requireNonNull(value, "Código de barras não pode ser nulo");
        
        // Remove quaisquer caracteres não numéricos
        String digitsOnly = value.replaceAll("[^\\d]", "");
        
        this.value = digitsOnly;
        this.formattedValue = formatBarcode(digitsOnly);
        
        // Validação
        validate();
    }
    
    /**
     * Método fábrica para criar um Barcode.
     */
    public static Barcode from(String value) {
        try {
            return new Barcode(value);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "Barcode",
                value,
                "Código de barras inválido: " + e.getMessage()
            );
        }
    }
    
    /**
     * Validação do código de barras.
     */
    @Override
    protected void validate() {
        // Valida comprimento
        if (value.length() != 13) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_LENGTH,
                "Código de barras deve ter exatamente 13 dígitos",
                "barcode"
            );
        }
        
        // Valida se contém apenas dígitos
        if (!value.matches("\\d+")) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_CHARACTERS,
                "Código de barras deve conter apenas dígitos",
                "barcode"
            );
        }
        
        // Valida dígito verificador EAN-13
        if (!ValidationPatterns.isValidEAN13CheckDigit(value)) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.CHECK_DIGIT_FAILED,
                "Dígito verificador do código de barras inválido",
                "barcode"
            );
        }
    }
    
    /**
     * Formata o código de barras para exibição (XXXXX-XXXXX-X).
     */
    private String formatBarcode(String barcode) {
        if (barcode.length() != 13) return barcode;
        
        return String.format("%s-%s-%s",
            barcode.substring(0, 5),
            barcode.substring(5, 10),
            barcode.substring(10, 13)
        );
    }
    
    /**
     * Retorna o código de barras sem formatação.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Retorna o código de barras formatado.
     */
    public String getFormattedValue() {
        return formattedValue;
    }
    
    /**
     * Retorna os primeiros 3 dígitos (código do país).
     */
    public String getCountryCode() {
        return value.substring(0, 3);
    }
    
    /**
     * Retorna o código do fabricante (dígitos 4-8).
     */
    public String getManufacturerCode() {
        return value.substring(3, 8);
    }
    
    /**
     * Retorna o código do produto (dígitos 9-12).
     */
    public String getProductCode() {
        return value.substring(8, 12);
    }
    
    /**
     * Retorna o dígito verificador.
     */
    public char getCheckDigit() {
        return value.charAt(12);
    }
    
    /**
     * Verifica se o código de barras é de um país específico.
     */
    public boolean isFromCountry(String countryPrefix) {
        return value.startsWith(countryPrefix);
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
        return "Barcode{value='" + formattedValue + "'}";
    }
}