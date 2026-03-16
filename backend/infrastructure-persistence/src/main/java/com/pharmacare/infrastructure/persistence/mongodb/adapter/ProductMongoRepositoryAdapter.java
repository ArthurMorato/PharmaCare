package com.pharmacare.infrastructure.persistence.mongodb.adapter;

import com.pharmacare.domain.core.inventory.exceptions.ProductNotFoundException;
import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.products.valueobjects.ProductId;
import com.pharmacare.domain.core.repository.ProductRepositoryPort;
import com.pharmacare.infrastructure.persistence.mongodb.adapter.mapper.ProductMongoMapper;
import com.pharmacare.infrastructure.persistence.mongodb.document.ProductDocument;
import com.pharmacare.infrastructure.persistence.mongodb.repository.ProductMongoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Validated
@Transactional(readOnly = true)
public class ProductMongoRepositoryAdapter implements ProductRepositoryPort {

    private final ProductMongoRepository productMongoRepository;
    private final ProductMongoMapper mapper;

    public ProductMongoRepositoryAdapter(ProductMongoRepository productMongoRepository,
                                         ProductMongoMapper mapper) {
        this.productMongoRepository = productMongoRepository;
        this.mapper = mapper;
        log.info("ProductMongoRepositoryAdapter inicializado");
    }

    @Override
    public Optional<Product> findById(@NotNull ProductId id) {
        log.debug("Buscando produto por ID: {}", id.getValue());
        return productMongoRepository.findById(id.getValue())
                .filter(ProductDocument::getActive)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByBarcode(String barcode) {
        return Optional.empty();
    }

    @Override
    public Page<Product> findActiveProducts(Pageable pageable) {
        return null;
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return false;
    }

    @Override
    public Product findByIdOrThrow(ProductId id) {
        log.debug("Buscando produto por ID ou lançando exceção: {}", id.getValue());
        return findById(id)
                .orElseThrow(() -> ProductNotFoundException.forId("Produto", id.getValue()));
    }

    public Optional<Product> findByCode(@NotNull String code) {
        log.debug("Buscando produto por código: {}", code);
        return productMongoRepository.findByCodeAndActiveTrue(code)
                .map(mapper::toDomain);
    }

    public List<Product> findByNameContaining(@NotNull String name) {
        log.debug("Buscando produtos por nome contendo: {}", name);
        return productMongoRepository.findByNameContainingIgnoreCase(name).stream()
                .filter(ProductDocument::getActive)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public Page<Product> findByNameContaining(@NotNull String name, Pageable pageable) {
        log.debug("Buscando produtos paginados por nome contendo: {}", name);
        Page<ProductDocument> page = productMongoRepository.findByNameContainingIgnoreCase(name, pageable);
        List<Product> products = page.getContent().stream()
                .filter(ProductDocument::getActive)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(products, pageable, page.getTotalElements());
    }

    public List<Product> findByCategory(@NotNull String category) {
        log.debug("Buscando produtos por categoria: {}", category);
        return productMongoRepository.findByCategory(category).stream()
                .filter(ProductDocument::getActive)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Product> findByBrand(@NotNull String brand) {
        log.debug("Buscando produtos por marca: {}", brand);
        return productMongoRepository.findByBrand(brand).stream()
                .filter(ProductDocument::getActive)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Product> findProductsWithLowStock(@NotNull Integer threshold) {
        log.debug("Buscando produtos com estoque baixo (threshold: {})", threshold);
        return productMongoRepository.findProductsWithLowStock(threshold).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Product save(@Valid @NotNull Product product) {
        log.debug("Salvando produto: {}", product.getName());

        // Verifica se é uma atualização
        Optional<ProductDocument> existingDocument = product.getId() != null ?
                productMongoRepository.findById(product.getId().getValue()) :
                Optional.empty();

        if (existingDocument.isPresent()) {
            // Atualização
            ProductDocument document = existingDocument.get();
            mapper.updateDocumentFromProduct(product, document);
            document.setUpdatedAt(java.time.LocalDateTime.now());
            ProductDocument saved = productMongoRepository.save(document);
            log.info("Produto atualizado: {} -> {}", product.getName(), saved.getId());
            return mapper.toDomain(saved);
        } else {
            // Inserção
            ProductDocument document = mapper.toDocument(product);
            document.setActive(true);
            ProductDocument saved = productMongoRepository.save(document);
            log.info("Produto criado: {} -> {}", product.getName(), saved.getId());
            return mapper.toDomain(saved);
        }
    }

    @Override
    @Transactional
    public void deleteById(@NotNull ProductId id) {
        log.debug("Deletando produto por ID: {}", id.getValue());
        // Soft delete
        productMongoRepository.findById(id.getValue())
                .ifPresent(document -> {
                    document.setActive(false);
                    document.setUpdatedAt(java.time.LocalDateTime.now());
                    productMongoRepository.save(document);
                    log.info("Produto deletado (soft delete): {}", id.getValue());
                });
    }

    @Transactional
    public void deleteByCode(@NotNull String code) {
        log.debug("Deletando produto por código: {}", code);
        productMongoRepository.findByCode(code)
                .ifPresent(document -> {
                    document.setActive(false);
                    document.setUpdatedAt(java.time.LocalDateTime.now());
                    productMongoRepository.save(document);
                    log.info("Produto deletado por código (soft delete): {}", code);
                });
    }

    public List<Product> findAll() {
        log.debug("Buscando todos os produtos ativos");
        return productMongoRepository.findAll().stream()
                .filter(ProductDocument::getActive)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public Page<Product> findAll(Pageable pageable) {
        log.debug("Buscando todos os produtos paginados");
        Page<ProductDocument> page = productMongoRepository.findAll(pageable);
        List<Product> products = page.getContent().stream()
                .filter(ProductDocument::getActive)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(products, pageable, page.getTotalElements());
    }

    public boolean existsByCode(@NotNull String code) {
        return productMongoRepository.existsByCode(code);
    }

    public Page<Product> searchProducts(@NotNull String searchTerm, Pageable pageable) {
        log.debug("Pesquisando produtos com termo: {}", searchTerm);
        Page<ProductDocument> page = productMongoRepository.searchProducts(searchTerm, pageable);
        List<Product> products = page.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(products, pageable, page.getTotalElements());
    }

    public List<Product> findByCategoryAndPriceRange(String category, BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Buscando produtos por categoria e faixa de preço: {} [{} - {}]",
                category, minPrice, maxPrice);
        return productMongoRepository.findByCategoryAndPriceRange(category, minPrice, maxPrice)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByCategory(String category) {
        return productMongoRepository.countByCategory(category);
    }

    public long countActiveProducts() {
        return productMongoRepository.countByActiveTrue();
    }
}
