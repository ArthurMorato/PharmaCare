package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.inventory.exceptions.ValidationException;
import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;

/**
 * Value Object para descrição de produto.
 * Imutável e validado.
 * 
 * Requisitos:
 * - Máximo 500 caracteres
 * - Pode ser vazio ou nulo (será convertido para string vazia)
 * - Trim automático
 */
public final class Description extends ValueObject {
    
    private final String value;
    private final boolean isEmpty;
    
    /**
     * Construtor privado para garantir imutabilidade.
     */
    private Description(String value) {
        // Converte null para string vazia
        String safeValue = value == null ? "" : value.trim();
        
        this.value = safeValue;
        this.isEmpty = safeValue.isEmpty();
        
        // Validação
        validate();
    }
    
    /**
     * Método fábrica para criar uma Description.
     */
    public static Description from(String value) {
        try {
            return new Description(value);
        } catch (ValidationException e) {
            throw new InvalidValueObjectException(
                "Description",
                value,
                "Descrição inválida: " + e.getMessage()
            );
        }
    }
    
    /**
     * Cria uma descrição vazia.
     */
    public static Description empty() {
        return new Description("");
    }
    
    /**
     * Validação da descrição.
     */
    @Override
    protected void validate() {
        // Valida comprimento máximo
        if (value.length() > 500) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_LENGTH,
                "Descrição não pode ter mais de 500 caracteres",
                "description"
            );
        }
        
        // Valida se não contém apenas espaços (já foi feito trim)
        if (value.isEmpty()) {
            // Descrição vazia é permitida
            return;
        }
        
        // Validações específicas do domínio
        validateDomainRules();
    }
    
    /**
     * Regras de domínio específicas para descrição.
     */
    private void validateDomainRules() {
        // Não pode começar ou terminar com caracteres especiais específicos
        if (value.matches("^[.,;:!?\\-].*") || value.matches(".*[.,;:!?\\-]$")) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Descrição não pode começar ou terminar com pontuação",
                "description"
            );
        }
        
        // Não pode ter múltiplas quebras de linha consecutivas
        if (value.contains("\n\n\n")) {
            throw new ValidationException(
                ValidationException.ValidationErrorCode.INVALID_FORMAT,
                "Descrição não pode ter múltiplas quebras de linha consecutivas",
                "description"
            );
        }
    }
    
    /**
     * Retorna o valor da descrição.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Verifica se a descrição está vazia.
     */
    public boolean isEmpty() {
        return isEmpty;
    }
    
    /**
     * Retorna o comprimento da descrição.
     */
    public int length() {
        return value.length();
    }
    
    /**
     * Retorna uma descrição truncada no comprimento especificado.
     */
    public String truncate(int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        
        String truncated = value.substring(0, maxLength - 3);
        return truncated + "...";
    }
    
    /**
     * Retorna a descrição em formato HTML seguro (quebra de linhas para <br>).
     */
    public String toHtmlSafe() {
        return value.replace("\n", "<br>")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
    
    /**
     * Concatena outra descrição.
     */
    public Description concat(Description other) {
        if (this.isEmpty()) return other;
        if (other.isEmpty()) return this;
        
        String combined = this.value + "\n\n" + other.value;
        return Description.from(combined);
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
        return "Description{length=" + value.length() + ", isEmpty=" + isEmpty + "}";
    }
}