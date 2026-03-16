package com.pharmacare.application.product.mappers;

import com.pharmacare.application.product.commands.CreateProductCommand;
import com.pharmacare.application.product.responses.ProductResponse;
import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.inventory.valueobjects.Money;
import org.mapstruct.*;

import java.math.BigDecimal;

/**
 * Mapper para conversão entre objetos de produto.
 * 
 * Usa MapStruct para geração de código de conversão type-safe.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {
    
    /**
     * Converte um Product aggregate para ProductResponse.
     */
    @Mapping(target = "productId", source = "id")
    @Mapping(target = "name", source = "productName.value")
    @Mapping(target = "barcode", source = "barcode.value")
    @Mapping(target = "description", source = "description.value")
    @Mapping(target = "brand", source = "brand.value")
    @Mapping(target = "categoryDisplayName", source = "category.displayName")
    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "cost", source = "cost.amount")
    @Mapping(target = "statusDisplayName", source = "status.displayName")
    @Mapping(target = "totalTax", expression = "java(calculateTotalTax(product))")
    ProductResponse toResponse(Product product);
    
    /**
     * Converte um CreateProductCommand para Product (usado em casos específicos).
     * Nota: A criação do Product é feita pela factory method do aggregate.
     */
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "barcode", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "cost", ignore = true)
    Product toDomain(CreateProductCommand command);
    
    /**
     * Atualiza um Product existente com dados do command.
     */
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "barcode", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "cost", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "domainEvents", ignore = true)
    void updateFromCommand(CreateProductCommand command, @MappingTarget Product product);
    
    /**
     * Calcula o total de impostos.
     */
    default BigDecimal calculateTotalTax(Product product) {
        if (product == null || !product.isTaxable()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal icms = product.getIcmsTax() != null ? product.getIcmsTax() : BigDecimal.ZERO;
        BigDecimal pis = product.getPisTax() != null ? product.getPisTax() : BigDecimal.ZERO;
        BigDecimal cofins = product.getCofinsTax() != null ? product.getCofinsTax() : BigDecimal.ZERO;
        
        return icms.add(pis).add(cofins);
    }
    
    /**
     * Converte BigDecimal para Money.
     */
    default Money toMoney(BigDecimal amount) {
        return amount != null ? Money.of(amount) : Money.zero();
    }
    
    /**
     * Converte Money para BigDecimal.
     */
    default BigDecimal toBigDecimal(Money money) {
        return money != null ? money.getAmount() : BigDecimal.ZERO;
    }
    
    /**
     * Configurações pós-mapping.
     */
    @AfterMapping
    default void afterMapping(@MappingTarget ProductResponse.ProductResponseBuilder builder, Product product) {
        // Adiciona lógica adicional se necessário
        if (product != null && product.isActive()) {
            builder.currentStock(0); // Será populado pelo serviço de estoque
        }
    }
}