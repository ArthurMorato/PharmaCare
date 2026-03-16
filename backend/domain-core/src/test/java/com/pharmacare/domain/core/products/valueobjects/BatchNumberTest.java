package com.pharmacare.domain.core.inventory.valueobjects;

import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para BatchNumber.
 */
class BatchNumberTest {
    
    @Test
    void shouldCreateValidBatchNumber() {
        // Arrange & Act
        BatchNumber batchNumber = BatchNumber.from("L1234567890");
        
        // Assert
        assertNotNull(batchNumber);
        assertEquals("L1234567890", batchNumber.getValue());
    }
    
    @Test
    void shouldGenerateBatchNumber() {
        // Arrange & Act
        BatchNumber batchNumber = BatchNumber.generate();
        
        // Assert
        assertNotNull(batchNumber);
        assertTrue(batchNumber.getValue().startsWith("L"));
        assertEquals(11, batchNumber.getValue().length());
    }
    
    @Test
    void shouldThrowExceptionWhenNullBatchNumber() {
        // Arrange & Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            BatchNumber.from(null);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenEmptyBatchNumber() {
        // Arrange & Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            BatchNumber.from("");
        });
    }
    
    @Test
    void shouldThrowExceptionWhenInvalidFormat() {
        // Arrange & Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            BatchNumber.from("1234567890"); // Sem L
        });
        
        assertThrows(InvalidValueObjectException.class, () -> {
            BatchNumber.from("L123456789"); // 9 dígitos em vez de 10
        });
        
        assertThrows(InvalidValueObjectException.class, () -> {
            BatchNumber.from("L12345678901"); // 11 dígitos em vez de 10
        });
    }
    
    @Test
    void shouldThrowExceptionWhenStartsWithZero() {
        // Arrange & Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            BatchNumber.from("L0123456789"); // Começa com zero
        });
    }
    
    @Test
    void shouldExtractManufacturingYear() {
        // Arrange
        BatchNumber batchNumber = BatchNumber.from("L2312345678");
        
        // Act
        int year = batchNumber.getManufacturingYear();
        
        // Assert
        assertEquals(2023, year); // 23 + 2000
    }
    
    @Test
    void shouldExtractFactoryCode() {
        // Arrange
        BatchNumber batchNumber = BatchNumber.from("L2312345678");
        
        // Act
        String factoryCode = batchNumber.getFactoryCode();
        
        // Assert
        assertEquals("1234", factoryCode);
    }
    
    @Test
    void shouldExtractSequenceNumber() {
        // Arrange
        BatchNumber batchNumber = BatchNumber.from("L2312345678");
        
        // Act
        String sequenceNumber = batchNumber.getSequenceNumber();
        
        // Assert
        assertEquals("5678", sequenceNumber);
    }
    
    @Test
    void shouldFormatBatchNumber() {
        // Arrange
        BatchNumber batchNumber = BatchNumber.from("L1234567890");
        
        // Act
        String formatted = batchNumber.getFormattedValue();
        
        // Assert
        assertEquals("L 1234 5678 90", formatted);
    }
    
    @Test
    void shouldBeEqualWhenSameValue() {
        // Arrange
        BatchNumber bn1 = BatchNumber.from("L1234567890");
        BatchNumber bn2 = BatchNumber.from("L1234567890");
        BatchNumber bn3 = BatchNumber.from("L9876543210");
        
        // Act & Assert
        assertEquals(bn1, bn2);
        assertEquals(bn1.hashCode(), bn2.hashCode());
        assertNotEquals(bn1, bn3);
    }
}