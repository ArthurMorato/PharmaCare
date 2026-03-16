package com.pharmacare.domain.core.products.enums;

/**
 * Status do produto no sistema.
 * Controla o ciclo de vida do produto.
 */
public enum ProductStatus {
    DRAFT("Rascunho", "Produto em fase de cadastro"),
    ACTIVE("Ativo", "Produto disponível para venda"),
    INACTIVE("Inativo", "Produto temporariamente indisponível"),
    DISCONTINUED("Descontinuado", "Produto não será mais comercializado"),
    BLOCKED("Bloqueado", "Produto bloqueado por questões regulatórias"),
    PENDING_APPROVAL("Aprovação Pendente", "Aguardando aprovação regulatória");
    
    private final String displayName;
    private final String description;
    
    ProductStatus(String displayName, String description) {
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
     * Verifica se o produto está disponível para venda.
     */
    public boolean isAvailableForSale() {
        return this == ACTIVE;
    }
    
    /**
     * Verifica se o produto pode ser editado.
     */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING_APPROVAL;
    }
    
    /**
     * Verifica se o produto está finalizado.
     */
    public boolean isFinal() {
        return this == DISCONTINUED || this == BLOCKED;
    }
    
    /**
     * Transições permitidas de status.
     */
    public boolean canTransitionTo(ProductStatus newStatus) {
        switch (this) {
            case DRAFT:
                return newStatus == PENDING_APPROVAL || newStatus == ACTIVE;
            case PENDING_APPROVAL:
                return newStatus == ACTIVE || newStatus == BLOCKED;
            case ACTIVE:
                return newStatus == INACTIVE || newStatus == DISCONTINUED || 
                       newStatus == BLOCKED;
            case INACTIVE:
                return newStatus == ACTIVE || newStatus == DISCONTINUED;
            case DISCONTINUED:
            case BLOCKED:
                return false; // Estados finais
            default:
                return false;
        }
    }
    
    /**
     * Converte de string, case-insensitive.
     */
    public static ProductStatus fromString(String value) {
        if (value == null) return null;
        
        try {
            return ProductStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Tenta encontrar por display name
            for (ProductStatus status : values()) {
                if (status.getDisplayName().equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Status inválido: " + value);
        }
    }
}