package com.pharmacare.domain.core.inventory.aggregates;

import com.pharmacare.domain.core.inventory.exceptions.InventoryDomainException;
import com.pharmacare.domain.core.inventory.valueobjects.*;
import com.pharmacare.domain.core.products.valueobjects.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para InventoryItem.
 */
class InventoryItemTest {
    
    private ProductId productId;
    private String branchId;
    private BatchNumber batchNumber;
    private Quantity initialQuantity;
    private ExpirationDate expirationDate;
    private double unitCost;
    
    @BeforeEach
    void setUp() {
        productId = ProductId.generate();
        branchId = "BRANCH-001";
        batchNumber = BatchNumber.from("L2312345678");
        initialQuantity = Quantity.of(100);
        expirationDate = ExpirationDate.daysFromNow(180);
        unitCost = 12.50;
    }
    
    @Test
    void shouldRegisterNewInventoryItem() {
        // Act
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Assert
        assertNotNull(item);
        assertEquals(productId, item.getProductId());
        assertEquals(branchId, item.getBranchId());
        assertEquals(batchNumber, item.getBatchNumber());
        assertEquals(initialQuantity, item.getQuantity());
        assertEquals(Quantity.zero(), item.getReservedQuantity());
        assertEquals(expirationDate, item.getExpirationDate());
        assertEquals(InventoryItem.InventoryStatus.ACTIVE, item.getStatus());
        assertEquals(unitCost, item.getUnitCost());
        assertEquals(1, item.getVersion());
        
        // Verifica que está ativo
        assertTrue(item.getStatus().isActive());
        assertTrue(item.getStatus().canBeSold());
        
        // Verifica quantidade disponível
        assertEquals(100, item.getAvailableQuantity());
        
        // Verifica eventos
        assertEquals(0, item.getDomainEvents().size());
    }
    
    @Test
    void shouldReconstituteExistingInventoryItem() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        Quantity reservedQuantity = Quantity.of(20);
        
        // Act
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            reservedQuantity,
            expirationDate,
            InventoryItem.InventoryStatus.RESERVED,
            createdAt,
            updatedAt,
            3,
            unitCost
        );
        
        // Assert
        assertNotNull(item);
        assertEquals(createdAt, item.getCreatedAt());
        assertEquals(updatedAt, item.getUpdatedAt());
        assertEquals(3, item.getVersion());
        assertEquals(reservedQuantity, item.getReservedQuantity());
        assertEquals(InventoryItem.InventoryStatus.RESERVED, item.getStatus());
    }
    
    @Test
    void shouldAdjustStockIncrease() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act
        InventoryItem adjustedItem = item.adjustStock(50, "Compra de fornecedor", "USER-001");
        
        // Assert
        assertEquals(150, adjustedItem.getQuantity().getValue());
        assertEquals(2, adjustedItem.getVersion());
        
        // Verifica eventos
        assertEquals(1, adjustedItem.getDomainEvents().size());
    }
    
    @Test
    void shouldAdjustStockDecrease() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act
        InventoryItem adjustedItem = item.adjustStock(-30, "Ajuste de perda", "USER-001");
        
        // Assert
        assertEquals(70, adjustedItem.getQuantity().getValue());
        assertEquals(2, adjustedItem.getVersion());
    }
    
    @Test
    void shouldThrowExceptionWhenAdjustingExpiredItem() {
        // Arrange
        // Cria um item com data expirada
        ExpirationDate expiredDate = ExpirationDate.daysFromNow(-1); // Data passada
        
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            Quantity.zero(),
            expiredDate,
            InventoryItem.InventoryStatus.EXPIRED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1,
            unitCost
        );
        
        // Act & Assert
        assertThrows(InventoryDomainException.class, () -> {
            item.adjustStock(10, "Teste", "USER-001");
        });
    }
    
    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act & Assert
        assertThrows(InventoryDomainException.class, () -> {
            item.adjustStock(-150, "Teste", "USER-001");
        });
    }
    
    @Test
    void shouldThrowExceptionWhenBelowMinimumAfterAdjustment() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            Quantity.of(10), // Exatamente o mínimo + 5
            expirationDate,
            unitCost
        );
        
        // Act & Assert
        assertThrows(InventoryDomainException.class, () -> {
            item.adjustStock(-6, "Teste", "USER-001"); // Ficaria com 4 (< 5)
        });
    }
    
    @Test
    void shouldReserveQuantity() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act
        InventoryItem reservedItem = item.reserve(30);
        
        // Assert
        assertEquals(30, reservedItem.getReservedQuantity().getValue());
        assertEquals(70, reservedItem.getAvailableQuantity());
        assertEquals(InventoryItem.InventoryStatus.RESERVED, reservedItem.getStatus());
        assertEquals(2, reservedItem.getVersion());
    }
    
    @Test
    void shouldThrowExceptionWhenReservingMoreThanAvailable() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act & Assert
        assertThrows(InventoryDomainException.class, () -> {
            item.reserve(150);
        });
    }
    
    @Test
    void shouldReleaseReservation() {
        // Arrange
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            Quantity.of(30),
            expirationDate,
            InventoryItem.InventoryStatus.RESERVED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1,
            unitCost
        );
        
        // Act
        InventoryItem releasedItem = item.releaseReservation(20);
        
        // Assert
        assertEquals(10, releasedItem.getReservedQuantity().getValue());
        assertEquals(InventoryItem.InventoryStatus.RESERVED, releasedItem.getStatus());
        assertEquals(2, releasedItem.getVersion());
    }
    
    @Test
    void shouldChangeToActiveWhenAllReservationReleased() {
        // Arrange
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            Quantity.of(30),
            expirationDate,
            InventoryItem.InventoryStatus.RESERVED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1,
            unitCost
        );
        
        // Act
        InventoryItem releasedItem = item.releaseReservation(30);
        
        // Assert
        assertEquals(0, releasedItem.getReservedQuantity().getValue());
        assertEquals(InventoryItem.InventoryStatus.ACTIVE, releasedItem.getStatus());
    }
    
    @Test
    void shouldConfirmSale() {
        // Arrange
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            Quantity.of(30),
            expirationDate,
            InventoryItem.InventoryStatus.RESERVED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1,
            unitCost
        );
        
        // Act
        InventoryItem soldItem = item.confirmSale(30);
        
        // Assert
        assertEquals(70, soldItem.getQuantity().getValue()); // 100 - 30
        assertEquals(0, soldItem.getReservedQuantity().getValue());
        assertEquals(InventoryItem.InventoryStatus.ACTIVE, soldItem.getStatus());
        assertEquals(2, soldItem.getVersion());
    }
    
    @Test
    void shouldExpireItem() {
        // Arrange
        // Cria item com data próxima da expiração
        ExpirationDate nearExpiredDate = ExpirationDate.daysFromNow(-1);
        
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            Quantity.zero(),
            nearExpiredDate,
            InventoryItem.InventoryStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1,
            unitCost
        );
        
        // Act
        InventoryItem expiredItem = item.expire();
        
        // Assert
        assertEquals(InventoryItem.InventoryStatus.EXPIRED, expiredItem.getStatus());
        assertEquals(2, expiredItem.getVersion());
    }
    
    @Test
    void shouldThrowExceptionWhenExpiringNonExpiredItem() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act & Assert
        assertThrows(InventoryDomainException.class, () -> {
            item.expire();
        });
    }
    
    @Test
    void shouldTransferToOtherBranch() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act
        InventoryItem transferredItem = item.transferTo("BRANCH-002", 30);
        
        // Assert
        assertEquals(70, transferredItem.getQuantity().getValue());
        assertEquals("BRANCH-001", transferredItem.getBranchId()); // Mantém branch original
        assertEquals(2, transferredItem.getVersion());
    }
    
    @Test
    void shouldThrowExceptionWhenTransferringWithReservedQuantity() {
        // Arrange
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            Quantity.of(20),
            expirationDate,
            InventoryItem.InventoryStatus.RESERVED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1,
            unitCost
        );
        
        // Act & Assert
        assertThrows(InventoryDomainException.class, () -> {
            item.transferTo("BRANCH-002", 30);
        });
    }
    
    @Test
    void shouldCheckAvailableQuantity() {
        // Arrange
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            Quantity.of(100),
            Quantity.of(30),
            expirationDate,
            InventoryItem.InventoryStatus.RESERVED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1,
            unitCost
        );
        
        // Act & Assert
        assertEquals(70, item.getAvailableQuantity());
    }
    
    @Test
    void shouldCheckSufficientStock() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act & Assert
        assertTrue(item.hasSufficientStock(50));
        assertFalse(item.hasSufficientStock(150));
    }
    
    @Test
    void shouldCheckNearExpiration() {
        // Arrange
        ExpirationDate nearDate = ExpirationDate.daysFromNow(15);
        InventoryItem nearItem = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            nearDate,
            unitCost
        );
        
        ExpirationDate farDate = ExpirationDate.daysFromNow(60);
        InventoryItem farItem = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            farDate,
            unitCost
        );
        
        // Act & Assert
        assertTrue(nearItem.isNearExpiration());
        assertFalse(farItem.isNearExpiration());
    }
    
    @Test
    void shouldCheckBelowMinimumStock() {
        // Arrange
        InventoryItem lowStockItem = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            Quantity.of(3), // Abaixo do mínimo
            expirationDate,
            unitCost
        );
        
        InventoryItem adequateStockItem = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            Quantity.of(20), // Acima do mínimo
            expirationDate,
            unitCost
        );
        
        // Act & Assert
        assertTrue(lowStockItem.isBelowMinimumStock());
        assertFalse(adequateStockItem.isBelowMinimumStock());
    }
    
    @Test
    void shouldCalculateInventoryValue() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            Quantity.of(100),
            expirationDate,
            12.50 // unit cost
        );
        
        // Act
        double value = item.getInventoryValue();
        
        // Assert
        assertEquals(1250.0, value); // 100 * 12.50
    }
    
    @Test
    void shouldCalculateAvailableInventoryValue() {
        // Arrange
        InventoryItem item = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            Quantity.of(100),
            Quantity.of(30),
            expirationDate,
            InventoryItem.InventoryStatus.RESERVED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            1,
            12.50
        );
        
        // Act
        double availableValue = item.getAvailableInventoryValue();
        
        // Assert
        assertEquals(875.0, availableValue); // 70 * 12.50
    }
    
    @Test
    void shouldClearDomainEvents() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        InventoryItem adjustedItem = item.adjustStock(10, "Teste", "USER-001");
        
        // Verifica que há eventos
        assertEquals(1, adjustedItem.getDomainEvents().size());
        
        // Act
        adjustedItem.clearDomainEvents();
        
        // Assert
        assertEquals(0, adjustedItem.getDomainEvents().size());
    }
    
    @Test
    void shouldGenerateCompositeId() {
        // Arrange
        InventoryItem item = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act
        String id = item.getId();
        
        // Assert
        String expectedId = String.format("%s|%s|%s", 
            productId.getStringValue(), branchId, batchNumber.getValue());
        assertEquals(expectedId, id);
    }
    
    @Test
    void shouldBeEqualWhenSameCompositeId() {
        // Arrange
        InventoryItem item1 = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        InventoryItem item2 = InventoryItem.reconstitute(
            productId,
            branchId,
            batchNumber,
            Quantity.of(200),
            Quantity.of(50),
            ExpirationDate.daysFromNow(365),
            InventoryItem.InventoryStatus.RESERVED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            5,
            15.75
        );
        
        // Act & Assert
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentCompositeId() {
        // Arrange
        InventoryItem item1 = InventoryItem.register(
            productId,
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        InventoryItem item2 = InventoryItem.register(
            ProductId.generate(), // Diferente productId
            branchId,
            batchNumber,
            initialQuantity,
            expirationDate,
            unitCost
        );
        
        // Act & Assert
        assertNotEquals(item1, item2);
        assertNotEquals(item1.hashCode(), item2.hashCode());
    }
}