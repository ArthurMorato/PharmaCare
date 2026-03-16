package com.pharmacare.domain.core.inventory.events;

import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.inventory.enums.ProductCategory;
import com.pharmacare.domain.core.products.enums.ProductStatus;
import com.pharmacare.domain.core.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de domínio para registro de novo produto.
 */
public class ProductRegisteredEvent implements DomainEvent {
    
    private final UUID eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final LocalDateTime occurredOn;
    private final int version;
    
    // Dados do evento
    private final String productId;
    private final String productName;
    private final String barcode;
    private final ProductCategory category;
    private final ProductStatus status;
    private final String price;
    private final LocalDateTime createdAt;
    
    /**
     * Construtor privado para usar o Builder.
     */
    private ProductRegisteredEvent(Builder builder) {
        this.eventId = builder.eventId;
        this.aggregateId = builder.aggregateId;
        this.aggregateType = builder.aggregateType;
        this.occurredOn = builder.occurredOn;
        this.version = builder.version;
        this.productId = builder.productId;
        this.productName = builder.productName;
        this.barcode = builder.barcode;
        this.category = builder.category;
        this.status = builder.status;
        this.price = builder.price;
        this.createdAt = builder.createdAt;
    }
    
    /**
     * Factory method para criar evento a partir do Aggregate.
     */
    public static ProductRegisteredEvent from(Product product) {
        return new Builder()
            .eventId(UUID.randomUUID())
            .aggregateId(product.getId().getStringValue())
            .aggregateType("Product")
            .occurredOn(LocalDateTime.now())
            .version(1)
            .productId(product.getId().getStringValue())
            .productName(product.getName().getValue())
            .barcode(product.getBarcode().getValue())
            .category(product.getCategory())
            .status(product.getStatus())
            .price(product.getPrice().getFormattedValue())
            .createdAt(product.getCreatedAt())
            .build();
    }
    
    @Override
    public UUID getEventId() {
        return eventId;
    }
    
    @Override
    public String getAggregateId() {
        return aggregateId;
    }
    
    @Override
    public String getAggregateType() {
        return aggregateType;
    }
    
    @Override
    public String getEventType() {
        return "ProductRegistered";
    }
    
    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    @Override
    public int getVersion() {
        return version;
    }
    
    @Override
    public Object getEventData() {
        return this;
    }
    
    // Getters específicos do evento
    public String getProductId() {
        return productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public ProductCategory getCategory() {
        return category;
    }
    
    public ProductStatus getStatus() {
        return status;
    }
    
    public String getPrice() {
        return price;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Builder para ProductRegisteredEvent.
     */
    public static class Builder {
        private UUID eventId;
        private String aggregateId;
        private String aggregateType;
        private LocalDateTime occurredOn;
        private int version;
        private String productId;
        private String productName;
        private String barcode;
        private ProductCategory category;
        private ProductStatus status;
        private String price;
        private LocalDateTime createdAt;
        
        public Builder eventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }
        
        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }
        
        public Builder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }
        
        public Builder occurredOn(LocalDateTime occurredOn) {
            this.occurredOn = occurredOn;
            return this;
        }
        
        public Builder version(int version) {
            this.version = version;
            return this;
        }
        
        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }
        
        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }
        
        public Builder barcode(String barcode) {
            this.barcode = barcode;
            return this;
        }
        
        public Builder category(ProductCategory category) {
            this.category = category;
            return this;
        }
        
        public Builder status(ProductStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder price(String price) {
            this.price = price;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public ProductRegisteredEvent build() {
            // Validações
            if (eventId == null) {
                throw new IllegalStateException("eventId não pode ser nulo");
            }
            if (aggregateId == null) {
                throw new IllegalStateException("aggregateId não pode ser nulo");
            }
            if (occurredOn == null) {
                throw new IllegalStateException("occurredOn não pode ser nulo");
            }
            
            return new ProductRegisteredEvent(this);
        }
    }
}