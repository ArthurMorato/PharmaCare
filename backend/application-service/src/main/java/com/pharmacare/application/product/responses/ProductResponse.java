package com.pharmacare.application.product.responses;

import com.pharmacare.domain.core.inventory.enums.ProductCategory;
import com.pharmacare.domain.core.products.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO para operações de produto.
 * 
 * Contém apenas dados de leitura para exposição via API.
 * Todos os campos são imutáveis.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
    
    String productId,
    
    String name,
    
    String barcode,
    
    String description,
    
    String brand,
    
    ProductCategory category,
    
    String categoryDisplayName,
    
    BigDecimal price,
    
    BigDecimal cost,
    
    Integer currentStock,
    
    Integer minimumStock,
    
    String unit,
    
    Boolean controlled,
    
    Boolean taxable,
    
    String ncmCode,
    
    String cestCode,
    
    String manufacturer,
    
    String supplier,
    
    ProductStatus status,
    
    String statusDisplayName,
    
    BigDecimal icmsTax,
    
    BigDecimal pisTax,
    
    BigDecimal cofinsTax,
    
    BigDecimal totalTax,
    
    Set<String> tags,
    
    String imageUrl,
    
    Boolean requiresPrescription,
    
    String instructions,
    
    String sideEffects,
    
    String composition,
    
    String anvisaRegistration,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt,
    
    String createdBy,
    
    String lastModifiedBy
    
) {
    
    /**
     * Factory method para criar response vazio (usado em testes).
     */
    public static ProductResponse empty() {
        return new ProductResponse(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
    
    /**
     * Calcula a margem de lucro.
     */
    public BigDecimal calculateProfitMargin() {
        if (price == null || cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return price.subtract(cost).divide(cost, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
    
    /**
     * Calcula o preço com impostos.
     */
    public BigDecimal calculatePriceWithTaxes() {
        if (price == null || totalTax == null) {
            return price != null ? price : BigDecimal.ZERO;
        }
        return price.add(price.multiply(totalTax.divide(BigDecimal.valueOf(100))));
    }
    
    /**
     * Verifica se está abaixo do estoque mínimo.
     */
    public boolean isBelowMinimumStock() {
        return currentStock != null && minimumStock != null 
            && currentStock < minimumStock;
    }
    
    /**
     * Verifica se é um medicamento controlado.
     */
    public boolean isControlledMedicine() {
        return Boolean.TRUE.equals(controlled) 
            && category == ProductCategory.MEDICINE;
    }
    
    /**
     * Retorna o preço formatado.
     */
    public String getFormattedPrice() {
        return price != null ? String.format("R$ %.2f", price) : "R$ 0,00";
    }
    
    /**
     * Retorna o preço com impostos formatado.
     */
    public String getFormattedPriceWithTaxes() {
        BigDecimal priceWithTaxes = calculatePriceWithTaxes();
        return String.format("R$ %.2f", priceWithTaxes);
    }
}