package com.pharmacare.domain.core.products.specifications;

import com.pharmacare.domain.core.products.aggregates.Product;

/**
 * Interface base para especificações de produto.
 * Segue o padrão Specification do DDD.
 */
public interface ProductSpecification {
    
    /**
     * Avalia se o produto satisfaz a especificação.
     */
    boolean isSatisfiedBy(Product product);
    
    /**
     * Combina duas especificações com AND.
     */
    default ProductSpecification and(ProductSpecification other) {
        return new AndSpecification(this, other);
    }
    
    /**
     * Combina duas especificações com OR.
     */
    default ProductSpecification or(ProductSpecification other) {
        return new OrSpecification(this, other);
    }
    
    /**
     * Nega a especificação.
     */
    default ProductSpecification not() {
        return new NotSpecification(this);
    }
    
    /**
     * Especificação AND.
     */
    class AndSpecification implements ProductSpecification {
        private final ProductSpecification left;
        private final ProductSpecification right;
        
        public AndSpecification(ProductSpecification left, ProductSpecification right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public boolean isSatisfiedBy(Product product) {
            return left.isSatisfiedBy(product) && right.isSatisfiedBy(product);
        }
    }
    
    /**
     * Especificação OR.
     */
    class OrSpecification implements ProductSpecification {
        private final ProductSpecification left;
        private final ProductSpecification right;
        
        public OrSpecification(ProductSpecification left, ProductSpecification right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public boolean isSatisfiedBy(Product product) {
            return left.isSatisfiedBy(product) || right.isSatisfiedBy(product);
        }
    }
    
    /**
     * Especificação NOT.
     */
    class NotSpecification implements ProductSpecification {
        private final ProductSpecification spec;
        
        public NotSpecification(ProductSpecification spec) {
            this.spec = spec;
        }
        
        @Override
        public boolean isSatisfiedBy(Product product) {
            return !spec.isSatisfiedBy(product);
        }
    }
}