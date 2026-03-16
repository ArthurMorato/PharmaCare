package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.inventory.valueobjects.Money;
import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para Money.
 */
class MoneyTest {
    
    @Test
    void shouldCreateValidMoney() {
        // Arrange
        BigDecimal amount = new BigDecimal("25.99");
        
        // Act
        Money money = Money.of(amount);
        
        // Assert
        assertNotNull(money);
        assertEquals(new BigDecimal("25.99"), money.getAmount());
        assertEquals("BRL", money.getCurrencyCode());
        assertEquals("R$ 25,99", money.getFormattedValue());
    }
    
    @Test
    void shouldCreateMoneyFromDouble() {
        // Arrange & Act
        Money money = Money.of(25.99);
        
        // Assert
        assertEquals(new BigDecimal("25.99"), money.getAmount());
    }
    
    @Test
    void shouldCreateMoneyFromString() {
        // Arrange & Act
        Money money = Money.fromString("25.99");
        
        // Assert
        assertEquals(new BigDecimal("25.99"), money.getAmount());
    }
    
    @Test
    void shouldThrowExceptionWhenNegativeAmount() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-10.00");
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Money.of(negativeAmount);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenTooManyDecimals() {
        // Arrange
        BigDecimal amount = new BigDecimal("25.999");
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Money.of(amount);
        });
    }
    
    @Test
    void shouldRoundToTwoDecimals() {
        // Arrange
        BigDecimal amount = new BigDecimal("25.9999");
        
        // Act
        Money money = Money.of(amount);
        
        // Assert
        assertEquals(new BigDecimal("26.00"), money.getAmount());
    }
    
    @Test
    void shouldAddMoney() {
        // Arrange
        Money money1 = Money.of(10.50);
        Money money2 = Money.of(20.75);
        
        // Act
        Money result = money1.add(money2);
        
        // Assert
        assertEquals(new BigDecimal("31.25"), result.getAmount());
    }
    
    @Test
    void shouldSubtractMoney() {
        // Arrange
        Money money1 = Money.of(30.00);
        Money money2 = Money.of(15.50);
        
        // Act
        Money result = money1.subtract(money2);
        
        // Assert
        assertEquals(new BigDecimal("14.50"), result.getAmount());
    }
    
    @Test
    void shouldThrowExceptionWhenSubtractResultNegative() {
        // Arrange
        Money money1 = Money.of(10.00);
        Money money2 = Money.of(20.00);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            money1.subtract(money2);
        });
    }
    
    @Test
    void shouldMultiplyMoney() {
        // Arrange
        Money money = Money.of(10.00);
        
        // Act
        Money result = money.multiply(3);
        
        // Assert
        assertEquals(new BigDecimal("30.00"), result.getAmount());
    }
    
    @Test
    void shouldCalculatePercentage() {
        // Arrange
        Money money = Money.of(100.00);
        
        // Act
        Money result = money.percentage(new BigDecimal("10"));
        
        // Assert
        assertEquals(new BigDecimal("10.00"), result.getAmount());
    }
    
    @Test
    void shouldCompareMoney() {
        // Arrange
        Money money1 = Money.of(10.00);
        Money money2 = Money.of(20.00);
        Money money3 = Money.of(10.00);
        
        // Act & Assert
        assertTrue(money1.isLessThan(money2));
        assertTrue(money2.isGreaterThan(money1));
        assertTrue(money1.isEqualTo(money3));
        assertFalse(money1.isGreaterThan(money3));
        assertFalse(money1.isLessThan(money3));
    }
    
    @Test
    void shouldBeEqualWhenSameAmountAndCurrency() {
        // Arrange
        Money money1 = Money.of(25.99);
        Money money2 = Money.of(25.99);
        Money money3 = Money.of(25.99, "USD");
        
        // Act & Assert
        assertEquals(money1, money2);
        assertEquals(money1.hashCode(), money2.hashCode());
        assertNotEquals(money1, money3); // Moeda diferente
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Arrange
        Money money = Money.of(25.99);
        
        // Act
        String toString = money.toString();
        
        // Assert
        assertTrue(toString.contains("Money"));
        assertTrue(toString.contains("25.99"));
        assertTrue(toString.contains("BRL"));
    }
}