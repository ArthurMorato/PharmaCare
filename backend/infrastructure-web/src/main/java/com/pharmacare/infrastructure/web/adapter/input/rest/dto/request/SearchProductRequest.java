package com.pharmacare.infrastructure.adapter.input.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

import java.util.Set;

/**
 * DTO para busca de produtos com filtros.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "Request para busca de produtos com filtros")
public class SearchProductRequest {
    
    @Schema(
        description = "Nome do produto (busca parcial)",
        example = "paracetamol"
    )
    private String name;
    
    @Pattern(regexp = "\\d{13}", message = "O código de barras deve ter 13 dígitos")
    @Schema(
        description = "Código de barras exato",
        example = "7891234567890"
    )
    private String barcode;
    
    @Schema(
        description = "Categoria do produto",
        example = "MEDICINE",
        allowableValues = {"MEDICINE", "COSMETIC", "PERSONAL_CARE", "SUPPLEMENT", "EQUIPMENT"}
    )
    private String category;
    
    @Schema(
        description = "Status do produto",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "INACTIVE", "DISCONTINUED"}
    )
    private String status;
    
    @Schema(
        description = "Indica se requer prescrição médica",
        example = "false"
    )
    private Boolean requiresPrescription;
    
    @Schema(
        description = "Fabricante do produto",
        example = "EMS"
    )
    private String manufacturer;
    
    @Schema(
        description = "Preço mínimo",
        example = "10.00"
    )
    private Double minPrice;
    
    @Schema(
        description = "Preço máximo",
        example = "100.00"
    )
    private Double maxPrice;
    
    @Schema(
        description = "Lista de IDs de produtos",
        example = "[\"prod-123\", \"prod-456\"]"
    )
    private Set<String> ids;
    
    @Min(value = 0, message = "A página não pode ser negativa")
    @Schema(
        description = "Número da página (0-based)",
        example = "0",
        defaultValue = "0"
    )
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "O tamanho da página deve ser pelo menos 1")
    @Max(value = 100, message = "O tamanho da página não pode exceder 100")
    @Schema(
        description = "Tamanho da página",
        example = "20",
        defaultValue = "20"
    )
    @Builder.Default
    private Integer size = 20;
    
    @Schema(
        description = "Campo para ordenação",
        example = "name",
        allowableValues = {"name", "price", "createdAt", "updatedAt"}
    )
    @Builder.Default
    private String sortBy = "name";
    
    @Schema(
        description = "Direção da ordenação",
        example = "ASC",
        allowableValues = {"ASC", "DESC"}
    )
    @Builder.Default
    private String sortDirection = "ASC";
}