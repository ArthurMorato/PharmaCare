package com.pharmacare.domain.core.inventory.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.exceptions.ValidationException;
import com.pharmacare.domain.core.exceptions.InvalidValueObjectException;

import java.util.Objects;

/**
 * Value Object para representar quantidade no estoque.
 * 
 * Regras:
 * - Não pode ser negativo
 * - Pode ser zero (item sem estoque)
 * - Validações específicas para cada contexto
 */
public final class Quantity extends ValueObject {
    
    private final int value;
    private final String unit;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private Quantity(int value, String unit) {
        this.value = value;
        this.unit = unit;
        validate();
    }
    
    /**
     * Método fábrica para criar Quantity.
     */
    public static Quantity of(int value) {
        try {
            return new Quantity(value, "units");
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "Quantity",
                String.valueOf(value),
                "Quantidade inválida: " + e.getMessage()
            );
        }
    }
    
    /**
     * Método fábrica com unidade personalizada.
     */
    public static Quantity of(int value, String unit) {
        try {
            return new Quantity(value, unit);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "Quantity",
                String.valueOf(value),
                "Quantidade inválida: " + e.getMessage()
            );
        }
    }
    
    /**
     * Cria Quantity zero.
     */
    public static Quantity zero() {
        return new Quantity(0, "units");
    }
    
    /**
     * Cria Quantity com quantidade mínima (5 unidades).
     */
    public static Quantity minimum() {
        return new Quantity(5, "units");
    }
    
    /**
     * Validação da quantidade.
     */
    @Override
    protected void validate() {
        // Não pode ser negativo
        if (value < 0) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_RANGE,
                "Quantidade não pode ser negativa",
                "value"
            );
        }
        
        // Valida unidade
        if (unit == null || unit.trim().isEmpty()) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Unidade não pode ser vazia",
                "unit"
            );
        }
        
        // Limite máximo por item (configurável)
        int maxQuantity = 999999;
        if (value > maxQuantity) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_RANGE,
                "Quantidade não pode exceder " + maxQuantity,
                "value"
            );
        }
    }
    
    /**
     * Adiciona quantidade.
     */
    public Quantity add(Quantity other) {
        validateSameUnit(other);
        return new Quantity(this.value + other.value, this.unit);
    }
    
    /**
     * Adiciona quantidade inteira.
     */
    public Quantity add(int amount) {
        return add(Quantity.of(amount, this.unit));
    }
    
    /**
     * Subtrai quantidade.
     */
    public Quantity subtract(Quantity other) {
        validateSameUnit(other);
        
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException(
                "Resultado da subtração não pode ser negativo"
            );
        }
        
        return new Quantity(result, this.unit);
    }
    
    /**
     * Subtrai quantidade inteira.
     */
    public Quantity subtract(int amount) {
        return subtract(Quantity.of(amount, this.unit));
    }
    
    /**
     * Multiplica por um fator.
     */
    public Quantity multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Fator de multiplicação não pode ser negativo");
        }
        
        return new Quantity(this.value * factor, this.unit);
    }
    
    /**
     * Divide por um divisor.
     */
    public Quantity divide(int divisor) {
        if (divisor <= 0) {
            throw new IllegalArgumentException("Divisor deve ser positivo");
        }
        
        return new Quantity(this.value / divisor, this.unit);
    }
    
    /**
     * Verifica se é zero.
     */
    public boolean isZero() {
        return value == 0;
    }
    
    /**
     * Verifica se está abaixo do mínimo.
     */
    public boolean isBelowMinimum() {
        return value < 5; // Quantidade mínima do sistema
    }
    
    /**
     * Verifica se está abaixo de um limite.
     */
    public boolean isBelow(int threshold) {
        return value < threshold;
    }
    
    /**
     * Verifica se está acima de um limite.
     */
    public boolean isAbove(int threshold) {
        return value > threshold;
    }
    
    /**
     * Compara quantidades.
     */
    public int compareTo(Quantity other) {
        validateSameUnit(other);
        return Integer.compare(this.value, other.value);
    }
    
    /**
     * Valida se as unidades são iguais.
     */
    private void validateSameUnit(Quantity other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException(
                "Operação entre quantidades com unidades diferentes não suportada: " +
                this.unit + " != " + other.unit
            );
        }
    }
    
    public int getValue() {
        return value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    /**
     * Retorna os componentes para igualdade.
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{value, unit};
    }
    
    @Override
    public String toString() {
        return "Quantity{value=" + value + ", unit='" + unit + "'}";
    }
}