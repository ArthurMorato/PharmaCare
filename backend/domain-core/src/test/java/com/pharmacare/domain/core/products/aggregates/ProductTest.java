package com.pharmacare.domain.core.products.aggregates;

import com.pharmacare.domain.core.inventory.enums.ProductCategory;
import com.pharmacare.domain.core.inventory.valueobjects.Money;
import com.pharmacare.domain.core.products.enums.ProductStatus;
import com.pharmacare.domain.core.common.DomainEvent;
import com.pharmacare.domain.core.inventory.events.ProductRegisteredEvent;
import com.pharmacare.domain.core.inventory.events.ProductUpdatedEvent;
import com.pharmacare.domain.core.products.events.ProductDiscontinuedEvent;
import com.pharmacare.domain.core.products.exceptions.ProductDomainException;
import com.pharmacare.domain.core.products.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para Product Aggregate Root.
 */
class ProductTest {
    
    private ProductName productName;
    private Barcode barcode;
    private Description description;
    private Brand brand;
    private Money price;
    
    @BeforeEach
    void setUp() {
        productName = ProductName.from("Paracetamol 500mg");
        barcode = Barcode.from("7891234567895");
        description = Description.from("Analgésico e antitérmico");
        brand = Brand.from("Medley");
        price = Money.of(12.99);
    }
    
    @Test
    void shouldRegisterNewProduct() {
        // Act
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        // Assert
        assertNotNull(product);
        assertNotNull(product.getId());
        assertEquals(productName, product.getName());
        assertEquals(barcode, product.getBarcode());
        assertEquals(description, product.getDescription());
        assertEquals(brand, product.getBrand());
        assertEquals(ProductCategory.MEDICINE, product.getCategory());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(price, product.getPrice());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
        assertEquals(1, product.getVersion());
        
        // Verifica evento de domínio
        List<DomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductRegisteredEvent);
    }
    
    @Test
    void shouldReconstituteExistingProduct() {
        // Arrange
        ProductId productId = ProductId.generate();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        // Act
        Product product = Product.reconstitute(
            productId,
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            ProductStatus.ACTIVE,
            price,
            createdAt,
            updatedAt,
            2
        );
        
        // Assert
        assertNotNull(product);
        assertEquals(productId, product.getId());
        assertEquals(createdAt, product.getCreatedAt());
        assertEquals(updatedAt, product.getUpdatedAt());
        assertEquals(2, product.getVersion());
        
        // Não deve ter eventos de domínio ao reconstituir
        assertTrue(product.getDomainEvents().isEmpty());
    }
    
    @Test
    void shouldUpdateProduct() {
        // Arrange
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        ProductName newName = ProductName.from("Paracetamol 500mg Comprimido");
        Description newDescription = Description.from("Analgésico, antitérmico, comprimido revestido");
        Brand newBrand = Brand.from("Eurofarma");
        
        // Act
        Product updatedProduct = product.update(
            newName,
            newDescription,
            newBrand,
            ProductCategory.GENERIC
        );
        
        // Assert
        assertNotSame(product, updatedProduct);
        assertEquals(newName, updatedProduct.getName());
        assertEquals(newDescription, updatedProduct.getDescription());
        assertEquals(newBrand, updatedProduct.getBrand());
        assertEquals(ProductCategory.GENERIC, updatedProduct.getCategory());
        assertEquals(price, updatedProduct.getPrice());
        assertEquals(2, updatedProduct.getVersion());
        
        // Verifica evento de atualização
        List<DomainEvent> events = updatedProduct.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductUpdatedEvent);
    }
    
    @Test
    void shouldThrowExceptionWhenUpdatingDiscontinuedProduct() {
        // Arrange
        Product product = Product.reconstitute(
            ProductId.generate(),
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            ProductStatus.DISCONTINUED,
            price,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1
        );
        
        // Act & Assert
        assertThrows(ProductDomainException.class, () -> {
            product.update(
                productName,
                description,
                brand,
                ProductCategory.MEDICINE
            );
        });
    }
    
    @Test
    void shouldMarkProductAsDiscontinued() {
        // Arrange
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        String reason = "Produto descontinuado pelo fabricante";
        
        // Act
        Product discontinuedProduct = product.markAsDiscontinued(reason);
        
        // Assert
        assertEquals(ProductStatus.DISCONTINUED, discontinuedProduct.getStatus());
        assertEquals(2, discontinuedProduct.getVersion());
        
        // Verifica evento de descontinuação
        List<DomainEvent> events = discontinuedProduct.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductDiscontinuedEvent);
        
        ProductDiscontinuedEvent event = (ProductDiscontinuedEvent) events.get(0);
        assertEquals(reason, event.getReason());
    }
    
    @Test
    void shouldChangeProductPrice() {
        // Arrange
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        Money newPrice = Money.of(15.99);
        
        // Act
        Product productWithNewPrice = product.changePrice(newPrice);
        
        // Assert
        assertEquals(newPrice, productWithNewPrice.getPrice());
        assertEquals(2, productWithNewPrice.getVersion());
        
        // Verifica evento de atualização
        List<DomainEvent> events = productWithNewPrice.getDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ProductUpdatedEvent);
    }
    
    @Test
    void shouldThrowExceptionWhenPriceIncreaseExceeds50Percent() {
        // Arrange
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            Money.of(100.00)
        );
        
        Money newPrice = Money.of(151.00); // 51% de aumento
        
        // Act & Assert
        assertThrows(ProductDomainException.class, () -> {
            product.changePrice(newPrice);
        });
    }
    
    @Test
    void shouldActivateProduct() {
        // Arrange
        Product product = Product.reconstitute(
            ProductId.generate(),
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            ProductStatus.INACTIVE,
            price,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1
        );
        
        // Act
        Product activatedProduct = product.activate();
        
        // Assert
        assertEquals(ProductStatus.ACTIVE, activatedProduct.getStatus());
        assertEquals(2, activatedProduct.getVersion());
    }
    
    @Test
    void shouldDeactivateProduct() {
        // Arrange
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        // Act
        Product deactivatedProduct = product.deactivate();
        
        // Assert
        assertEquals(ProductStatus.INACTIVE, deactivatedProduct.getStatus());
        assertEquals(2, deactivatedProduct.getVersion());
    }
    
    @Test
    void shouldValidateProductInvariants() {
        // Act & Assert - Produto sem nome
        assertThrows(ProductDomainException.class, () -> {
            Product product = Product.reconstitute(
                ProductId.generate(),
                null, // Nome nulo
                barcode,
                description,
                brand,
                ProductCategory.MEDICINE,
                ProductStatus.ACTIVE,
                price,
                LocalDateTime.now(),
                LocalDateTime.now(),
                1
            );
        });
        
        // Act & Assert - Produto sem código de barras
        assertThrows(ProductDomainException.class, () -> {
            Product product = Product.reconstitute(
                ProductId.generate(),
                productName,
                null, // Barcode nulo
                description,
                brand,
                ProductCategory.MEDICINE,
                ProductStatus.ACTIVE,
                price,
                LocalDateTime.now(),
                LocalDateTime.now(),
                1
            );
        });
        
        // Act & Assert - Produto sem preço
        assertThrows(ProductDomainException.class, () -> {
            Product product = Product.reconstitute(
                ProductId.generate(),
                productName,
                barcode,
                description,
                brand,
                ProductCategory.MEDICINE,
                ProductStatus.ACTIVE,
                null, // Preço nulo
                LocalDateTime.now(),
                LocalDateTime.now(),
                1
            );
        });
    }
    
    @Test
    void shouldCheckProductBusinessMethods() {
        // Arrange
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        // Act & Assert
        assertTrue(product.isAvailableForSale());
        assertTrue(product.isControlled());
        assertTrue(product.isTaxExempt());
        assertFalse(product.isDiscontinued());
    }
    
    @Test
    void shouldClearDomainEvents() {
        // Arrange
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        // Verifica que há eventos
        assertFalse(product.getDomainEvents().isEmpty());
        
        // Act
        product.clearDomainEvents();
        
        // Assert
        assertTrue(product.getDomainEvents().isEmpty());
    }
    
    @Test
    void shouldBeEqualWhenSameId() {
        // Arrange
        ProductId productId = ProductId.generate();
        
        Product product1 = Product.reconstitute(
            productId,
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            ProductStatus.ACTIVE,
            price,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1
        );
        
        Product product2 = Product.reconstitute(
            productId,
            ProductName.from("Ibuprofeno 400mg"),
            Barcode.from("7891234567896"),
            Description.from("Anti-inflamatório"),
            Brand.from("Eurofarma"),
            ProductCategory.GENERIC,
            ProductStatus.INACTIVE,
            Money.of(18.99),
            LocalDateTime.now(),
            LocalDateTime.now(),
            2
        );
        
        // Act & Assert
        assertEquals(product1, product2);
        assertEquals(product1.hashCode(), product2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentIds() {
        // Arrange
        Product product1 = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        Product product2 = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        // Act & Assert
        assertNotEquals(product1, product2);
        assertNotEquals(product1.hashCode(), product2.hashCode());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Arrange
        Product product = Product.register(
            productName,
            barcode,
            description,
            brand,
            ProductCategory.MEDICINE,
            price
        );
        
        // Act
        String toString = product.toString();
        
        // Assert
        assertTrue(toString.contains("Product"));
        assertTrue(toString.contains(product.getId().getStringValue()));
        assertTrue(toString.contains(productName.getValue()));
        assertTrue(toString.contains("MEDICINE"));
        assertTrue(toString.contains("ACTIVE"));
        assertTrue(toString.contains(price.getFormattedValue()));
    }
}