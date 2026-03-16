package com.pharmacare.application.product.handlers;

import com.pharmacare.application.product.commands.CreateProductCommand;
import com.pharmacare.application.product.responses.ProductResponse;

/**
 * Use case para registro de novo produto.
 * 
 * Porta de entrada para a operação de criação de produto.
 * Segregada da implementação para manter a inversão de dependência.
 */
public interface RegisterProductUseCase {
    
    /**
     * Executa o registro de um novo produto.
     * 
     * @param command Dados necessários para criar o produto
     * @return Response com os dados do produto criado
     * @throws com.pharmacare.application.common.exception.BusinessException Se houver violação de regra de negócio
     * @throws com.pharmacare.application.common.exception.ValidationException Se houver erro de validação
     */
    ProductResponse execute(CreateProductCommand command);
    
    /**
     * Executa o registro de um novo produto de forma assíncrona.
     * 
     * @param command Dados necessários para criar o produto
     * @return ID do produto criado (para acompanhamento assíncrono)
     */
    default String executeAsync(CreateProductCommand command) {
        // Implementação default sincrona
        return execute(command).productId();
    }
}