package com.pharmacare.infrastructure.persistence.mongodb.adapter.mapper;

import com.pharmacare.domain.core.model.*;
import com.pharmacare.infrastructure.persistence.mongodb.document.ProductDocument;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper responsável pela conversão entre Product (domínio) e ProductDocument (persistência).
 * Usa MapStruct para geração automática de código.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface ProductMongoMapper {
    
    /**
     * Converte ProductDocument para Product (domínio)
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "toProductId")
    @Mapping(target = "name", expression = "java(ProductName.of(productDocument.getName()))")
    @Mapping(target = "price", expression = "java(Money.of(productDocument.getPrice(), \"BRL\"))")
    Product toDomain(ProductDocument productDocument);
    
    /**
     * Converte Product para ProductDocument
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "toDocumentId")
    @Mapping(target = "name", source = "name.value")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "createdAt", defaultExpression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", defaultExpression = "java(java.time.LocalDateTime.now())")
    ProductDocument toDocument(Product product);
    
    /**
     * Atualiza documento existente a partir do domínio
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDocumentFromDomain(Product product, @MappingTarget ProductDocument productDocument);
    
    /**
     * Converte lista de documentos para lista de domínios
     */
    List<Product> toDomainList(List<ProductDocument> productDocuments);
    
    /**
     * Converte Page de documentos para Page de domínios
     */
    default Page<Product> toDomainPage(Page<ProductDocument> page) {
        return page.map(this::toDomain);
    }
    
    /**
     * Converte String ID para ProductId
     */
    @Named("toProductId")
    default ProductId toProductId(String id) {
        return id != null ? ProductId.from(id) : null;
    }
    
    /**
     * Converte ProductId para String ID
     */
    @Named("toDocumentId")
    default String toDocumentId(ProductId productId) {
        return productId != null ? productId.getValue() : null;
    }
}