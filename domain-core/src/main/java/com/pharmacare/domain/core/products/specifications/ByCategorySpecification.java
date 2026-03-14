package com.pharmacare.domain.core.products.specifications;

import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.products.enums.ProductCategory;

import java.util.Arrays;
import java.util.List;

/**
 * Especificação por categoria de produto.
 */
public class ByCategorySpecification implements ProductSpecification {
    
    private final ProductCategory category;
    private final List<ProductCategory> categories;
    
    public ByCategorySpecification(ProductCategory category) {
        this.category = category;
        this.categories = null;
    }
    
    public ByCategorySpecification(List<ProductCategory> categories) {
        this.category = null;
        this.categories = categories;
    }
    
    public ByCategorySpecification(ProductCategory... categories) {
        this.category = null;
        this.categories = Arrays.asList(categories);
    }
    
    @Override
    public boolean isSatisfiedBy(Product product) {
        if (product == null) return false;
        
        if (category != null) {
            return product.getCategory() == category;
        }
        
        if (categories != null && !categories.isEmpty()) {
            return categories.contains(product.getCategory());
        }
        
        return false;
    }
    
    /**
     * Especificação para produtos controlados.
     */
    public static class ControlledProducts extends ByCategorySpecification {
        public ControlledProducts() {
            super(ProductCategory.medicineCategories());
        }
        
        @Override
        public boolean isSatisfiedBy(Product product) {
            return product != null && product.isControlled();
        }
    }
    
    /**
     * Especificação para produtos isentos de impostos.
     */
    public static class TaxExemptProducts extends ByCategorySpecification {
        public TaxExemptProducts() {
            super((ProductCategory) null);
        }
        
        @Override
        public boolean isSatisfiedBy(Product product) {
            return product != null && product.isTaxExempt();
        }
    }
}