package com.pharmacare.domain.core.inventory.valueobjects;

import com.pharmacare.domain.core.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para Quantity.
 */
class QuantityTest {
    
    @Test
    void shouldCreateValidQuantity() {
        // Arrange & Act
        Quantity quantity = Quantity.of(100);
        
        // Assert
        assertNotNull(quantity);
        assertEquals(100, quantity.getValue());
        assertEquals("units", quantity.getUnit());
    }
    
    @Test
    void shouldCreateQuantityWithCustomUnit() {
        // Arrange & Act
        Quantity quantity = Quantity.of(50, "boxes");
        
        // Assert
        assertEquals(50, quantity.getValue());
        assertEquals("boxes", quantity.getUnit());
    }
    
    @Test
    void shouldCreateZeroQuantity() {
        // Arrange & Act
        Quantity quantity = Quantity.zero();
        
        // Assert
        assertEquals(0, quantity.getValue());
        assertTrue(quantity.isZero());
    }
    
    @Test
    void shouldCreateMinimumQuantity() {
        // Arrange & Act
        Quantity quantity = Quantity.minimum();
        
        // Assert
        assertEquals(5, quantity.getValue());
    }
    
    @Test
    void shouldThrowExceptionWhenNegativeQuantity() {
        // Arrange & Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Quantity.of(-10);
        });
    }
    
    @Test
    void shouldAddQuantities() {
        // Arrange
        Quantity q1 = Quantity.of(50);
        Quantity q2 = Quantity.of(30);
        
        // Act
        Quantity result = q1.add(q2);
        
        // Assert
        assertEquals(80, result.getValue());
    }
    
    @Test
    void shouldAddIntegerToQuantity() {
        // Arrange
        Quantity q1 = Quantity.of(50);
        
        // Act
        Quantity result = q1.add(30);
        
        // Assert
        assertEquals(80, result.getValue());
    }
    
    @Test
    void shouldSubtractQuantities() {
        // Arrange
        Quantity q1 = Quantity.of(50);
        Quantity q2 = Quantity.of(30);
        
        // Act
        Quantity result = q1.subtract(q2);
        
        // Assert
        assertEquals(20, result.getValue());
    }
    
    @Test
    void shouldSubtractIntegerFromQuantity() {
        // Arrange
        Quantity q1 = Quantity.of(50);
        
        // Act
        Quantity result = q1.subtract(30);
        
        // Assert
        assertEquals(20, result.getValue());
    }
    
    @Test
    void shouldThrowExceptionWhenSubtractResultNegative() {
        // Arrange
        Quantity q1 = Quantity.of(10);
        Quantity q2 = Quantity.of(20);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            q1.subtract(q2);
        });
    }
    
    @Test
    void shouldMultiplyQuantity() {
        // Arrange
        Quantity quantity = Quantity.of(10);
        
        // Act
        Quantity result = quantity.multiply(5);
        
        // Assert
        assertEquals(50, result.getValue());
    }
    
    @Test
    void shouldDivideQuantity() {
        // Arrange
        Quantity quantity = Quantity.of(50);
        
        // Act
        Quantity result = quantity.divide(5);
        
        // Assert
        assertEquals(10, result.getValue());
    }
    
    @Test
    void shouldCheckIfZero() {
        // Arrange
        Quantity zero = Quantity.of(0);
        Quantity nonZero = Quantity.of(10);
        
        // Act & Assert
        assertTrue(zero.isZero());
        assertFalse(nonZero.isZero());
    }
    
    @Test
    void shouldCheckIfBelowMinimum() {
        // Arrange
        Quantity belowMin = Quantity.of(3);
        Quantity aboveMin = Quantity.of(10);
        
        // Act & Assert
        assertTrue(belowMin.isBelowMinimum());
        assertFalse(aboveMin.isBelowMinimum());
    }
    
    @Test
    void shouldCheckIfBelowThreshold() {
        // Arrange
        Quantity quantity = Quantity.of(10);
        
        // Act & Assert
        assertTrue(quantity.isBelow(15));
        assertFalse(quantity.isBelow(5));
    }
    
    @Test
    void shouldCheckIfAboveThreshold() {
        // Arrange
        Quantity quantity = Quantity.of(10);
        
        // Act & Assert
        assertTrue(quantity.isAbove(5));
        assertFalse(quantity.isAbove(15));
    }
    
    @Test
    void shouldCompareQuantities() {
        // Arrange
        Quantity q1 = Quantity.of(10);
        Quantity q2 = Quantity.of(20);
        Quantity q3 = Quantity.of(10);
        
        // Act & Assert
        assertTrue(q1.compareTo(q2) < 0);
        assertTrue(q2.compareTo(q1) > 0);
        assertEquals(0, q1.compareTo(q3));
    }
    
    @Test
    void shouldThrowExceptionWhenDifferentUnits() {
        // Arrange
        Quantity q1 = Quantity.of(10, "units");
        Quantity q2 = Quantity.of(20, "boxes");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            q1.add(q2);
        });
    }
    
    @Test
    void shouldBeEqualWhenSameValueAndUnit() {
        // Arrange
        Quantity q1 = Quantity.of(100, "units");
        Quantity q2 = Quantity.of(100, "units");
        Quantity q3 = Quantity.of(100, "boxes");
        
        // Act & Assert
        assertEquals(q1, q2);
        assertEquals(q1.hashCode(), q2.hashCode());
        assertNotEquals(q1, q3);
    }
}