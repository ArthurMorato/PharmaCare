package com.pharmacare.domain.core.inventory.valueobjects;

import com.pharmacare.domain.core.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ExpirationDate.
 */
class ExpirationDateTest {
    
    @Test
    void shouldCreateValidExpirationDate() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(30);
        
        // Act
        ExpirationDate expirationDate = ExpirationDate.of(futureDate);
        
        // Assert
        assertNotNull(expirationDate);
        assertEquals(futureDate, expirationDate.getDate());
    }
    
    @Test
    void shouldCreateFromString() {
        // Arrange
        String futureDateStr = LocalDate.now().plusDays(30).toString();
        
        // Act
        ExpirationDate expirationDate = ExpirationDate.fromString(futureDateStr);
        
        // Assert
        assertNotNull(expirationDate);
        assertEquals(futureDateStr, expirationDate.getFormattedDate());
    }
    
    @Test
    void shouldCreateDaysFromNow() {
        // Arrange & Act
        ExpirationDate expirationDate = ExpirationDate.daysFromNow(30);
        
        // Assert
        assertEquals(LocalDate.now().plusDays(30), expirationDate.getDate());
    }
    
    @Test
    void shouldCreateMonthsFromNow() {
        // Arrange & Act
        ExpirationDate expirationDate = ExpirationDate.monthsFromNow(6);
        
        // Assert
        assertEquals(LocalDate.now().plusMonths(6), expirationDate.getDate());
    }
    
    @Test
    void shouldCreateYearsFromNow() {
        // Arrange & Act
        ExpirationDate expirationDate = ExpirationDate.yearsFromNow(2);
        
        // Assert
        assertEquals(LocalDate.now().plusYears(2), expirationDate.getDate());
    }
    
    @Test
    void shouldThrowExceptionWhenNullDate() {
        // Arrange & Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            ExpirationDate.of(null);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenPastDate() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusDays(1);
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            ExpirationDate.of(pastDate);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenToday() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            ExpirationDate.of(today);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenMoreThan5Years() {
        // Arrange
        LocalDate distantFuture = LocalDate.now().plusYears(6);
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            ExpirationDate.of(distantFuture);
        });
    }
    
    @Test
    void shouldCheckIfNearExpiration() {
        // Arrange
        ExpirationDate nearFuture = ExpirationDate.daysFromNow(15);
        ExpirationDate farFuture = ExpirationDate.daysFromNow(60);
        
        // Act & Assert
        assertTrue(nearFuture.isNearExpiration());
        assertFalse(farFuture.isNearExpiration());
    }
    
    @Test
    void shouldCheckIfExpired() {
        // Arrange
        LocalDate pastDate = LocalDate.now().minusDays(1);
        
        // Simula data passada (precisa contornar a validação)
        // Vamos testar apenas a lógica isExpired() usando reflexão
        ExpirationDate expirationDate = null;
        try {
            // Cria uma data futura primeiro
            expirationDate = ExpirationDate.daysFromNow(30);
            
            // Usa reflexão para mudar a data interna (apenas para teste)
            var field = ExpirationDate.class.getDeclaredField("date");
            field.setAccessible(true);
            field.set(expirationDate, pastDate);
        } catch (Exception e) {
            // Ignora para teste
        }
        
        // Act & Assert
        if (expirationDate != null) {
            assertTrue(expirationDate.isExpired());
        }
    }
    
    @Test
    void shouldCheckIfFarFromExpiration() {
        // Arrange
        ExpirationDate nearFuture = ExpirationDate.daysFromNow(15);
        ExpirationDate farFuture = ExpirationDate.yearsFromNow(2);
        
        // Act & Assert
        assertFalse(nearFuture.isFarFromExpiration());
        assertTrue(farFuture.isFarFromExpiration());
    }
    
    @Test
    void shouldCalculateDaysUntilExpiration() {
        // Arrange
        int daysToAdd = 30;
        ExpirationDate expirationDate = ExpirationDate.daysFromNow(daysToAdd);
        
        // Act
        long daysUntil = expirationDate.daysUntilExpiration();
        
        // Assert
        assertEquals(daysToAdd, daysUntil);
    }
    
    @Test
    void shouldCalculateMonthsUntilExpiration() {
        // Arrange
        int monthsToAdd = 3;
        ExpirationDate expirationDate = ExpirationDate.monthsFromNow(monthsToAdd);
        
        // Act
        long monthsUntil = expirationDate.monthsUntilExpiration();
        
        // Assert
        assertEquals(monthsToAdd, monthsUntil);
    }
    
    @Test
    void shouldFormatBrazilianDate() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 12, 31);
        ExpirationDate expirationDate = ExpirationDate.of(date);
        
        // Act
        String brazilianFormat = expirationDate.getBrazilianFormat();
        
        // Assert
        assertEquals("31/12/2024", brazilianFormat);
    }
    
    @Test
    void shouldBeEqualWhenSameDate() {
        // Arrange
        LocalDate date1 = LocalDate.now().plusDays(30);
        LocalDate date2 = LocalDate.now().plusDays(30);
        LocalDate date3 = LocalDate.now().plusDays(60);
        
        ExpirationDate ed1 = ExpirationDate.of(date1);
        ExpirationDate ed2 = ExpirationDate.of(date2);
        ExpirationDate ed3 = ExpirationDate.of(date3);
        
        // Act & Assert
        assertEquals(ed1, ed2);
        assertEquals(ed1.hashCode(), ed2.hashCode());
        assertNotEquals(ed1, ed3);
    }
}