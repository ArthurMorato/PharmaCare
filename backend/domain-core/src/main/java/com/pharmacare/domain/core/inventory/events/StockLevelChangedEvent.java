package com.pharmacare.domain.core.inventory.events;

import com.pharmacare.domain.core.common.DomainEvent;
import com.pharmacare.domain.core.inventory.aggregates.InventoryItem;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de domínio para mudança no nível de estoque.
 */
public class StockLevelChangedEvent implements DomainEvent {
    
    private final UUID eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final LocalDateTime occurredOn;
    private final int version;
    
    // Dados específicos do evento
    private final String inventoryItemId;
    private final String productId;
    private final String branchId;
    private final String batchNumber;
    private final int oldQuantity;
    private final int newQuantity;
    private final int adjustment;
    private final String reason;
    private final String changedBy;
    
    /**
     * Construtor privado para usar o Builder.
     */
    private StockLevelChangedEvent(Builder builder) {
        this.eventId = builder.eventId;
        this.aggregateId = builder.aggregateId;
        this.aggregateType = builder.aggregateType;
        this.occurredOn = builder.occurredOn;
        this.version = builder.version;
        this.inventoryItemId = builder.inventoryItemId;
        this.productId = builder.productId;
        this.branchId = builder.branchId;
        this.batchNumber = builder.batchNumber;
        this.oldQuantity = builder.oldQuantity;
        this.newQuantity = builder.newQuantity;
        this.adjustment = builder.adjustment;
        this.reason = builder.reason;
        this.changedBy = builder.changedBy;
    }
    
    /**
     * Factory method para criar evento a partir do InventoryItem.
     */
    public static StockLevelChangedEvent from(
            InventoryItem item, 
            int oldQuantity, 
            int adjustment, 
            String reason, 
            String changedBy) {
        
        return new Builder()
            .eventId(UUID.randomUUID())
            .aggregateId(item.getId().toString())
            .aggregateType("InventoryItem")
            .occurredOn(LocalDateTime.now())
            .version(1)
            .inventoryItemId(item.getId().toString())
            .productId(item.getProductId().getStringValue())
            .branchId(item.getBranchId())
            .batchNumber(item.getBatchNumber().getValue())
            .oldQuantity(oldQuantity)
            .newQuantity(item.getQuantity().getValue())
            .adjustment(adjustment)
            .reason(reason)
            .changedBy(changedBy)
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
        return "StockLevelChanged";
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
    
    // Getters específicos
    public String getInventoryItemId() {
        return inventoryItemId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public String getBranchId() {
        return branchId;
    }
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public int getOldQuantity() {
        return oldQuantity;
    }
    
    public int getNewQuantity() {
        return newQuantity;
    }
    
    public int getAdjustment() {
        return adjustment;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    /**
     * Builder para StockLevelChangedEvent.
     */
    public static class Builder {
        private UUID eventId;
        private String aggregateId;
        private String aggregateType;
        private LocalDateTime occurredOn;
        private int version;
        private String inventoryItemId;
        private String productId;
        private String branchId;
        private String batchNumber;
        private int oldQuantity;
        private int newQuantity;
        private int adjustment;
        private String reason;
        private String changedBy;
        
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
        
        public Builder inventoryItemId(String inventoryItemId) {
            this.inventoryItemId = inventoryItemId;
            return this;
        }
        
        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }
        
        public Builder branchId(String branchId) {
            this.branchId = branchId;
            return this;
        }
        
        public Builder batchNumber(String batchNumber) {
            this.batchNumber = batchNumber;
            return this;
        }
        
        public Builder oldQuantity(int oldQuantity) {
            this.oldQuantity = oldQuantity;
            return this;
        }
        
        public Builder newQuantity(int newQuantity) {
            this.newQuantity = newQuantity;
            return this;
        }
        
        public Builder adjustment(int adjustment) {
            this.adjustment = adjustment;
            return this;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder changedBy(String changedBy) {
            this.changedBy = changedBy;
            return this;
        }
        
        public StockLevelChangedEvent build() {
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
            
            return new StockLevelChangedEvent(this);
        }
    }
}