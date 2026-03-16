package com.pharmacare.domain.core.common;

import java.util.Arrays;
import java.util.Objects;

/**
 * Classe base abstrata para todos os Value Objects.
 * Implementa equals() e hashCode() baseados nos componentes do Value Object.
 */
public abstract class ValueObject {

    /**
     * Retorna os componentes que definem a identidade do Value Object.
     * Deve ser implementado pelas subclasses.
     */
    protected abstract Object[] getEqualityComponents();

    /**
     * Dois Value Objects são iguais se todos os seus componentes são iguais.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ValueObject other = (ValueObject) obj;
        Object[] theseComponents = getEqualityComponents();
        Object[] thoseComponents = other.getEqualityComponents();

        if (theseComponents.length != thoseComponents.length) return false;

        for (int i = 0; i < theseComponents.length; i++) {
            if (!Objects.equals(theseComponents[i], thoseComponents[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * hashCode baseado em todos os componentes.
     */
    @Override
    public int hashCode() {
        Object[] components = getEqualityComponents();
        return Objects.hash(components);
    }

    /**
     * Representação em string para debugging.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "components=" + Arrays.toString(getEqualityComponents()) +
                '}';
    }

    /**
     * Valida se o objeto está em um estado válido.
     * Lança ValidationException se não estiver.
     */
    protected abstract void validate();
}