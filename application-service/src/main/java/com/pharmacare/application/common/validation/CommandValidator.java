package com.pharmacare.application.common.validation;

import com.pharmacare.application.common.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Componente para validação de commands usando Bean Validation.
 */
@Component
public class CommandValidator {
    
    private final Validator validator;
    
    public CommandValidator(Validator validator) {
        this.validator = validator;
    }
    
    /**
     * Valida um command e lança ValidationException se houver violações.
     */
    public <T> void validate(T command) {
        Set<ConstraintViolation<T>> violations = validator.validate(command);
        
        if (!violations.isEmpty()) {
            var validationErrors = violations.stream()
                .map(violation -> new ValidationException.ValidationError(
                    violation.getPropertyPath().toString(),
                    violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                    violation.getMessage()
                ))
                .collect(Collectors.toList());
            
            throw new ValidationException(
                "VALIDATION_ERROR",
                "Erros de validação encontrados no comando",
                validationErrors
            );
        }
        
        // Valida regras customizadas se o command tiver o método
        try {
            var method = command.getClass().getMethod("validateCustomRules");
            method.invoke(command);
        } catch (NoSuchMethodException e) {
            // Não tem validação customizada, tudo bem
        } catch (Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw new ValidationException(
                    "CUSTOM_VALIDATION_ERROR",
                    e.getCause().getMessage()
                );
            }
            throw new RuntimeException("Erro ao validar command", e);
        }
    }
    
    /**
     * Valida e retorna as violações sem lançar exceção.
     */
    public <T> Set<ConstraintViolation<T>> validateSilently(T command) {
        return validator.validate(command);
    }
    
    /**
     * Verifica se o command é válido.
     */
    public <T> boolean isValid(T command) {
        return validator.validate(command).isEmpty();
    }
}