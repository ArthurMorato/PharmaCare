package com.pharmacare.domain.core.products.events;

import com.pharmacare.domain.core.products.aggregates.Product;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Evento de domínio para atualização de produto.
 */
public class ProductUpdatedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final LocalDateTime occurredOn;
    private final int version;
    
    // Dados do evento
    private final String productId;
    private final Map<String, Object> changes;
    private final LocalDateTime updatedAt;
    
    /**
     * Construtor privado para usar o Builder.
     */
    private ProductUpdatedEvent(Builder builder) {
        this.eventId = builder.eventId;
        this.aggregateId = builder.aggregateId;
        this.aggregateType = builder.aggregateType;
        this.occurredOn = builder.occurredOn;
        this.version = builder.version;
        this.productId = builder.productId;
        this.changes = builder.changes;
        this.updatedAt = builder.updatedAt;
    }
    
    /**
     * Factory method para criar evento a partir do Aggregate.
     */
    public static ProductUpdatedEvent from(Product product, Map<String, Object> changes) {
        return new Builder()
            .eventId(UUID.randomUUID())
            .aggregateId(product.getId().getStringValue())
            .aggregateType("Product")
            .occurredOn(LocalDateTime.now())
            .version(1)
            .productId(product.getId().getStringValue())
            .changes(changes)
            .updatedAt(product.getUpdatedAt())
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
        return "ProductUpdated";
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
    
    public Map<String, Object> getChanges() {
        return changes;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Builder para ProductUpdatedEvent.
     */
    public static class Builder {
        private UUID eventId;
        private String aggregateId;
        private String aggregateType;
        private LocalDateTime occurredOn;
        private int version;
        private String productId;
        private Map<String, Object> changes;
        private LocalDateTime updatedAt;
        
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
        
        public Builder changes(Map<String, Object> changes) {
            this.changes = changes;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public ProductUpdatedEvent build() {
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
            if (changes == null || changes.isEmpty()) {
                throw new IllegalStateException("changes não pode ser nulo ou vazio");
            }
            
            return new ProductUpdatedEvent(this);
        }
    }
}