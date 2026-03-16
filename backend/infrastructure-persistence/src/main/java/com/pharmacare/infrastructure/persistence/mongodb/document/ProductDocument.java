package com.pharmacare.infrastructure.persistence.mongodb.document;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "products")
@CompoundIndex(name = "name_brand_idx", def = "{'name': 1, 'brand': 1}", unique = true)
@CompoundIndex(name = "category_brand_idx", def = "{'category': 1, 'brand': 1}")
@CompoundIndex(name = "supplier_stock_idx", def = "{'supplier': 1, 'quantityInStock': -1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    @Field("code")
    private String code;

    @NotBlank
    @Size(min = 3, max = 100)
    @Field("name")
    private String name;

    @Size(max = 500)
    @Field("description")
    private String description;

    @NotBlank
    @Size(max = 50)
    @Field("brand")
    private String brand;

    @NotBlank
    @Size(max = 50)
    @Field("category")
    private String category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Field("unit_price")
    private BigDecimal unitPrice;

    @NotNull
    @Min(0)
    @Field("quantity_in_stock")
    private Integer quantityInStock;

    @NotBlank
    @Size(max = 100)
    @Field("supplier")
    private String supplier;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    @Field("version")
    private Long version;

    @Field("active")
    private Boolean active;

    // Custom constructor for builder pattern
    public static ProductDocumentBuilder builder() {
        return new ProductDocumentBuilder()
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }
}
