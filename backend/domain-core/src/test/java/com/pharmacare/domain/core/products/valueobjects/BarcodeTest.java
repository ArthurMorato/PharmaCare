package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para Barcode.
 */
class BarcodeTest {
    
    @Test
    void shouldCreateValidBarcode() {
        // Arrange
        String validBarcode = "7891234567895"; // EAN-13 válido
        
        // Act
        Barcode barcode = Barcode.from(validBarcode);
        
        // Assert
        assertNotNull(barcode);
        assertEquals("7891234567895", barcode.getValue());
        assertEquals("78912-34567-895", barcode.getFormattedValue());
    }
    
    @Test
    void shouldRemoveNonNumericCharacters() {
        // Arrange
        String barcodeWithFormat = "789-123456-7895";
        
        // Act
        Barcode barcode = Barcode.from(barcodeWithFormat);
        
        // Assert
        assertEquals("7891234567895", barcode.getValue());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "123456789012",      // 12 dígitos
        "12345678901234",    // 14 dígitos
        "12345",            // Muito curto
        "ABCDEFGHIJKLM",    // Não numérico
        "123456789012X"     // Contém letra
    })
    void shouldThrowExceptionWhenInvalidFormat(String invalidBarcode) {
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Barcode.from(invalidBarcode);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenInvalidCheckDigit() {
        // Arrange
        String invalidBarcode = "7891234567890"; // Dígito verificador incorreto
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Barcode.from(invalidBarcode);
        });
    }
    
    @Test
    void shouldExtractCountryCode() {
        // Arrange
        Barcode barcode = Barcode.from("7891234567895");
        
        // Act
        String countryCode = barcode.getCountryCode();
        
        // Assert
        assertEquals("789", countryCode);
    }
    
    @Test
    void shouldExtractManufacturerCode() {
        // Arrange
        Barcode barcode = Barcode.from("7891234567895");
        
        // Act
        String manufacturerCode = barcode.getManufacturerCode();
        
        // Assert
        assertEquals("12345", manufacturerCode);
    }
    
    @Test
    void shouldExtractProductCode() {
        // Arrange
        Barcode barcode = Barcode.from("7891234567895");
        
        // Act
        String productCode = barcode.getProductCode();
        
        // Assert
        assertEquals("6789", productCode);
    }
    
    @Test
    void shouldExtractCheckDigit() {
        // Arrange
        Barcode barcode = Barcode.from("7891234567895");
        
        // Act
        char checkDigit = barcode.getCheckDigit();
        
        // Assert
        assertEquals('5', checkDigit);
    }
    
    @Test
    void shouldCheckCountry() {
        // Arrange
        Barcode barcode = Barcode.from("7891234567895");
        
        // Act & Assert
        assertTrue(barcode.isFromCountry("789")); // Brasil
        assertFalse(barcode.isFromCountry("123"));
    }
    
    @Test
    void shouldBeEqualWhenSameValue() {
        // Arrange
        Barcode barcode1 = Barcode.from("7891234567895");
        Barcode barcode2 = Barcode.from("7891234567895");
        Barcode barcode3 = Barcode.from("789-123456-7895");
        
        // Act & Assert
        assertEquals(barcode1, barcode2);
        assertEquals(barcode1.hashCode(), barcode2.hashCode());
        assertEquals(barcode1, barcode3); // Formatação não afeta igualdade
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        // Arrange
        Barcode barcode1 = Barcode.from("7891234567895");
        Barcode barcode2 = Barcode.from("7891234567896"); // Último dígito diferente
        
        // Act & Assert
        assertNotEquals(barcode1, barcode2);
        assertNotEquals(barcode1.hashCode(), barcode2.hashCode());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Arrange
        Barcode barcode = Barcode.from("7891234567895");
        
        // Act
        String toString = barcode.toString();
        
        // Assert
        assertTrue(toString.contains("Barcode"));
        assertTrue(toString.contains("78912-34567-895"));
    }
    
    @Test
    void shouldHandleRealEAN13Examples() {
        // Testes com EAN-13 reais conhecidos
        String[] validEAN13s = {
            "4006381333931", // Schwarzkopf
            "7613032621199", // Nestlé
            "7891000053508", // Coca-Cola Brasil
            "7891910000197", // Pilhas Duracell
        };
        
        for (String ean : validEAN13s) {
            assertDoesNotThrow(() -> {
                Barcode barcode = Barcode.from(ean);
                assertNotNull(barcode);
                assertEquals(ean, barcode.getValue());
            });
        }
    }
}