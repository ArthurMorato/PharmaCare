package com.pharmacare.infrastructure.adapter.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para criação de produto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para criação de um novo produto")
public class CreateProductRequest {
    
    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 3, max = 200, message = "O nome deve ter entre 3 e 200 caracteres")
    @Schema(
        description = "Nome do produto",
        example = "Paracetamol 500mg",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;
    
    @NotBlank(message = "O código de barras é obrigatório")
    @Pattern(regexp = "\\d{13}", message = "O código de barras deve ter 13 dígitos")
    @Schema(
        description = "Código de barras EAN-13",
        example = "7891234567890",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String barcode;
    
    @Size(max = 1000, message = "A descrição não pode exceder 1000 caracteres")
    @Schema(
        description = "Descrição detalhada do produto",
        example = "Analgésico e antitérmico para dor e febre"
    )
    private String description;
    
    @NotBlank(message = "A categoria é obrigatória")
    @Schema(
        description = "Categoria do produto",
        example = "MEDICINE",
        allowableValues = {"MEDICINE", "COSMETIC", "PERSONAL_CARE", "SUPPLEMENT", "EQUIPMENT"}
    )
    private String category;
    
    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @DecimalMax(value = "999999.99", message = "O preço não pode exceder 999999.99")
    @Schema(
        description = "Preço de venda do produto",
        example = "12.50",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal price;
    
    @Min(value = 0, message = "O estoque mínimo não pode ser negativo")
    @Schema(
        description = "Estoque mínimo para alerta",
        example = "50"
    )
    private Integer minimumStock;
    
    @Schema(
        description = "Fabricante do produto",
        example = "EMS"
    )
    private String manufacturer;
    
    @Schema(
        description = "Princípio ativo (para medicamentos)",
        example = "Paracetamol"
    )
    private String activePrinciple;
    
    @Schema(
        description = "Concentração (para medicamentos)",
        example = "500mg"
    )
    private String concentration;
    
    @Schema(
        description = "Unidade de medida",
        example = "Comprimido"
    )
    private String unit;
    
    @Schema(
        description = "Indica se requer prescrição médica",
        example = "false"
    )
    private boolean requiresPrescription;
    
    @Schema(
        description = "URL da imagem do produto",
        example = "https://example.com/products/paracetamol.jpg"
    )
    private String imageUrl;
    
    @JsonProperty("isTaxable")
    @Schema(
        description = "Indica se o produto é tributável",
        example = "true"
    )
    private boolean taxable;
    
    @DecimalMin(value = "0.0", message = "A alíquota de ICMS não pode ser negativa")
    @DecimalMax(value = "100.0", message = "A alíquota de ICMS não pode exceder 100%")
    @Schema(
        description = "Alíquota de ICMS (%)",
        example = "18.0"
    )
    private BigDecimal icmsTaxRate;
}