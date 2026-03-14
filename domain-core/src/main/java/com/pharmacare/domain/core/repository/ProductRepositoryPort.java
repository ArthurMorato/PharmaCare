package com.pharmacare.domain.core.repository;

import com.pharmacare.domain.core.model.Product;
import com.pharmacare.domain.core.model.ProductId;
import com.pharmacare.sharedkernel.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Porta do repositório de produtos para a camada de domínio.
 * Segrega a interface de persistência da implementação.
 */
public interface ProductRepositoryPort {
    
    /**
     * Salva um produto (criação ou atualização)
     */
    Product save(Product product);
    
    /**
     * Busca produto por ID
     */
    Optional<Product> findById(ProductId id);
    
    /**
     * Busca produto por código de barras
     */
    Optional<Product> findByBarcode(String barcode);
    
    /**
     * Busca produtos ativos com paginação
     */
    Page<Product> findActiveProducts(Pageable pageable);
    
    /**
     * Verifica existência por código de barras
     */
    boolean existsByBarcode(String barcode);
    
    /**
     * Exclui produto por ID
     */
    void deleteById(ProductId id);
    
    /**
     * Busca ou falha - lança exceção se não encontrar
     */
    default Product findByIdOrThrow(ProductId id) {
        return findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Produto não encontrado com ID: " + id.getValue()
            ));
    }
}