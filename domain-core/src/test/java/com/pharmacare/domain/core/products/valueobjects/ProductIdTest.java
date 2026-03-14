package com.pharmacare.domain.core.products.valueobjects;

import com.pharmacare.domain.core.exceptions.InvalidValueObjectException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ProductId.
 */
class ProductIdTest {
    
    @Test
    void shouldCreateValidProductIdFromUUID() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        
        // Act
        ProductId productId = ProductId.from(uuid);
        
        // Assert
        assertNotNull(productId);
        assertEquals(uuid, productId.getValue());
        assertEquals(uuid.toString().toLowerCase(), productId.getStringValue());
    }
    
    @Test
    void shouldCreateValidProductIdFromString() {
        // Arrange
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        
        // Act
        ProductId productId = ProductId.from(uuidString);
        
        // Assert
        assertNotNull(productId);
        assertEquals(UUID.fromString(uuidString), productId.getValue());
        assertEquals(uuidString.toLowerCase(), productId.getStringValue());
    }
    
    @Test
    void shouldGenerateRandomProductId() {
        // Act
        ProductId productId = ProductId.generate();
        
        // Assert
        assertNotNull(productId);
        assertEquals(4, productId.getVersion()); // UUID versão 4
        assertNotNull(productId.getStringValue());
    }
    
    @Test
    void shouldThrowExceptionWhenNullUUID() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            ProductId.from((UUID) null);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenNullString() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            ProductId.from((String) null);
        });
    }
    
    @Test
    void shouldThrowExceptionWhenInvalidUUIDString() {
        // Arrange
        String[] invalidUUIDs = {
            "invalid-uuid",
            "12345",
            "123e4567-e89b-12d3-a456-42661417400", // Muito curto
            "123e4567-e89b-12d3-a456-4266141740000", // Muito longo
            "G23e4567-e89b-12d3-a456-426614174000" // Caractere inválido
        };
        
        // Act & Assert
        for (String invalidUUID : invalidUUIDs) {
            assertThrows(InvalidValueObjectException.class, () -> {
                ProductId.from(invalidUUID);
            });
        }
    }
    
    @Test
    void shouldThrowExceptionWhenNotVersion4() {
        // Arrange
        // UUID versão 1 (baseado em tempo)
        String version1UUID = "d5a3b1c0-7c9a-11eb-9439-0242ac130002";
        
        // Act & Assert
        assertThrows(InvalidValueObjectException.class, () -> {
            ProductId.from(version1UUID);
        });
    }
    
    @Test
    void shouldGetCompactString() {
        // Arrange
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ProductId productId = ProductId.from(uuid);
        
        // Act
        String compact = productId.getCompactString();
        
        // Assert
        assertEquals("123e4567e89b12d3a456426614174000", compact);
        assertFalse(compact.contains("-"));
    }
    
    @Test
    void shouldGetVersionAndVariant() {
        // Arrange
        ProductId productId = ProductId.generate();
        
        // Act
        int version = productId.getVersion();
        int variant = productId.getVariant();
        
        // Assert
        assertEquals(4, version); // UUID versão 4 (aleatório)
        assertEquals(2, variant); // Variante RFC 4122
    }
    
    @Test
    void shouldReturnNullTimestampForVersion4() {
        // Arrange
        ProductId productId = ProductId.generate();
        
        // Act
        Long timestamp = productId.getTimestamp();
        
        // Assert
        assertNull(timestamp); // UUID versão 4 não tem timestamp
    }
    
    @Test
    void shouldCheckIfNull() {
        // Arrange
        UUID nullUUID = new UUID(0L, 0L);
        ProductId nullId = ProductId.from(nullUUID);
        ProductId nonNullId = ProductId.generate();
        
        // Act & Assert
        assertTrue(nullId.isNull());
        assertFalse(nonNullId.isNull());
    }
    
    @Test
    void shouldBeEqualWhenSameUUID() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        ProductId id1 = ProductId.from(uuid);
        ProductId id2 = ProductId.from(uuid);
        ProductId id3 = ProductId.from(uuid.toString());
        
        // Act & Assert
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertEquals(id1, id3);
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentUUIDs() {
        // Arrange
        ProductId id1 = ProductId.generate();
        ProductId id2 = ProductId.generate();
        
        // Act & Assert
        assertNotEquals(id1, id2);
        assertNotEquals(id1.hashCode(), id2.hashCode());
    }
    
    @Test
    void shouldBeCaseInsensitiveInEquality() {
        // Arrange
        String upperUUID = "123E4567-E89B-12D3-A456-426614174000";
        String lowerUUID = "123e4567-e89b-12d3-a456-426614174000";
        
        ProductId id1 = ProductId.from(upperUUID);
        ProductId id2 = ProductId.from(lowerUUID);
        
        // Act & Assert
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
    
    @Test
    void shouldReturnCorrectToString() {
        // Arrange
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ProductId productId = ProductId.from(uuid);
        
        // Act
        String toString = productId.toString();
        
        // Assert
        assertTrue(toString.contains("ProductId"));
        assertTrue(toString.contains("123e4567-e89b-12d3-a456-426614174000"));
    }
}