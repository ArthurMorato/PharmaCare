package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ProductName.
 */
class ProductNameTest {
    
    @Test
    void shouldCreateValidProductName() {
        // Arrange
        String validName = "Paracetamol 500mg";
        
        // Act
        ProductName productName = ProductName.from(validName);
        
        // Assert
        assertNotNull(productName);
        assertEquals("Paracetamol 500mg", productName.getValue());
        assertEquals("Paracetamol 500mg", productName.getNormalizedValue());
    }
    
    @Test
    void shouldTrimSpaces() {
        // Arrange
        String nameWithSpaces = "  Paracetamol 500mg  ";
        
        // Act
        ProductName productName = ProductName.from(nameWithSpaces);
        
        // Assert
        assertEquals("Paracetamol 500mg", productName.getValue());
    }
    
    @Test
    void shouldNormalizeMultipleSpaces() {
        // Arrange
        String nameWithMultipleSpaces = "Paracetamol   500mg  Comprimido";
        
        // Act
        ProductName productName = ProductName.from(nameWithMultipleSpaces);
        
        // Assert
        assertEquals("Paracetamol 500mg Comprimido", productName.getNormalizedValue());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "Ab",           // Muito curto
        "",             // Vazio
        "   ",          // Apenas espaços
        "X".repeat(101) // Muito longo
    })
    void shouldThrowExceptionWhenInvalidLength(String invalidName) {
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            ProductName.from(invalidName);
        });
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "Produto@Inválido",
        "Produto#Inválido",
        "Produto$Inválido",
        "Produto%Inválido"
    })
    void shouldThrowExceptionWhenInvalidCharacters(String invalidName) {
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            ProductName.from(invalidName);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenOnlyNumbers() {
        // Arrange
        String onlyNumbers = "1234567890";
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            ProductName.from(onlyNumbers);
        });
    }
    
    @Test
    void shouldConvertToUpperCase() {
        // Arrange
        ProductName productName = ProductName.from("Paracetamol 500mg");
        
        // Act
        String upperCase = productName.toUpperCase();
        
        // Assert
        assertEquals("PARACETAMOL 500MG", upperCase);
    }
    
    @Test
    void shouldConvertToLowerCase() {
        // Arrange
        ProductName productName = ProductName.from("PARACETAMOL 500MG");
        
        // Act
        String lowerCase = productName.toLowerCase();
        
        // Assert
        assertEquals("paracetamol 500mg", lowerCase);
    }
    
    @Test
    void shouldCheckContainsIgnoreCase() {
        // Arrange
        ProductName productName = ProductName.from("Paracetamol 500mg Comprimido");
        
        // Act & Assert
        assertTrue(productName.containsIgnoreCase("paracetamol"));
        assertTrue(productName.containsIgnoreCase("500MG"));
        assertTrue(productName.containsIgnoreCase("comprimido"));
        assertFalse(productName.containsIgnoreCase("aspirina"));
    }
    
    @Test
    void shouldBeEqualWhenSameNormalizedValue() {
        // Arrange
        ProductName name1 = ProductName.from("Paracetamol 500mg");
        ProductName name2 = ProductName.from("Paracetamol  500mg");
        ProductName name3 = ProductName.from("paracetamol 500mg");
        
        // Act & Assert
        assertEquals(name1, name2);
        assertEquals(name1.hashCode(), name2.hashCode());
        assertEquals(name1, name3); // Case-insensitive equality
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        // Arrange
        ProductName name1 = ProductName.from("Paracetamol 500mg");
        ProductName name2 = ProductName.from("Ibuprofeno 400mg");
        
        // Act & Assert
        assertNotEquals(name1, name2);
        assertNotEquals(name1.hashCode(), name2.hashCode());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Arrange
        ProductName productName = ProductName.from("Paracetamol 500mg");
        
        // Act
        String toString = productName.toString();
        
        // Assert
        assertTrue(toString.contains("ProductName"));
        assertTrue(toString.contains("Paracetamol 500mg"));
    }
}