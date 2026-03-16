package com.pharmacare.infrastructure.persistence.mongodb.adapter.mapper;

import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.products.valueobjects.ProductId;
import com.pharmacare.infrastructure.persistence.mongodb.document.ProductDocument;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMongoMapper {
    ProductMongoMapper INSTANCE = Mappers.getMapper(ProductMongoMapper.class);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "createdAt", expression = "java(product.getCreatedAt() != null ? product.getCreatedAt() : LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "active", defaultValue = "true")
    ProductDocument toDocument(Product product);

    @Mapping(target = "id", expression = "java(ProductId.of(document.getId()))")
    Product toDomain(ProductDocument document);

    /**
     * Atualiza apenas os campos não nulos do documento
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Código não pode ser alterado
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    void updateDocumentFromProduct(Product product, @MappingTarget ProductDocument document);



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
        return productId != null ? String.valueOf(productId.getValue()) : null;
    }
}