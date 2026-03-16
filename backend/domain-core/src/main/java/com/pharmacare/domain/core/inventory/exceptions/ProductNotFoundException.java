package com.pharmacare.domain.core.inventory.exceptions;

import com.pharmacare.domain.core.common.DomainException;

/**
 * Exceção lançada quando uma entidade não é encontrada no repositório.
 * Esta é uma exceção de domínio que representa uma regra de negócio.
 */
public class ProductNotFoundException extends DomainException {

    /**
     * Constrói uma nova exceção com a mensagem especificada.
     *
     * @param message a mensagem de detalhe
     */
    public ProductNotFoundException(String message) {
        super(message);
    }

    /**
     * Constrói uma nova exceção com a mensagem especificada e causa.
     *
     * @param message a mensagem de detalhe
     * @param cause a causa da exceção
     */
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Método factory para criar exceção com template de mensagem.
     *
     * @param entityType tipo da entidade (ex: "Produto", "Cliente")
     * @param identifier identificador não encontrado
     * @return instância da exceção
     */
    public static ProductNotFoundException of(String entityType, Object identifier) {
        return new ProductNotFoundException(
                String.format("%s não encontrado com identificador: %s",
                        entityType, identifier)
        );
    }

    /**
     * Método factory para criar exceção para entidade por ID.
     *
     * @param entityType tipo da entidade
     * @param id ID não encontrado
     * @return instância da exceção
     */
    public static ProductNotFoundException forId(String entityType, Object id) {
        return new ProductNotFoundException(
                String.format("%s não encontrado com ID: %s", entityType, id)
        );
    }
}
