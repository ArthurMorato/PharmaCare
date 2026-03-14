package com.pharmacare.domain.core.products.specifications;

import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.products.valueobjects.Money;

import java.math.BigDecimal;

/**
 * Especificação por faixa de preço.
 */
public class PriceRangeSpecification implements ProductSpecification {
    
    private final Money minPrice;
    private final Money maxPrice;
    
    public PriceRangeSpecification(Money minPrice, Money maxPrice) {
        if (minPrice != null && maxPrice != null && 
            minPrice.getAmount().compareTo(maxPrice.getAmount()) > 0) {
            throw new IllegalArgumentException("Preço mínimo não pode ser maior que preço máximo");
        }
        
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    public PriceRangeSpecification(BigDecimal minPrice, BigDecimal maxPrice) {
        this(
            minPrice != null ? Money.of(minPrice) : null,
            maxPrice != null ? Money.of(maxPrice) : null
        );
    }
    
    @Override
    public boolean isSatisfiedBy(Product product) {
        if (product == null || product.getPrice() == null) {
            return false;
        }
        
        Money productPrice = product.getPrice();
        
        boolean satisfiesMin = minPrice == null || 
                              productPrice.compareTo(minPrice) >= 0;
        boolean satisfiesMax = maxPrice == null || 
                              productPrice.compareTo(maxPrice) <= 0;
        
        return satisfiesMin && satisfiesMax;
    }
    
    /**
     * Especificação para produtos com preço menor que.
     */
    public static class PriceLessThan extends PriceRangeSpecification {
        public PriceLessThan(Money maxPrice) {
            super(null, maxPrice);
        }
    }
    
    /**
     * Especificação para produtos com preço maior que.
     */
    public static class PriceGreaterThan extends PriceRangeSpecification {
        public PriceGreaterThan(Money minPrice) {
            super(minPrice, null);
        }
    }
    
    /**
     * Especificação para produtos dentro de um orçamento.
     */
    public static class WithinBudget extends PriceRangeSpecification {
        public WithinBudget(Money budget) {
            super(Money.zero(), budget);
        }
    }
}