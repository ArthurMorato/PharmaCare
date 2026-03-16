package com.pharmacare.domain.core.products.aggregates;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.products.valueobjects.ProductId;
import com.pharmacare.domain.core.products.valueobjects.Category;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends ValueObject {

    @NotNull
    private ProductId id;

    @NotNull
    @Size(min = 3, max = 50)
    private String code;

    @NotNull
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @Size(min = 2, max = 50)
    private String brand;

    @NotNull
    private Category category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal unitPrice;

    @NotNull
    @Min(0)
    private Integer quantityInStock;

    @NotNull
    @Size(min = 2, max = 100)
    private String supplier;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private Boolean active = true;

    // Factory method for creation
    public static Product create(
            String code,
            String name,
            String description,
            String brand,
            Category category,
            BigDecimal unitPrice,
            Integer quantityInStock,
            String supplier) {

        Product product = Product.builder()
                .id(ProductId.generate())
                .code(code)
                .name(name)
                .description(description)
                .brand(brand)
                .category(category)
                .unitPrice(unitPrice)
                .quantityInStock(quantityInStock)
                .supplier(supplier)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();

        product.validate();
        return product;
    }

    // Business methods
    public void updateStock(Integer newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.quantityInStock = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        this.unitPrice = newPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return this.quantityInStock < 10;
    }

    public BigDecimal calculateTotalValue() {
        return unitPrice.multiply(new BigDecimal(quantityInStock));
    }

    @Override
    protected Object[] getEqualityComponents() {
        return new Object[0];
    }

    // Validation method
    public void validate() {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Product code is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Product name is required");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Product price must be greater than zero");
        }
        if (category == null) {
            throw new IllegalStateException("Product category is required");
        }
        if (quantityInStock == null || quantityInStock < 0) {
            throw new IllegalStateException("Product quantity in stock must be non-negative");
        }
        if (supplier == null || supplier.trim().isEmpty()) {
            throw new IllegalStateException("Product supplier is required");
        }
    }
}
