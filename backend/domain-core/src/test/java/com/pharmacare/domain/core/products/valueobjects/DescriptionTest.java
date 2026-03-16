package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.products.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para Description.
 */
class DescriptionTest {
    
    @Test
    void shouldCreateValidDescription() {
        // Arrange
        String validDescription = "Analgésico e antitérmico. Alivia dores e reduz febre.";
        
        // Act
        Description description = Description.from(validDescription);
        
        // Assert
        assertNotNull(description);
        assertEquals(validDescription, description.getValue());
        assertFalse(description.isEmpty());
    }
    
    @Test
    void shouldCreateEmptyDescription() {
        // Arrange & Act
        Description empty = Description.empty();
        Description fromNull = Description.from(null);
        Description fromEmpty = Description.from("");
        
        // Assert
        assertTrue(empty.isEmpty());
        assertTrue(fromNull.isEmpty());
        assertTrue(fromEmpty.isEmpty());
        assertEquals("", empty.getValue());
        assertEquals(0, empty.length());
    }
    
    @Test
    void shouldTrimSpaces() {
        // Arrange
        String descriptionWithSpaces = "  Analgésico e antitérmico.  ";
        
        // Act
        Description description = Description.from(descriptionWithSpaces);
        
        // Assert
        assertEquals("Analgésico e antitérmico.", description.getValue());
    }
    
    @Test
    void shouldThrowExceptionWhenTooLong() {
        // Arrange
        String longDescription = "X".repeat(501);
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            Description.from(longDescription);
        });
    }
    
    @Test
    void shouldAcceptMaximumLength() {
        // Arrange
        String maxLengthDescription = "X".repeat(500);
        
        // Act
        Description description = Description.from(maxLengthDescription);
        
        // Assert
        assertNotNull(description);
        assertEquals(500, description.length());
    }
    
    @Test
    void shouldThrowExceptionWhenStartsWithPunctuation() {
        // Arrange
        String[] invalidDescriptions = {
            ".Analgésico",
            ",Analgésico",
            ";Analgésico",
            ":Analgésico",
            "!Analgésico",
            "?Analgésico",
            "-Analgésico"
        };
        
        // Act & Assert
        for (String desc : invalidDescriptions) {
            assertThrows(InvalidValueObjectException.class, () -> {
                Description.from(desc);
            });
        }
    }
    
    @Test
    void shouldThrowExceptionWhenEndsWithPunctuation() {
        // Arrange
        String[] invalidDescriptions = {
            "Analgésico.",
            "Analgésico,",
            "Analgésico;",
            "Analgésico:",
            "Analgésico!",
            "Analgésico?",
            "Analgésico-"
        };
        
        // Act & Assert
        for (String desc : invalidDescriptions) {
            assertThrows(InvalidValueObjectException.class, () -> {
                Description.from(desc);
            });
        }
    }
    
    @Test
    void shouldTruncateDescription() {
        // Arrange
        String longDescription = "Este é um texto muito longo que precisa ser truncado para caber em espaços menores.";
        Description description = Description.from(longDescription);
        
        // Act
        String truncated = description.truncate(20);
        
        // Assert
        assertEquals(20, truncated.length());
        assertTrue(truncated.endsWith("..."));
    }
    
    @Test
    void shouldNotTruncateWhenShorter() {
        // Arrange
        String shortDescription = "Texto curto";
        Description description = Description.from(shortDescription);
        
        // Act
        String truncated = description.truncate(50);
        
        // Assert
        assertEquals(shortDescription, truncated);
    }
    
    @Test
    void shouldConvertToHtmlSafe() {
        // Arrange
        String descriptionWithSpecialChars = "Analgésico <especial> & 'para' dores";
        Description description = Description.from(descriptionWithSpecialChars);
        
        // Act
        String htmlSafe = description.toHtmlSafe();
        
        // Assert
        assertFalse(htmlSafe.contains("<"));
        assertFalse(htmlSafe.contains(">"));
        assertTrue(htmlSafe.contains("&lt;"));
        assertTrue(htmlSafe.contains("&gt;"));
    }
    
    @Test
    void shouldConcatDescriptions() {
        // Arrange
        Description desc1 = Description.from("Primeira linha.");
        Description desc2 = Description.from("Segunda linha.");
        
        // Act
        Description combined = desc1.concat(desc2);
        
        // Assert
        assertEquals("Primeira linha.\n\nSegunda linha.", combined.getValue());
    }
    
    @Test
    void shouldHandleEmptyInConcat() {
        // Arrange
        Description desc1 = Description.from("Primeira linha.");
        Description empty = Description.empty();
        
        // Act
        Description result1 = desc1.concat(empty);
        Description result2 = empty.concat(desc1);
        
        // Assert
        assertEquals(desc1, result1);
        assertEquals(desc1, result2);
    }
    
    @Test
    void shouldBeEqualWhenSameValue() {
        // Arrange
        Description desc1 = Description.from("Analgésico e antitérmico");
        Description desc2 = Description.from("Analgésico e antitérmico");
        Description desc3 = Description.from("  Analgésico e antitérmico  ");
        
        // Act & Assert
        assertEquals(desc1, desc2);
        assertEquals(desc1.hashCode(), desc2.hashCode());
        assertEquals(desc1, desc3); // Trim não afeta igualdade
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentValues() {
        // Arrange
        Description desc1 = Description.from("Analgésico");
        Description desc2 = Description.from("Antitérmico");
        
        // Act & Assert
        assertNotEquals(desc1, desc2);
        assertNotEquals(desc1.hashCode(), desc2.hashCode());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Arrange
        Description description = Description.from("Analgésico");
        
        // Act
        String toString = description.toString();
        
        // Assert
        assertTrue(toString.contains("Description"));
        assertTrue(toString.contains("length"));
    }
}