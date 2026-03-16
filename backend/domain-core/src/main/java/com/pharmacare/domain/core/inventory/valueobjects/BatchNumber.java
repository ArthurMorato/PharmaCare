package com.pharmacare.domain.core.inventory.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.inventory.exceptions.ValidationException;
import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;

import java.util.regex.Pattern;

/**
 * Value Object para número de lote.
 * 
 * Formato padrão ANVISA: L + 10 dígitos numéricos
 * Exemplo: L1234567890
 */
public final class BatchNumber extends ValueObject {
    
    private static final Pattern BATCH_PATTERN = 
        Pattern.compile("^L\\d{10}$");
    
    private final String value;
    private final String formattedValue;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private BatchNumber(String value) {
        this.value = value;
        this.formattedValue = formatBatchNumber(value);
        validate();
    }
    
    /**
     * Método fábrica para criar BatchNumber.
     */
    public static BatchNumber from(String value) {
        try {
            return new BatchNumber(value);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "BatchNumber",
                value,
                "Número de lote inválido: " + e.getMessage()
            );
        }
    }
    
    /**
     * Gera um número de lote válido para testes.
     */
    public static BatchNumber generate() {
        // Gera 10 dígitos aleatórios
        long randomDigits = (long) (Math.random() * 10_000_000_000L);
        String batchNumber = "L" + String.format("%010d", randomDigits);
        return from(batchNumber);
    }
    
    /**
     * Validação do número de lote.
     */
    @Override
    protected void validate() {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.REQUIRED_FIELD,
                "Número de lote é obrigatório",
                "batchNumber"
            );
        }
        
        // Formato: L + 10 dígitos
        if (!BATCH_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Número de lote deve seguir o formato: L + 10 dígitos (ex: L1234567890)",
                "batchNumber"
            );
        }
        
        // Primeiro dígito após L não pode ser zero
        String digits = value.substring(1);
        if (digits.startsWith("0")) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Dígitos do lote não podem começar com zero",
                "batchNumber"
            );
        }
        
        // Dígitos de controle (opcional)
        validateCheckDigits(digits);
    }
    
    /**
     * Valida dígitos de controle (se necessário).
     */
    private void validateCheckDigits(String digits) {
        // Implementação de validação de dígitos de controle
        // Pode ser baseado em regras específicas da empresa ou ANVISA
        
        // Exemplo simples: soma dos dígitos não pode ser múltiplo de 10
        int sum = digits.chars()
            .map(Character::getNumericValue)
            .sum();
        
        if (sum % 10 == 0) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Número de lote inválido (validação de dígitos falhou)",
                "batchNumber"
            );
        }
    }
    
    /**
     * Formata o número de lote para exibição.
     */
    private String formatBatchNumber(String batchNumber) {
        // Formato: L 1234 5678 90
        if (batchNumber.length() == 11) {
            return String.format("%s %s %s %s",
                batchNumber.substring(0, 1),
                batchNumber.substring(1, 5),
                batchNumber.substring(5, 9),
                batchNumber.substring(9));
        }
        return batchNumber;
    }
    
    /**
     * Extrai o ano de fabricação do lote.
     */
    public int getManufacturingYear() {
        // Assume que os primeiros 2 dígitos após L são o ano
        String yearDigits = value.substring(1, 3);
        return Integer.parseInt(yearDigits) + 2000; // Assume século 21
    }
    
    /**
     * Extrai o código da fábrica.
     */
    public String getFactoryCode() {
        // Assume que os dígitos 3-6 são o código da fábrica
        return value.substring(3, 7);
    }
    
    /**
     * Extrai o número sequencial.
     */
    public String getSequenceNumber() {
        // Assume que os últimos 4 dígitos são o sequencial
        return value.substring(7);
    }
    
    public String getValue() {
        return value;
    }
    
    public String getFormattedValue() {
        return formattedValue;
    }
    
    /**
     * Retorna os componentes para igualdade.
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{value};
    }
    
    @Override
    public String toString() {
        return "BatchNumber{value='" + value + "'}";
    }
}