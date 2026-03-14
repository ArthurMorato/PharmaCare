package com.pharmacare.infrastructure.persistence.mongodb.repository;

import com.pharmacare.domain.core.model.Category;
import com.pharmacare.domain.core.model.ProductStatus;
import com.pharmacare.infrastructure.persistence.mongodb.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data MongoDB para operações de persistência.
 * Define queries customizadas e métodos derivados.
 */
@Repository
public interface ProductMongoRepository extends MongoRepository<ProductDocument, String> {
    
    /**
     * Busca por código de barras (índice único)
     */
    Optional<ProductDocument> findByBarcode(String barcode);
    
    /**
     * Verifica existência por código de barras
     */
    boolean existsByBarcode(String barcode);
    
    /**
     * Busca produtos ativos com paginação
     */
    Page<ProductDocument> findByStatus(ProductStatus status, Pageable pageable);
    
    /**
     * Busca produtos por categoria e status
     */
    Page<ProductDocument> findByCategoryAndStatus(
        Category category, 
        ProductStatus status, 
        Pageable pageable
    );
    
    /**
     * Busca produtos com nome contendo texto (case-insensitive)
     * Usa regex para busca parcial
     */
    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'status': ?1 }")
    Page<ProductDocument> findByNameContainingAndStatus(
        String name, 
        ProductStatus status, 
        Pageable pageable
    );
    
    /**
     * Busca produtos com estoque abaixo do mínimo
     */
    @Query("{ 'currentStock': { $lt: '$minimumStock' }, 'status': 'ACTIVE' }")
    Page<ProductDocument> findLowStockProducts(Pageable pageable);
    
    /**
     * Busca produtos que requerem prescrição
     */
    Page<ProductDocument> findByRequiresPrescriptionAndStatus(
        boolean requiresPrescription, 
        ProductStatus status, 
        Pageable pageable
    );
}