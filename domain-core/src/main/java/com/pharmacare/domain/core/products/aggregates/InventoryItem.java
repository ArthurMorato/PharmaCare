package com.pharmacare.domain.core.inventory.aggregates;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.inventory.events.StockLevelChangedEvent;
import com.pharmacare.domain.core.inventory.exceptions.InventoryDomainException;
import com.pharmacare.domain.core.inventory.valueobjects.*;
import com.pharmacare.domain.core.products.valueobjects.ProductId;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate Root para gestão de estoque.
 * Identidade composta: ProductId + BranchId + BatchNumber
 * 
 * Responsável por:
 * - Gerenciar quantidade em estoque
 * - Controlar reservas
 * - Validar datas de expiração
 * - Gerenciar transferências entre filiais
 */
public class InventoryItem extends ValueObject {
    
    // Identificação composta
    private final ProductId productId;
    private final String branchId;
    private final BatchNumber batchNumber;
    
    // Informações do estoque
    private Quantity quantity;
    private Quantity reservedQuantity;
    private final ExpirationDate expirationDate;
    
    // Status
    private InventoryStatus status;
    
    // Metadados
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final int version;
    
    // Informações de custo
    private final double unitCost;
    
    // Eventos de domínio
    private final List<com.pharmacare.domain.core.products.events.DomainEvent> domainEvents;
    
    /**
     * Enum para status do item de inventário.
     */
    public enum InventoryStatus {
        ACTIVE("Ativo"),
        RESERVED("Reservado"),
        EXPIRED("Expirado"),
        QUARANTINE("Quarentena"),
        DAMAGED("Danificado"),
        SOLD("Vendido");
        
        private final String displayName;
        
        InventoryStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean isActive() {
            return this == ACTIVE || this == RESERVED;
        }
        
        public boolean canBeSold() {
            return this == ACTIVE || this == RESERVED;
        }
        
        public boolean canBeTransferred() {
            return this == ACTIVE;
        }
    }
    
    /**
     * Construtor privado para uso do Builder.
     */
    private InventoryItem(Builder builder) {
        this.productId = builder.productId;
        this.branchId = builder.branchId;
        this.batchNumber = builder.batchNumber;
        this.quantity = builder.quantity;
        this.reservedQuantity = builder.reservedQuantity;
        this.expirationDate = builder.expirationDate;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.version = builder.version;
        this.unitCost = builder.unitCost;
        this.domainEvents = new ArrayList<>();
        
        // Valida invariantes
        validateInvariants();
        
        // Valida estado inicial
        if (builder.isNew) {
            validateInitialState();
        }
    }
    
    /**
     * Factory method para criar novo item de inventário.
     */
    public static InventoryItem register(
            ProductId productId,
            String branchId,
            BatchNumber batchNumber,
            Quantity initialQuantity,
            ExpirationDate expirationDate,
            double unitCost) {
        
        return new Builder()
            .productId(productId)
            .branchId(branchId)
            .batchNumber(batchNumber)
            .quantity(initialQuantity)
            .reservedQuantity(Quantity.zero())
            .expirationDate(expirationDate)
            .status(InventoryStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .version(1)
            .unitCost(unitCost)
            .isNew(true)
            .build();
    }
    
    /**
     * Factory method para reconstituir item existente.
     */
    public static InventoryItem reconstitute(
            ProductId productId,
            String branchId,
            BatchNumber batchNumber,
            Quantity quantity,
            Quantity reservedQuantity,
            ExpirationDate expirationDate,
            InventoryStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int version,
            double unitCost) {
        
        return new Builder()
            .productId(productId)
            .branchId(branchId)
            .batchNumber(batchNumber)
            .quantity(quantity)
            .reservedQuantity(reservedQuantity)
            .expirationDate(expirationDate)
            .status(status)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .version(version)
            .unitCost(unitCost)
            .isNew(false)
            .build();
    }
    
    /**
     * Ajusta o estoque (adiciona ou remove).
     */
    public InventoryItem adjustStock(int adjustment, String reason, String changedBy) {
        validateCanAdjustStock();
        
        int oldQuantity = this.quantity.getValue();
        Quantity newQuantity;
        
        if (adjustment > 0) {
            // Adição ao estoque
            newQuantity = this.quantity.add(adjustment);
        } else if (adjustment < 0) {
            // Remoção do estoque
            validateSufficientStock(Math.abs(adjustment));
            newQuantity = this.quantity.subtract(Math.abs(adjustment));
        } else {
            // Nenhuma mudança
            return this;
        }
        
        // Verifica se está abaixo do mínimo após ajuste
        validateMinimumStock(newQuantity);
        
        InventoryItem adjustedItem = new Builder()
            .productId(this.productId)
            .branchId(this.branchId)
            .batchNumber(this.batchNumber)
            .quantity(newQuantity)
            .reservedQuantity(this.reservedQuantity)
            .expirationDate(this.expirationDate)
            .status(this.status)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .unitCost(this.unitCost)
            .isNew(false)
            .build();
        
        // Adiciona evento de mudança de estoque
        adjustedItem.registerDomainEvent(
            StockLevelChangedEvent.from(adjustedItem, oldQuantity, adjustment, reason, changedBy)
        );
        
        return adjustedItem;
    }
    
    /**
     * Reserva quantidade para venda.
     */
    public InventoryItem reserve(int quantityToReserve) {
        validateCanReserve(quantityToReserve);
        
        Quantity newReservedQuantity = this.reservedQuantity.add(quantityToReserve);
        
        return new Builder()
            .productId(this.productId)
            .branchId(this.branchId)
            .batchNumber(this.batchNumber)
            .quantity(this.quantity)
            .reservedQuantity(newReservedQuantity)
            .expirationDate(this.expirationDate)
            .status(InventoryStatus.RESERVED)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .unitCost(this.unitCost)
            .isNew(false)
            .build();
    }
    
    /**
     * Libera quantidade reservada.
     */
    public InventoryItem releaseReservation(int quantityToRelease) {
        if (quantityToRelease <= 0) {
            throw new IllegalArgumentException("Quantidade a liberar deve ser positiva");
        }
        
        if (quantityToRelease > this.reservedQuantity.getValue()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.RESERVATION_EXCEEDS_STOCK,
                "Quantidade a liberar excede a quantidade reservada",
                "quantityToRelease"
            );
        }
        
        Quantity newReservedQuantity = this.reservedQuantity.subtract(quantityToRelease);
        
        InventoryStatus newStatus = newReservedQuantity.isZero() ? 
            InventoryStatus.ACTIVE : InventoryStatus.RESERVED;
        
        return new Builder()
            .productId(this.productId)
            .branchId(this.branchId)
            .batchNumber(this.batchNumber)
            .quantity(this.quantity)
            .reservedQuantity(newReservedQuantity)
            .expirationDate(this.expirationDate)
            .status(newStatus)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .unitCost(this.unitCost)
            .isNew(false)
            .build();
    }
    
    /**
     * Confirma venda (remove quantidade reservada).
     */
    public InventoryItem confirmSale(int quantitySold) {
        if (quantitySold <= 0) {
            throw new IllegalArgumentException("Quantidade vendida deve ser positiva");
        }
        
        validateSufficientReservedStock(quantitySold);
        
        // Remove da quantidade reservada e da quantidade total
        Quantity newReservedQuantity = this.reservedQuantity.subtract(quantitySold);
        Quantity newQuantity = this.quantity.subtract(quantitySold);
        
        InventoryStatus newStatus = newReservedQuantity.isZero() ? 
            InventoryStatus.ACTIVE : InventoryStatus.RESERVED;
        
        InventoryItem soldItem = new Builder()
            .productId(this.productId)
            .branchId(this.branchId)
            .batchNumber(this.batchNumber)
            .quantity(newQuantity)
            .reservedQuantity(newReservedQuantity)
            .expirationDate(this.expirationDate)
            .status(newStatus)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .unitCost(this.unitCost)
            .isNew(false)
            .build();
        
        return soldItem;
    }
    
    /**
     * Marca como expirado.
     */
    public InventoryItem expire() {
        if (this.status == InventoryStatus.EXPIRED) {
            return this; // Já está expirado
        }
        
        validateCanExpire();
        
        return new Builder()
            .productId(this.productId)
            .branchId(this.branchId)
            .batchNumber(this.batchNumber)
            .quantity(this.quantity)
            .reservedQuantity(this.reservedQuantity)
            .expirationDate(this.expirationDate)
            .status(InventoryStatus.EXPIRED)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .unitCost(this.unitCost)
            .isNew(false)
            .build();
    }
    
    /**
     * Transfere quantidade para outra filial.
     */
    public InventoryItem transferTo(String targetBranchId, int quantityToTransfer) {
        validateCanTransfer(quantityToTransfer);
        
        // Remove do estoque atual
        Quantity newQuantity = this.quantity.subtract(quantityToTransfer);
        
        return new Builder()
            .productId(this.productId)
            .branchId(this.branchId) // Mantém na filial origem
            .batchNumber(this.batchNumber)
            .quantity(newQuantity)
            .reservedQuantity(this.reservedQuantity)
            .expirationDate(this.expirationDate)
            .status(this.status)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .unitCost(this.unitCost)
            .isNew(false)
            .build();
    }
    
    /**
     * Retorna a quantidade disponível (total - reservada).
     */
    public int getAvailableQuantity() {
        return this.quantity.getValue() - this.reservedQuantity.getValue();
    }
    
    /**
     * Verifica se há estoque suficiente.
     */
    public boolean hasSufficientStock(int requiredQuantity) {
        return getAvailableQuantity() >= requiredQuantity;
    }
    
    /**
     * Verifica se está próximo da expiração.
     */
    public boolean isNearExpiration() {
        return this.expirationDate.isNearExpiration();
    }
    
    /**
     * Verifica se está abaixo do estoque mínimo.
     */
    public boolean isBelowMinimumStock() {
        return this.quantity.isBelowMinimum();
    }
    
    /**
     * Calcula o valor total do estoque.
     */
    public double getInventoryValue() {
        return this.quantity.getValue() * this.unitCost;
    }
    
    /**
     * Calcula o valor disponível para venda.
     */
    public double getAvailableInventoryValue() {
        return getAvailableQuantity() * this.unitCost;
    }
    
    /**
     * Valida invariantes do agregado.
     */
    private void validateInvariants() {
        // Validação: productId é obrigatório
        if (productId == null) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND,
                "ProductId é obrigatório",
                "productId"
            );
        }
        
        // Validação: branchId é obrigatório
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND,
                "BranchId é obrigatório",
                "branchId"
            );
        }
        
        // Validação: batchNumber é obrigatório
        if (batchNumber == null) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND,
                "BatchNumber é obrigatório",
                "batchNumber"
            );
        }
        
        // Validação: quantidade não pode ser negativa
        if (quantity == null || quantity.getValue() < 0) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVALID_QUANTITY,
                "Quantidade não pode ser negativa",
                "quantity"
            );
        }
        
        // Validação: quantidade reservada não pode ser negativa
        if (reservedQuantity == null || reservedQuantity.getValue() < 0) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVALID_QUANTITY,
                "Quantidade reservada não pode ser negativa",
                "reservedQuantity"
            );
        }
        
        // Validação: quantidade reservada não pode exceder quantidade total
        if (reservedQuantity.getValue() > quantity.getValue()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.RESERVATION_EXCEEDS_STOCK,
                "Quantidade reservada não pode exceder quantidade total",
                "reservedQuantity"
            );
        }
        
        // Validação: expirationDate é obrigatória
        if (expirationDate == null) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND,
                "Data de validade é obrigatória",
                "expirationDate"
            );
        }
        
        // Validação: unitCost não pode ser negativo
        if (unitCost < 0) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND,
                "Custo unitário não pode ser negativo",
                "unitCost"
            );
        }
    }
    
    /**
     * Valida estado inicial do item.
     */
    private void validateInitialState() {
        // Quantidade inicial deve ser pelo menos o mínimo
        validateMinimumStock(this.quantity);
        
        // Item não pode ser criado já expirado
        if (this.expirationDate.isExpired()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.EXPIRED_ITEM,
                "Item não pode ser criado com data de validade expirada",
                "expirationDate"
            );
        }
    }
    
    /**
     * Valida se pode ajustar estoque.
     */
    private void validateCanAdjustStock() {
        // Não pode ajustar estoque expirado
        if (this.expirationDate.isExpired()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.EXPIRED_ITEM,
                "Não é possível ajustar estoque de item expirado",
                "status"
            );
        }
        
        // Não pode ajustar estoque com status inválido
        if (!this.status.canBeSold()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND,
                "Não é possível ajustar estoque com status: " + this.status,
                "status"
            );
        }
    }
    
    /**
     * Valida estoque mínimo.
     */
    private void validateMinimumStock(Quantity quantity) {
        if (quantity.isBelowMinimum()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.BELOW_MINIMUM_STOCK,
                "Quantidade não pode ser menor que 5 unidades",
                "quantity"
            );
        }
    }
    
    /**
     * Valida estoque suficiente.
     */
    private void validateSufficientStock(int requiredQuantity) {
        if (!hasSufficientStock(requiredQuantity)) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INSUFFICIENT_STOCK,
                String.format("Estoque insuficiente. Disponível: %d, Requerido: %d", 
                    getAvailableQuantity(), requiredQuantity),
                "quantity"
            );
        }
    }
    
    /**
     * Valida estoque reservado suficiente.
     */
    private void validateSufficientReservedStock(int requiredQuantity) {
        if (this.reservedQuantity.getValue() < requiredQuantity) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.RESERVATION_EXCEEDS_STOCK,
                String.format("Quantidade reservada insuficiente. Reservada: %d, Requerida: %d", 
                    this.reservedQuantity.getValue(), requiredQuantity),
                "reservedQuantity"
            );
        }
    }
    
    /**
     * Valida se pode reservar.
     */
    private void validateCanReserve(int quantityToReserve) {
        validateCanAdjustStock();
        
        // Valida quantidade positiva
        if (quantityToReserve <= 0) {
            throw new IllegalArgumentException("Quantidade a reservar deve ser positiva");
        }
        
        // Valida estoque disponível
        validateSufficientStock(quantityToReserve);
    }
    
    /**
     * Valida se pode expirar.
     */
    private void validateCanExpire() {
        // Só pode expirar se estiver ativo ou reservado
        if (!this.status.isActive()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND,
                "Não é possível expirar item com status: " + this.status,
                "status"
            );
        }
        
        // Deve estar realmente expirado
        if (!this.expirationDate.isExpired()) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.EXPIRED_ITEM,
                "Item ainda não está expirado",
                "expirationDate"
            );
        }
    }
    
    /**
     * Valida se pode transferir.
     */
    private void validateCanTransfer(int quantityToTransfer) {
        validateCanAdjustStock();
        
        // Valida quantidade positiva
        if (quantityToTransfer <= 0) {
            throw new IllegalArgumentException("Quantidade a transferir deve ser positiva");
        }
        
        // Valida estoque disponível
        validateSufficientStock(quantityToTransfer);
        
        // Não pode transferir se estiver reservado
        if (this.reservedQuantity.getValue() > 0) {
            throw new InventoryDomainException(
                InventoryDomainException.InventoryErrorCode.TRANSFER_NOT_ALLOWED,
                "Não é possível transferir item com quantidade reservada",
                "reservedQuantity"
            );
        }
    }
    
    /**
     * Registra evento de domínio.
     */
    private void registerDomainEvent(com.pharmacare.domain.core.products.events.DomainEvent event) {
        if (domainEvents != null) {
            domainEvents.add(event);
        }
    }
    
    /**
     * Limpa eventos de domínio.
     */
    public void clearDomainEvents() {
        if (domainEvents != null) {
            domainEvents.clear();
        }
    }
    
    // Getters
    public ProductId getProductId() {
        return productId;
    }
    
    public String getBranchId() {
        return branchId;
    }
    
    public BatchNumber getBatchNumber() {
        return batchNumber;
    }
    
    public Quantity getQuantity() {
        return quantity;
    }
    
    public Quantity getReservedQuantity() {
        return reservedQuantity;
    }
    
    public ExpirationDate getExpirationDate() {
        return expirationDate;
    }
    
    public InventoryStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public int getVersion() {
        return version;
    }
    
    public double getUnitCost() {
        return unitCost;
    }
    
    public List<com.pharmacare.domain.core.products.events.DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * Retorna identificador composto como string.
     */
    public String getId() {
        return String.format("%s|%s|%s", 
            productId.getStringValue(), 
            branchId, 
            batchNumber.getValue());
    }
    
    /**
     * Retorna os componentes para igualdade.
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{productId, branchId, batchNumber};
    }
    
    /**
     * Builder para InventoryItem.
     */
    public static class Builder {
        private ProductId productId;
        private String branchId;
        private BatchNumber batchNumber;
        private Quantity quantity;
        private Quantity reservedQuantity;
        private ExpirationDate expirationDate;
        private InventoryStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int version;
        private double unitCost;
        private boolean isNew;
        
        public Builder productId(ProductId productId) {
            this.productId = productId;
            return this;
        }
        
        public Builder branchId(String branchId) {
            this.branchId = branchId;
            return this;
        }
        
        public Builder batchNumber(BatchNumber batchNumber) {
            this.batchNumber = batchNumber;
            return this;
        }
        
        public Builder quantity(Quantity quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public Builder reservedQuantity(Quantity reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
            return this;
        }
        
        public Builder expirationDate(ExpirationDate expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }
        
        public Builder status(InventoryStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder version(int version) {
            this.version = version;
            return this;
        }
        
        public Builder unitCost(double unitCost) {
            this.unitCost = unitCost;
            return this;
        }
        
        public Builder isNew(boolean isNew) {
            this.isNew = isNew;
            return this;
        }
        
        public InventoryItem build() {
            return new InventoryItem(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "InventoryItem{id=%s, productId=%s, branchId=%s, batchNumber=%s, quantity=%d, reserved=%d, status=%s}",
            getId(),
            productId.getStringValue(),
            branchId,
            batchNumber.getValue(),
            quantity.getValue(),
            reservedQuantity.getValue(),
            status
        );
    }
}