package com.pharmacare.infrastructure.persistence.mongodb.document;

import com.pharmacare.domain.core.model.Category;
import com.pharmacare.domain.core.model.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Documento MongoDB para persistência de produtos.
 * Representa a entidade Product no banco de dados.
 */
@Document(collection = "products")
@CompoundIndex(name = "category_status_idx", def = "{'category': 1, 'status': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {
    
    @Id
    private String id;
    
    @Indexed(unique = true, sparse = true)
    private String barcode;
    
    @Indexed
    private String name;
    
    private String description;
    
    @Indexed
    private Category category;
    
    @Indexed
    private ProductStatus status;
    
    private BigDecimal price;
    
    private Integer minimumStock;
    
    private String manufacturer;
    
    private String activePrinciple;
    
    private String concentration;
    
    private String unit;
    
    private boolean requiresPrescription;
    
    private String imageUrl;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    // Campos de auditoria
    private String createdBy;
    private String updatedBy;
    
    /**
     * Método para atualizar timestamp de modificação
     */
    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }
}