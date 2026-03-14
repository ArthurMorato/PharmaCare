package com.pharmacare.infrastructure.adapter.input.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para atualização de produto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para atualização de um produto existente")
public class UpdateProductRequest {
    
    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 3, max = 200, message = "O nome deve ter entre 3 e 200 caracteres")
    @Schema(
        description = "Nome do produto",
        example = "Paracetamol 500mg - Nova Formulação"
    )
    private String name;
    
    @Size(max = 1000, message = "A descrição não pode exceder 1000 caracteres")
    @Schema(
        description = "Descrição detalhada do produto",
        example = "Analgésico e antitérmico para dor e febre - Nova embalagem"
    )
    private String description;
    
    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @DecimalMax(value = "999999.99", message = "O preço não pode exceder 999999.99")
    @Schema(
        description = "Preço de venda do produto",
        example = "13.75"
    )
    private BigDecimal price;
    
    @Min(value = 0, message = "O estoque mínimo não pode ser negativo")
    @Schema(
        description = "Estoque mínimo para alerta",
        example = "60"
    )
    private Integer minimumStock;
    
    @Schema(
        description = "Fabricante do produto",
        example = "EMS"
    )
    private String manufacturer;
    
    @Schema(
        description = "Status do produto",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "INACTIVE", "DISCONTINUED"}
    )
    private String status;
    
    @Schema(
        description = "URL da imagem do produto",
        example = "https://example.com/products/paracetamol-v2.jpg"
    )
    private String imageUrl;
    
    @Schema(
        description = "Indica se requer prescrição médica",
        example = "false"
    )
    private boolean requiresPrescription;
}