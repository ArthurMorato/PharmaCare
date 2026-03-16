package com.pharmacare.domain.core.products.events;

import com.pharmacare.domain.core.common.DomainEvent;
import com.pharmacare.domain.core.products.aggregates.Product;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de domínio para descontinuação de produto.
 */
public class ProductDiscontinuedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final LocalDateTime occurredOn;
    private final int version;
    
    // Dados do evento
    private final String productId;
    private final String reason;
    private final LocalDateTime discontinuedAt;
    
    /**
     * Construtor privado para usar o Builder.
     */
    private ProductDiscontinuedEvent(Builder builder) {
        this.eventId = builder.eventId;
        this.aggregateId = builder.aggregateId;
        this.aggregateType = builder.aggregateType;
        this.occurredOn = builder.occurredOn;
        this.version = builder.version;
        this.productId = builder.productId;
        this.reason = builder.reason;
        this.discontinuedAt = builder.discontinuedAt;
    }
    
    /**
     * Factory method para criar evento a partir do Aggregate.
     */
    public static ProductDiscontinuedEvent from(Product product, String reason) {
        return new Builder()
            .eventId(UUID.randomUUID())
            .aggregateId(product.getId().getStringValue())
            .aggregateType("Product")
            .occurredOn(LocalDateTime.now())
            .version(1)
            .productId(product.getId().getStringValue())
            .reason(reason)
            .discontinuedAt(product.getUpdatedAt())
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
        return "ProductDiscontinued";
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
    
    public String getReason() {
        return reason;
    }
    
    public LocalDateTime getDiscontinuedAt() {
        return discontinuedAt;
    }
    
    /**
     * Builder para ProductDiscontinuedEvent.
     */
    public static class Builder {
        private UUID eventId;
        private String aggregateId;
        private String aggregateType;
        private LocalDateTime occurredOn;
        private int version;
        private String productId;
        private String reason;
        private LocalDateTime discontinuedAt;
        
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
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder discontinuedAt(LocalDateTime discontinuedAt) {
            this.discontinuedAt = discontinuedAt;
            return this;
        }
        
        public ProductDiscontinuedEvent build() {
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
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalStateException("reason não pode ser nulo ou vazio");
            }
            
            return new ProductDiscontinuedEvent(this);
        }
    }
}