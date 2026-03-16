package com.pharmacare.application.product.commands;

import jakarta.validation.constraints.*;
import com.pharmacare.domain.core.inventory.enums.ProductCategory;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Command para criação de novo produto.
 * 
 * Todas as validações são feitas usando Bean Validation.
 * Regras específicas de negócio são tratadas no use case.
 */
public record CreateProductCommand(
    
    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    String name,
    
    @NotBlank(message = "Código de barras é obrigatório")
    @Pattern(
        regexp = "^[0-9]{8,14}$", 
        message = "Código de barras deve conter apenas números e ter entre 8 e 14 dígitos"
    )
    String barcode,
    
    @Size(max = 1000, message = "Descrição não pode exceder 1000 caracteres")
    String description,
    
    @NotBlank(message = "Marca é obrigatória")
    @Size(min = 2, max = 100, message = "Marca deve ter entre 2 e 100 caracteres")
    String brand,
    
    @NotNull(message = "Categoria é obrigatória")
    ProductCategory category,
    
    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @DecimalMax(value = "999999.99", message = "Preço não pode exceder 999999.99")
    BigDecimal price,
    
    @DecimalMin(value = "0.00", message = "Custo não pode ser negativo")
    @DecimalMax(value = "999999.99", message = "Custo não pode exceder 999999.99")
    BigDecimal cost,
    
    @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
    @Max(value = 999999, message = "Estoque mínimo não pode exceder 999999")
    Integer minimumStock,
    
    @NotNull(message = "Unidade de medida é obrigatória")
    @Size(min = 1, max = 20, message = "Unidade deve ter entre 1 e 20 caracteres")
    String unit,
    
    @NotNull(message = "Controlado é obrigatório")
    Boolean controlled,
    
    @NotNull(message = "Tributável é obrigatório")
    Boolean taxable,
    
    @Size(max = 50, message = "Código NCM não pode exceder 50 caracteres")
    String ncmCode,
    
    @Size(max = 50, message = "Código CEST não pode exceder 50 caracteres")
    String cestCode,
    
    @Size(max = 100, message = "Fabricante não pode exceder 100 caracteres")
    String manufacturer,
    
    @Size(max = 100, message = "Fornecedor não pode exceder 100 caracteres")
    String supplier,
    
    @NotNull(message = "Ativo é obrigatório")
    Boolean active,
    
    @NotNull(message = "Impostos são obrigatórios")
    @DecimalMin(value = "0.00", message = "ICMS não pode ser negativo")
    @DecimalMax(value = "100.00", message = "ICMS não pode exceder 100%")
    BigDecimal icmsTax,
    
    @NotNull(message = "Impostos são obrigatórios")
    @DecimalMin(value = "0.00", message = "PIS não pode ser negativo")
    @DecimalMax(value = "100.00", message = "PIS não pode exceder 100%")
    BigDecimal pisTax,
    
    @NotNull(message = "Impostos são obrigatórios")
    @DecimalMin(value = "0.00", message = "COFINS não pode ser negativo")
    @DecimalMax(value = "100.00", message = "COFINS não pode exceder 100%")
    BigDecimal cofinsTax,
    
    Set<String> tags,
    
    @Size(max = 255, message = "URL da imagem não pode exceder 255 caracteres")
    String imageUrl,
    
    @NotNull(message = "Necessita receita é obrigatório")
    Boolean requiresPrescription,
    
    @Size(max = 1000, message = "Instruções não podem exceder 1000 caracteres")
    String instructions,
    
    @Size(max = 1000, message = "Efeitos colaterais não podem exceder 1000 caracteres")
    String sideEffects,
    
    @Size(max = 500, message = "Composição não pode exceder 500 caracteres")
    String composition,
    
    @Size(max = 100, message = "Registro ANVISA não pode exceder 100 caracteres")
    String anvisaRegistration,
    
    @NotNull(message = "Criado por é obrigatório")
    @Size(min = 1, max = 100, message = "Criado por deve ter entre 1 e 100 caracteres")
    String createdBy
    
) {
    
    /**
     * Validações customizadas que não são cobertas pelo Bean Validation.
     */
    public void validateCustomRules() {
        // Custo não pode ser maior que o preço
        if (cost != null && price != null && cost.compareTo(price) > 0) {
            throw new IllegalArgumentException("Custo não pode ser maior que o preço de venda");
        }
        
        // Medicamentos controlados devem ter registro ANVISA
        if (Boolean.TRUE.equals(controlled) && 
            (anvisaRegistration == null || anvisaRegistration.trim().isEmpty())) {
            throw new IllegalArgumentException("Produtos controlados necessitam de registro ANVISA");
        }
        
        // Medicamentos que necessitam receita devem ser controlados
        if (Boolean.TRUE.equals(requiresPrescription) && 
            !Boolean.TRUE.equals(controlled)) {
            throw new IllegalArgumentException("Produtos que necessitam receita devem ser controlados");
        }
        
        // Valida categoria específica
        validateCategoryRules();
    }
    
    /**
     * Valida regras específicas por categoria.
     */
    private void validateCategoryRules() {
        if (category == null) return;
        
        switch (category) {
            case MEDICINE:
                // Medicamentos devem ter instruções
                if (instructions == null || instructions.trim().isEmpty()) {
                    throw new IllegalArgumentException("Medicamentos devem ter instruções de uso");
                }
                break;
                
            case COSMETIC:
                // Cosméticos devem ter composição
                if (composition == null || composition.trim().isEmpty()) {
                    throw new IllegalArgumentException("Cosméticos devem ter composição declarada");
                }
                break;
                
            case SUPPLEMENT:
                // Suplementos devem ter efeitos colaterais
                if (sideEffects == null || sideEffects.trim().isEmpty()) {
                    throw new IllegalArgumentException("Suplementos devem listar efeitos colaterais");
                }
                break;
        }
    }
    
    /**
     * Factory method para criar command com valores default.
     */
    public static CreateProductCommand withDefaults(String name, String barcode, String brand,
                                                   ProductCategory category, BigDecimal price,
                                                   String createdBy) {
        return new CreateProductCommand(
            name,
            barcode,
            "",
            brand,
            category,
            price,
            BigDecimal.ZERO,
            5,
            "un",
            false,
            true,
            "",
            "",
            "",
            "",
            true,
            BigDecimal.valueOf(18.0),
            BigDecimal.valueOf(1.65),
            BigDecimal.valueOf(7.6),
            Set.of(),
            "",
            false,
            "",
            "",
            "",
            "",
            createdBy
        );
    }
    
    /**
     * Retorna o preço formatado.
     */
    public String getFormattedPrice() {
        return String.format("R$ %.2f", price);
    }
}