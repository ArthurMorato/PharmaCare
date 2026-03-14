package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para Brand.
 */
class BrandTest {
    
    @Test
    void shouldCreateValidBrand() {
        // Arrange
        String validBrand = "Medley";
        
        // Act
        Brand brand = Brand.from(validBrand);
        
        // Assert
        assertNotNull(brand);
        assertEquals("Medley", brand.getValue());
        assertEquals("Medley", brand.getNormalizedValue());
    }
    
    @Test
    void shouldTrimSpaces() {
        // Arrange
        String brandWithSpaces = "  Medley  ";
        
        // Act
        Brand brand = Brand.from(brandWithSpaces);
        
        // Assert
        assertEquals("Medley", brand.getValue());
    }
    
    @Test
    void shouldNormalizeMultipleSpaces() {
        // Arrange
        String brandWithMultipleSpaces = "Euro  Farma";
        
        // Act
        Brand brand = Brand.from(brandWithMultipleSpaces);
        
        // Assert
        assertEquals("Euro Farma", brand.getNormalizedValue());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "",             // Vazio
        "   ",          // Apenas espaços
        "X".repeat(51)  // Muito longo
    })
    void shouldThrowExceptionWhenInvalidLength(String invalidBrand) {
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Brand.from(invalidBrand);
        });
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "Marca@Inválida",
        "Marca#Inválida",
        "Marca$Inválida",
        "Marca%Inválida"
    })
    void shouldThrowExceptionWhenInvalidCharacters(String invalidBrand) {
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Brand.from(invalidBrand);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenOnlyNumbers() {
        // Arrange
        String onlyNumbers = "123456";
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Brand.from(onlyNumbers);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenStartsWithSpecialChar() {
        // Arrange
        String[] invalidBrands = {
            ".Marca",
            ",Marca",
            "-Marca",
            "&Marca"
        };
        
        // Act & Assert
        for (String brand : invalidBrands) {
            assertThrows(InvalidValueObjectException.class, () -> {
                Brand.from(brand);
            });
        }
    }
    
    @Test
    void shouldThrowExceptionWhenEndsWithSpecialChar() {
        // Arrange
        String[] invalidBrands = {
            "Marca.",
            "Marca,",
            "Marca-",
            "Marca&"
        };
        
        // Act & Assert
        for (String brand : invalidBrands) {
            assertThrows(InvalidValueObjectException.class, () -> {
                Brand.from(brand);
            });
        }
    }
    
    @Test
    void shouldAcceptValidSpecialCharacters() {
        // Arrange
        String[] validBrands = {
            "Johnson & Johnson",
            "Bayer HealthCare",
            "Novartis - Pharma",
            "Roche S.A."
        };
        
        // Act & Assert
        for (String brand : validBrands) {
            assertDoesNotThrow(() -> {
                Brand b = Brand.from(brand);
                assertNotNull(b);
                assertEquals(brand.trim(), b.getValue());
            });
        }
    }
    
    @Test
    void shouldBeEqualWhenSameNormalizedValue() {
        // Arrange
        Brand brand1 = Brand.from("Medley");
        Brand brand2 = Brand.from("Medley");
        Brand brand3 = Brand.from("  Medley  ");
        Brand brand4 = Brand.from("MEDLEY");
        
        // Act & Assert
        assertEquals(brand1, brand2);
        assertEquals(brand1.hashCode(), brand2.hashCode());
        assertEquals(brand1, brand3); // Trim não afeta igualdade
        assertEquals(brand1, brand4); // Case-insensitive equality
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        // Arrange
        Brand brand1 = Brand.from("Medley");
        Brand brand2 = Brand.from("Eurofarma");
        
        // Act & Assert
        assertNotEquals(brand1, brand2);
        assertNotEquals(brand1.hashCode(), brand2.hashCode());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Arrange
        Brand brand = Brand.from("Medley");
        
        // Act
        String toString = brand.toString();
        
        // Assert
        assertTrue(toString.contains("Brand"));
        assertTrue(toString.contains("Medley"));
    }
}