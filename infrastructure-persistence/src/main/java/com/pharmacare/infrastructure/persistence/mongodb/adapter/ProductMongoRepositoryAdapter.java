package com.pharmacare.infrastructure.persistence.mongodb.adapter;

import com.pharmacare.domain.core.model.Product;
import com.pharmacare.domain.core.model.ProductId;
import com.pharmacare.domain.core.repository.ProductRepositoryPort;
import com.pharmacare.infrastructure.persistence.mongodb.adapter.mapper.ProductMongoMapper;
import com.pharmacare.infrastructure.persistence.mongodb.document.ProductDocument;
import com.pharmacare.infrastructure.persistence.mongodb.repository.ProductMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador que implementa ProductRepositoryPort usando MongoDB.
 * Faz a ponte entre o domínio e a infraestrutura de persistência.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductMongoRepositoryAdapter implements ProductRepositoryPort {
    
    private final ProductMongoRepository productMongoRepository;
    private final ProductMongoMapper productMongoMapper;
    
    @Override
    @Transactional
    public Product save(Product product) {
        log.debug("Salvando produto: {}", product.getId().getValue());
        
        ProductDocument document;
        
        if (product.getId() != null) {
            // Atualização
            document = productMongoRepository.findById(product.getId().getValue())
                .orElseGet(() -> {
                    log.warn("Produto não encontrado para atualização, criando novo: {}", 
                            product.getId().getValue());
                    return new ProductDocument();
                });
            
            productMongoMapper.updateDocumentFromDomain(product, document);
            document.markAsUpdated();
        } else {
            // Criação
            document = productMongoMapper.toDocument(product);
        }
        
        ProductDocument savedDocument = productMongoRepository.save(document);
        log.info("Produto salvo com sucesso: {}", savedDocument.getId());
        
        return productMongoMapper.toDomain(savedDocument);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId id) {
        log.debug("Buscando produto por ID: {}", id.getValue());
        
        return productMongoRepository.findById(id.getValue())
            .map(productMongoMapper::toDomain)
            .map(product -> {
                log.debug("Produto encontrado: {}", id.getValue());
                return product;
            });
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findByBarcode(String barcode) {
        log.debug("Buscando produto por código de barras: {}", barcode);
        
        return productMongoRepository.findByBarcode(barcode)
            .map(productMongoMapper::toDomain)
            .map(product -> {
                log.debug("Produto encontrado pelo barcode: {}", barcode);
                return product;
            });
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Product> findActiveProducts(Pageable pageable) {
        log.debug("Buscando produtos ativos com paginação: page={}, size={}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        
        return productMongoMapper.toDomainPage(
            productMongoRepository.findByStatus(
                com.pharmacare.domain.core.model.ProductStatus.ACTIVE, 
                pageable
            )
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByBarcode(String barcode) {
        log.debug("Verificando existência por código de barras: {}", barcode);
        return productMongoRepository.existsByBarcode(barcode);
    }
    
    @Override
    @Transactional
    public void deleteById(ProductId id) {
        log.debug("Excluindo produto por ID: {}", id.getValue());
        
        if (productMongoRepository.existsById(id.getValue())) {
            productMongoRepository.deleteById(id.getValue());
            log.info("Produto excluído com sucesso: {}", id.getValue());
        } else {
            log.warn("Tentativa de exclusão de produto inexistente: {}", id.getValue());
        }
    }
}