package com.pharmacare.domain.core.inventory.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.inventory.exceptions.ValidationException;
import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value Object para data de validade.
 * 
 * Regras:
 * - Deve ser uma data futura
 * - Validade mínima: 1 dia a partir de hoje
 * - Validade máxima: 5 anos a partir de hoje
 */
public final class ExpirationDate extends ValueObject {
    
    private final LocalDate date;
    private final LocalDate today;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private ExpirationDate(LocalDate date) {
        this.date = date;
        this.today = LocalDate.now();
        validate();
    }
    
    /**
     * Método fábrica para criar ExpirationDate.
     */
    public static ExpirationDate of(LocalDate date) {
        try {
            return new ExpirationDate(date);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "ExpirationDate",
                date.toString(),
                "Data de validade inválida: " + e.getMessage()
            );
        }
    }
    
    /**
     * Método fábrica a partir de string (yyyy-MM-dd).
     */
    public static ExpirationDate fromString(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            return of(date);
        } catch (Exception e) {
            throw new InvalidValueObjectException(
                "ExpirationDate",
                dateString,
                "Data de validade inválida: " + e.getMessage()
            );
        }
    }
    
    /**
     * Cria ExpirationDate adicionando dias à data atual.
     */
    public static ExpirationDate daysFromNow(int days) {
        LocalDate futureDate = LocalDate.now().plusDays(days);
        return of(futureDate);
    }
    
    /**
     * Cria ExpirationDate adicionando meses à data atual.
     */
    public static ExpirationDate monthsFromNow(int months) {
        LocalDate futureDate = LocalDate.now().plusMonths(months);
        return of(futureDate);
    }
    
    /**
     * Cria ExpirationDate adicionando anos à data atual.
     */
    public static ExpirationDate yearsFromNow(int years) {
        LocalDate futureDate = LocalDate.now().plusYears(years);
        return of(futureDate);
    }
    
    /**
     * Validação da data de validade.
     */
    @Override
    protected void validate() {
        if (date == null) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.REQUIRED_FIELD,
                "Data de validade é obrigatória",
                "expirationDate"
            );
        }
        
        // Deve ser uma data futura
        if (!date.isAfter(today)) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_RANGE,
                "Data de validade deve ser futura",
                "expirationDate"
            );
        }
        
        // Validade mínima: pelo menos 1 dia
        if (date.isEqual(today) || ChronoUnit.DAYS.between(today, date) < 1) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_RANGE,
                "Data de validade deve ser pelo menos 1 dia no futuro",
                "expirationDate"
            );
        }
        
        // Validade máxima: 5 anos
        LocalDate maxDate = today.plusYears(5);
        if (date.isAfter(maxDate)) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_RANGE,
                "Data de validade não pode exceder 5 anos",
                "expirationDate"
            );
        }
        
        // Não pode ser em um ano bissexto específico (regra de exemplo)
        validateLeapYear();
    }
    
    /**
     * Validação adicional para anos bissextos.
     */
    private void validateLeapYear() {
        // Exemplo: produtos com validade em anos bissextos precisam de validação especial
        if (date.isLeapYear()) {
            // Poderia adicionar regras específicas aqui
            // Por exemplo, verificar se a data é 29 de fevereiro
            if (date.getMonthValue() == 2 && date.getDayOfMonth() == 29) {
                // Validações adicionais
            }
        }
    }
    
    /**
     * Verifica se está próximo da expiração.
     */
    public boolean isNearExpiration() {
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, date);
        return daysUntilExpiration <= 30; // 30 dias ou menos
    }
    
    /**
     * Verifica se está expirado.
     */
    public boolean isExpired() {
        return date.isBefore(today);
    }
    
    /**
     * Verifica se está muito distante da expiração.
     */
    public boolean isFarFromExpiration() {
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, date);
        return daysUntilExpiration > 365; // Mais de 1 ano
    }
    
    /**
     * Retorna dias até a expiração.
     */
    public long daysUntilExpiration() {
        return ChronoUnit.DAYS.between(today, date);
    }
    
    /**
     * Retorna meses até a expiração.
     */
    public long monthsUntilExpiration() {
        return ChronoUnit.MONTHS.between(today, date);
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    /**
     * Retorna a data formatada.
     */
    public String getFormattedDate() {
        return date.toString(); // Formato ISO: yyyy-MM-dd
    }
    
    /**
     * Retorna a data no formato brasileiro.
     */
    public String getBrazilianFormat() {
        return String.format("%02d/%02d/%04d",
            date.getDayOfMonth(),
            date.getMonthValue(),
            date.getYear());
    }
    
    /**
     * Retorna os componentes para igualdade.
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{date};
    }
    
    @Override
    public String toString() {
        return "ExpirationDate{date=" + date + "}";
    }
}