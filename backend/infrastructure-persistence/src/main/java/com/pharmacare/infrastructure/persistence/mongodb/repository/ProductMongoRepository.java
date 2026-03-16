package com.pharmacare.infrastructure.persistence.mongodb.repository;

import com.pharmacare.infrastructure.persistence.mongodb.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMongoRepository extends MongoRepository<ProductDocument, String> {

    Optional<ProductDocument> findByCode(String code);

    Optional<ProductDocument> findByCodeAndActiveTrue(String code);

    List<ProductDocument> findByNameContainingIgnoreCase(String name);

    Page<ProductDocument> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<ProductDocument> findByCategory(String category);

    Page<ProductDocument> findByCategory(String category, Pageable pageable);

    List<ProductDocument> findByBrand(String brand);

    List<ProductDocument> findByBrandAndCategory(String brand, String category);

    List<ProductDocument> findBySupplier(String supplier);

    @Query("{ 'quantityInStock': { $lt: ?0 }, 'active': true }")
    List<ProductDocument> findProductsWithLowStock(Integer threshold);

    @Query("{ 'quantityInStock': { $lt: ?0 }, 'active': true }")
    Page<ProductDocument> findProductsWithLowStock(Integer threshold, Pageable pageable);

    @Query("{ " +
            "'$or': [ " +
            "  { 'name': { '$regex': ?0, '$options': 'i' } }, " +
            "  { 'code': { '$regex': ?0, '$options': 'i' } }, " +
            "  { 'brand': { '$regex': ?0, '$options': 'i' } } " +
            "], " +
            "'active': true " +
            "}")
    Page<ProductDocument> searchProducts(String searchTerm, Pageable pageable);

    @Query("{ " +
            "'category': ?0, " +
            "'unitPrice': { $gte: ?1, $lte: ?2 }, " +
            "'active': true " +
            "}")
    List<ProductDocument> findByCategoryAndPriceRange(String category, BigDecimal minPrice, BigDecimal maxPrice);

    long countByCategory(String category);

    long countByActiveTrue();

    boolean existsByCode(String code);

    void deleteByCode(String code);
}
