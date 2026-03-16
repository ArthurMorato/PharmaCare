package com.pharmacare.domain.core.inventory.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.inventory.exceptions.ValidationException;
import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * Value Object para representação monetária.
 * Imutável e validado.
 * 
 * Requisitos:
 * - Valor não negativo (≥ 0)
 * - Duas casas decimais
 * - Moeda BRL por padrão
 * - Não pode ser nulo
 */
public final class Money extends ValueObject {
    
    private final BigDecimal amount;
    private final Currency currency;
    private final String formattedValue;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private Money(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "Valor não pode ser nulo");
        Objects.requireNonNull(currency, "Moeda não pode ser nula");
        
        // Arredonda para 2 casas decimais
        this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        this.currency = currency;
        this.formattedValue = formatMoney();
        
        // Validação
        validate();
    }
    
    /**
     * Método fábrica para criar Money com moeda BRL.
     */
    public static Money of(BigDecimal amount) {
        try {
            return new Money(amount, Currency.getInstance("BRL"));
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "Money",
                amount.toString(),
                "Valor monetário inválido: " + e.getMessage()
            );
        }
    }
    
    /**
     * Método fábrica para criar Money com moeda específica.
     */
    public static Money of(BigDecimal amount, String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            return new Money(amount, currency);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "Money",
                amount.toString(),
                "Valor monetário inválido: " + e.getMessage()
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidValueObjectException(
                "Money",
                currencyCode,
                "Código de moeda inválido: " + e.getMessage()
            );
        }
    }
    
    /**
     * Cria Money a partir de double.
     */
    public static Money of(double amount) {
        return of(BigDecimal.valueOf(amount));
    }
    
    /**
     * Cria Money a partir de string.
     */
    public static Money fromString(String amount) {
        try {
            return of(new BigDecimal(amount));
        } catch (NumberFormatException e) {
            throw new InvalidValueObjectException(
                "Money",
                amount,
                "Valor monetário inválido: " + e.getMessage()
            );
        }
    }
    
    /**
     * Retorna valor zero.
     */
    public static Money zero() {
        return of(BigDecimal.ZERO);
    }
    
    /**
     * Validação do valor monetário.
     */
    @Override
    protected void validate() {
        // Valida se o valor não é negativo
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_RANGE,
                "Valor monetário não pode ser negativo",
                "amount"
            );
        }
        
        // Valida escala (máximo 2 casas decimais)
        if (amount.scale() > 2) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Valor monetário deve ter no máximo 2 casas decimais",
                "amount"
            );
        }
        
        // Valida valor máximo (opcional)
        BigDecimal maxValue = new BigDecimal("9999999.99");
        if (amount.compareTo(maxValue) > 0) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_RANGE,
                "Valor monetário não pode exceder " + maxValue,
                "amount"
            );
        }
    }
    
    /**
     * Formata o valor para exibição.
     */
    private String formatMoney() {
        // Usa locale brasileiro para formatação
        Locale brazil = new Locale("pt", "BR");
        
        if (currency.getCurrencyCode().equals("BRL")) {
            return String.format(brazil, "R$ %,.2f", amount);
        } else {
            return String.format(brazil, "%s %,.2f", 
                currency.getSymbol(brazil), amount);
        }
    }
    
    /**
     * Retorna o valor numérico.
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * Retorna a moeda.
     */
    public Currency getCurrency() {
        return currency;
    }
    
    /**
     * Retorna o código da moeda.
     */
    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }
    
    /**
     * Retorna o valor formatado.
     */
    public String getFormattedValue() {
        return formattedValue;
    }
    
    /**
     * Soma outro valor monetário (mesma moeda).
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    /**
     * Subtrai outro valor monetário (mesma moeda).
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Resultado da subtração não pode ser negativo"
            );
        }
        
        return new Money(result, this.currency);
    }
    
    /**
     * Multiplica por um multiplicador.
     */
    public Money multiply(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Multiplicador não pode ser negativo");
        }
        
        BigDecimal result = this.amount.multiply(multiplier);
        return new Money(result, this.currency);
    }
    
    /**
     * Multiplica por um multiplicador inteiro.
     */
    public Money multiply(int multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }
    
    /**
     * Calcula porcentagem.
     */
    public Money percentage(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Porcentagem não pode ser negativa");
        }
        
        BigDecimal multiplier = percentage.divide(BigDecimal.valueOf(100));
        BigDecimal result = this.amount.multiply(multiplier);
        return new Money(result, this.currency);
    }
    
    /**
     * Compara valores (mesma moeda).
     */
    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }
    
    /**
     * Verifica se é maior que outro valor.
     */
    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }
    
    /**
     * Verifica se é menor que outro valor.
     */
    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }
    
    /**
     * Verifica se é igual a outro valor.
     */
    public boolean isEqualTo(Money other) {
        return compareTo(other) == 0;
    }
    
    /**
     * Verifica se é zero.
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Valida se a moeda é a mesma.
     */
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Operação entre moedas diferentes não suportada: " +
                this.currency + " != " + other.currency
            );
        }
    }
    
    /**
     * Retorna os componentes para igualdade.
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{amount, currency};
    }
    
    /**
     * Representação em string.
     */
    @Override
    public String toString() {
        return "Money{amount=" + amount + ", currency=" + currency + "}";
    }
}