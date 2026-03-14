package com.pharmacare.infrastructure.adapter.input.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de resposta para produto.
 * Inclui HATEOAS links.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "products", itemRelation = "product")
@Schema(description = "Resposta com dados do produto")
public class ProductResponse extends RepresentationModel<ProductResponse> {
    
    @Schema(
        description = "ID único do produto",
        example = "prod-1234567890"
    )
    private String id;
    
    @Schema(
        description = "Nome do produto",
        example = "Paracetamol 500mg"
    )
    private String name;
    
    @Schema(
        description = "Código de barras EAN-13",
        example = "7891234567890"
    )
    private String barcode;
    
    @Schema(
        description = "Descrição do produto",
        example = "Analgésico e antitérmico para dor e febre"
    )
    private String description;
    
    @Schema(
        description = "Categoria do produto",
        example = "MEDICINE"
    )
    private String category;
    
    @Schema(
        description = "Preço de venda",
        example = "12.50"
    )
    private BigDecimal price;
    
    @Schema(
        description = "Moeda",
        example = "BRL"
    )
    private String currency;
    
    @Schema(
        description = "Estoque mínimo",
        example = "50"
    )
    private Integer minimumStock;
    
    @Schema(
        description = "Estoque atual",
        example = "150"
    )
    private Integer currentStock;
    
    @Schema(
        description = "Fabricante",
        example = "EMS"
    )
    private String manufacturer;
    
    @Schema(
        description = "Princípio ativo",
        example = "Paracetamol"
    )
    private String activePrinciple;
    
    @Schema(
        description = "Concentração",
        example = "500mg"
    )
    private String concentration;
    
    @Schema(
        description = "Unidade de medida",
        example = "Comprimido"
    )
    private String unit;
    
    @Schema(
        description = "Status do produto",
        example = "ACTIVE"
    )
    private String status;
    
    @JsonProperty("requiresPrescription")
    @Schema(
        description = "Indica se requer prescrição médica",
        example = "false"
    )
    private boolean prescriptionRequired;
    
    @Schema(
        description = "URL da imagem do produto",
        example = "https://example.com/products/paracetamol.jpg"
    )
    private String imageUrl;
    
    @Schema(
        description = "Indica se o produto é tributável",
        example = "true"
    )
    private boolean taxable;
    
    @Schema(
        description = "Alíquota de ICMS (%)",
        example = "18.0"
    )
    private BigDecimal icmsTaxRate;
    
    @Schema(
        description = "Data de criação",
        example = "2024-01-15T10:30:00"
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Data da última atualização",
        example = "2024-01-20T14:45:00"
    )
    private LocalDateTime updatedAt;
    
    @Schema(
        description = "Metadados adicionais",
        example = "{\"shelfLife\": \"24 meses\", \"storageCondition\": \"Ambiente seco\"}"
    )
    private Map<String, Object> metadata;
    
    @Schema(
        description = "Versão do documento (para concorrência)",
        example = "3"
    )
    private Long version;
}