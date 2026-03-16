package com.pharmacare.domain.core.products.specifications;

import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.products.enums.ProductStatus;

/**
 * Especificação para produtos ativos.
 */
public class ActiveProductsSpecification implements ProductSpecification {

    @Override
    public boolean isSatisfiedBy(Product product) {
        return product != null && product.getStatus() == ProductStatus.ACTIVE;
    }

    /**
     * Produtos ativos com preço maior que zero.
     */
    public static class WithPositivePrice extends ActiveProductsSpecification {
        @Override
        public boolean isSatisfiedBy(Product product) {
            return super.isSatisfiedBy(product) &&
                    product.getPrice() != null &&
                    !product.getPrice().isZero();
        }
    }

    /**
     * Produtos ativos e disponíveis para venda.
     */
    public static class AvailableForSale extends ActiveProductsSpecification {
        @Override
        public boolean isSatisfiedBy(Product product) {
            return super.isSatisfiedBy(product) && product.isAvailableForSale();
        }
    }
}