package com.pharmacare.domain.core.inventory.enums;

/**
 * Categorias de produtos farmacêuticos.
 * Baseado na classificação ANVISA e necessidades do domínio.
 */
public enum ProductCategory {
    // Medicamentos
    MEDICINE("Medicamento", "Produtos com finalidade terapêutica"),
    GENERIC("Genérico", "Medicamento genérico intercambiável"),
    SIMILAR("Similar", "Medicamento similar"),
    REFERENCED("Referência", "Medicamento de referência"),
    
    // Cosméticos
    COSMETIC("Cosmético", "Produtos de higiene e beleza"),
    HYGIENE("Higiene", "Produtos de higiene pessoal"),
    PERFUME("Perfume", "Perfumes e colônias"),
    
    // Equipamentos
    EQUIPMENT("Equipamento", "Equipamentos médicos e hospitalares"),
    INSTRUMENT("Instrumento", "Instrumentos médicos"),
    PROTECTION("Proteção", "Equipamentos de proteção individual"),
    
    // Suplementos
    SUPPLEMENT("Suplemento", "Suplementos alimentares"),
    VITAMIN("Vitamina", "Vitaminas e minerais"),
    
    // Materiais
    MATERIAL("Material", "Materiais de consumo"),
    DISPOSABLE("Descartável", "Materiais descartáveis"),
    
    // Outros
    DIET("Diet", "Produtos dietéticos"),
    FIT("Fit", "Produtos fitoterápicos"),
    HOMEOPATHIC("Homeopático", "Produtos homeopáticos"),
    OTHER("Outro", "Outras categorias");
    
    private final String displayName;
    private final String description;
    
    ProductCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se a categoria é de medicamento.
     */
    public boolean isMedicine() {
        return this == MEDICINE || this == GENERIC || 
               this == SIMILAR || this == REFERENCED;
    }
    
    /**
     * Verifica se a categoria é controlada (exige receita).
     */
    public boolean isControlled() {
        return isMedicine() || this == HOMEOPATHIC;
    }
    
    /**
     * Verifica se a categoria é isenta de impostos.
     */
    public boolean isTaxExempt() {
        return isMedicine() || this == EQUIPMENT || this == PROTECTION;
    }
    
    /**
     * Retorna todas as categorias de medicamentos.
     */
    public static ProductCategory[] medicineCategories() {
        return new ProductCategory[]{MEDICINE, GENERIC, SIMILAR, REFERENCED};
    }
    
    /**
     * Retorna todas as categorias de cosméticos.
     */
    public static ProductCategory[] cosmeticCategories() {
        return new ProductCategory[]{COSMETIC, HYGIENE, PERFUME};
    }
    
    /**
     * Retorna todas as categorias de equipamentos.
     */
    public static ProductCategory[] equipmentCategories() {
        return new ProductCategory[]{EQUIPMENT, INSTRUMENT, PROTECTION};
    }
    
    /**
     * Converte de string, case-insensitive.
     */
    public static ProductCategory fromString(String value) {
        if (value == null) return null;
        
        try {
            return ProductCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Tenta encontrar por display name
            for (ProductCategory category : values()) {
                if (category.getDisplayName().equalsIgnoreCase(value)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("Categoria inválida: " + value);
        }
    }
}