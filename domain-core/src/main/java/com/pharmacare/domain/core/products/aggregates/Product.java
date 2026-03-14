package com.pharmacare.domain.core.products.aggregates;

import com.pharmacare.domain.core.common.ValueObject;
import com.pharmacare.domain.core.products.enums.ProductCategory;
import com.pharmacare.domain.core.products.enums.ProductStatus;
import com.pharmacare.domain.core.products.events.*;
import com.pharmacare.domain.core.products.exceptions.ProductDomainException;
import com.pharmacare.domain.core.products.valueobjects.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate Root para entidade Product.
 * Responsável por garantir a consistência de todo o agregado.
 * 
 * Princípios DDD:
 * - Encapsulamento total (sem setters públicos)
 * - Invariantes mantidas em todos os métodos
 * - Eventos de domínio para comunicação
 * - Validações de negócio centralizadas
 */
public class Product extends ValueObject {
    
    // Identificação
    private final ProductId id;
    private final ProductName name;
    private final Barcode barcode;
    private final Description description;
    private final Brand brand;
    
    // Classificação
    private final ProductCategory category;
    private ProductStatus status;
    
    // Financeiro
    private Money price;
    
    // Metadados
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Version para concorrência
    private final int version;
    
    // Eventos de domínio (não persistidos)
    private final List<DomainEvent> domainEvents;
    
    /**
     * Construtor privado para uso do Builder.
     */
    private Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.barcode = builder.barcode;
        this.description = builder.description;
        this.brand = builder.brand;
        this.category = builder.category;
        this.status = builder.status;
        this.price = builder.price;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.version = builder.version;
        this.domainEvents = new ArrayList<>();
        
        // Valida invariantes
        validateInvariants();
        
        // Adiciona evento de domínio se for um novo produto
        if (builder.isNew) {
            registerDomainEvent(ProductRegisteredEvent.from(this));
        }
    }
    
    /**
     * Factory method para registro de novo produto.
     */
    public static Product register(
            ProductName name,
            Barcode barcode,
            Description description,
            Brand brand,
            ProductCategory category,
            Money price) {
        
        return new Builder()
            .id(ProductId.generate())
            .name(name)
            .barcode(barcode)
            .description(description)
            .brand(brand)
            .category(category)
            .status(ProductStatus.ACTIVE)
            .price(price)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .version(1)
            .isNew(true)
            .build();
    }
    
    /**
     * Factory method para reconstruir produto existente.
     * Usado pelo repositório ao carregar do banco.
     */
    public static Product reconstitute(
            ProductId id,
            ProductName name,
            Barcode barcode,
            Description description,
            Brand brand,
            ProductCategory category,
            ProductStatus status,
            Money price,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int version) {
        
        return new Builder()
            .id(id)
            .name(name)
            .barcode(barcode)
            .description(description)
            .brand(brand)
            .category(category)
            .status(status)
            .price(price)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .version(version)
            .isNew(false)
            .build();
    }
    
    /**
     * Atualiza informações básicas do produto.
     */
    public Product update(
            ProductName newName,
            Description newDescription,
            Brand newBrand,
            ProductCategory newCategory) {
        
        // Valida se pode ser atualizado
        validateCanBeUpdated();
        
        Map<String, Object> changes = new HashMap<>();
        
        // Verifica mudanças
        if (!this.name.equals(newName)) {
            changes.put("name", newName.getValue());
        }
        if (!this.description.equals(newDescription)) {
            changes.put("description", newDescription.getValue());
        }
        if (!this.brand.equals(newBrand)) {
            changes.put("brand", newBrand.getValue());
        }
        if (this.category != newCategory) {
            validateCategoryChange(newCategory);
            changes.put("category", newCategory);
        }
        
        // Se não houver mudanças, retorna o mesmo produto
        if (changes.isEmpty()) {
            return this;
        }
        
        // Cria novo produto com as alterações
        Product updatedProduct = new Builder()
            .id(this.id)
            .name(newName)
            .barcode(this.barcode)
            .description(newDescription)
            .brand(newBrand)
            .category(newCategory)
            .status(this.status)
            .price(this.price)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .isNew(false)
            .build();
        
        // Adiciona evento de atualização
        updatedProduct.registerDomainEvent(ProductUpdatedEvent.from(updatedProduct, changes));
        
        return updatedProduct;
    }
    
    /**
     * Marca produto como descontinuado.
     */
    public Product markAsDiscontinued(String reason) {
        validateCanBeDiscontinued();
        
        Product discontinuedProduct = new Builder()
            .id(this.id)
            .name(this.name)
            .barcode(this.barcode)
            .description(this.description)
            .brand(this.brand)
            .category(this.category)
            .status(ProductStatus.DISCONTINUED)
            .price(this.price)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .isNew(false)
            .build();
        
        // Adiciona evento de descontinuação
        discontinuedProduct.registerDomainEvent(
            ProductDiscontinuedEvent.from(discontinuedProduct, reason)
        );
        
        return discontinuedProduct;
    }
    
    /**
     * Altera o preço do produto.
     */
    public Product changePrice(Money newPrice) {
        validatePriceChange(newPrice);
        
        Product productWithNewPrice = new Builder()
            .id(this.id)
            .name(this.name)
            .barcode(this.barcode)
            .description(this.description)
            .brand(this.brand)
            .category(this.category)
            .status(this.status)
            .price(newPrice)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .isNew(false)
            .build();
        
        // Adiciona evento de atualização específico para preço
        Map<String, Object> changes = new HashMap<>();
        changes.put("price", newPrice.getFormattedValue());
        changes.put("oldPrice", this.price.getFormattedValue());
        productWithNewPrice.registerDomainEvent(
            ProductUpdatedEvent.from(productWithNewPrice, changes)
        );
        
        return productWithNewPrice;
    }
    
    /**
     * Ativa o produto.
     */
    public Product activate() {
        if (this.status.isAvailableForSale()) {
            return this; // Já está ativo
        }
        
        validateStatusTransition(ProductStatus.ACTIVE);
        
        Product activatedProduct = new Builder()
            .id(this.id)
            .name(this.name)
            .barcode(this.barcode)
            .description(this.description)
            .brand(this.brand)
            .category(this.category)
            .status(ProductStatus.ACTIVE)
            .price(this.price)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .isNew(false)
            .build();
        
        return activatedProduct;
    }
    
    /**
     * Inativa o produto.
     */
    public Product deactivate() {
        if (this.status == ProductStatus.INACTIVE) {
            return this; // Já está inativo
        }
        
        validateStatusTransition(ProductStatus.INACTIVE);
        
        Product deactivatedProduct = new Builder()
            .id(this.id)
            .name(this.name)
            .barcode(this.barcode)
            .description(this.description)
            .brand(this.brand)
            .category(this.category)
            .status(ProductStatus.INACTIVE)
            .price(this.price)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .version(this.version + 1)
            .isNew(false)
            .build();
        
        return deactivatedProduct;
    }
    
    /**
     * Valida invariantes do agregado.
     */
    private void validateInvariants() {
        // Validação de negócio: nome não pode ser vazio
        if (name == null) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRODUCT_STATE,
                "Nome do produto é obrigatório",
                "name"
            );
        }
        
        // Validação: barcode é obrigatório
        if (barcode == null) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRODUCT_STATE,
                "Código de barras é obrigatório",
                "barcode"
            );
        }
        
        // Validação: preço não pode ser negativo
        if (price == null || price.isZero()) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRODUCT_STATE,
                "Preço do produto é obrigatório e deve ser maior que zero",
                "price"
            );
        }
        
        // Validação: categoria é obrigatória
        if (category == null) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRODUCT_STATE,
                "Categoria do produto é obrigatória",
                "category"
            );
        }
        
        // Validação: status é obrigatório
        if (status == null) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRODUCT_STATE,
                "Status do produto é obrigatório",
                "status"
            );
        }
    }
    
    /**
     * Valida se o produto pode ser atualizado.
     */
    private void validateCanBeUpdated() {
        if (this.status.isFinal()) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.PRODUCT_DISCONTINUED,
                "Produto descontinuado ou bloqueado não pode ser atualizado",
                "status"
            );
        }
    }
    
    /**
     * Valida se o produto pode ser descontinuado.
     */
    private void validateCanBeDiscontinued() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.PRODUCT_DISCONTINUED,
                "Produto já está descontinuado",
                "status"
            );
        }
        
        if (this.status == ProductStatus.BLOCKED) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRODUCT_STATE,
                "Produto bloqueado não pode ser descontinuado",
                "status"
            );
        }
    }
    
    /**
     * Valida mudança de categoria.
     */
    private void validateCategoryChange(ProductCategory newCategory) {
        // Produtos controlados não podem mudar para não-controlados sem validação
        if (this.category.isControlled() && !newCategory.isControlled()) {
            // Poderia adicionar lógica específica aqui
            // Por exemplo, exigir aprovação de farmacêutico
        }
    }
    
    /**
     * Valida mudança de preço.
     */
    private void validatePriceChange(Money newPrice) {
        validateCanBeUpdated();
        
        // Preço não pode ser negativo
        if (newPrice == null || newPrice.isZero()) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRICE_CHANGE,
                "Novo preço não pode ser zero ou negativo",
                "price"
            );
        }
        
        // Limite para aumento de preço (não mais que 50% de uma vez)
        BigDecimal maxIncrease = this.price.getAmount()
            .multiply(new BigDecimal("1.5"));
        
        if (newPrice.getAmount().compareTo(maxIncrease) > 0) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRICE_CHANGE,
                "Aumento de preço não pode exceder 50% de uma vez",
                "price"
            );
        }
        
        // Para produtos controlados, mudanças de preço podem exigir aprovação
        if (this.category.isControlled()) {
            // Lógica específica para produtos controlados
        }
    }
    
    /**
     * Valida transição de status.
     */
    private void validateStatusTransition(ProductStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new ProductDomainException(
                ProductDomainException.ProductErrorCode.INVALID_PRODUCT_STATE,
                String.format(
                    "Transição de status inválida: %s -> %s",
                    this.status, newStatus
                ),
                "status"
            );
        }
    }
    
    /**
     * Registra evento de domínio.
     */
    private void registerDomainEvent(DomainEvent event) {
        if (domainEvents != null) {
            domainEvents.add(event);
        }
    }
    
    /**
     * Limpa eventos de domínio após serem processados.
     */
    public void clearDomainEvents() {
        if (domainEvents != null) {
            domainEvents.clear();
        }
    }
    
    // Getters (sem setters)
    public ProductId getId() {
        return id;
    }
    
    public ProductName getName() {
        return name;
    }
    
    public Barcode getBarcode() {
        return barcode;
    }
    
    public Description getDescription() {
        return description;
    }
    
    public Brand getBrand() {
        return brand;
    }
    
    public ProductCategory getCategory() {
        return category;
    }
    
    public ProductStatus getStatus() {
        return status;
    }
    
    public Money getPrice() {
        return price;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public int getVersion() {
        return version;
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * Métodos de negócio auxiliares.
     */
    public boolean isAvailableForSale() {
        return status.isAvailableForSale();
    }
    
    public boolean isControlled() {
        return category.isControlled();
    }
    
    public boolean isTaxExempt() {
        return category.isTaxExempt();
    }
    
    public boolean isDiscontinued() {
        return status == ProductStatus.DISCONTINUED;
    }
    
    /**
     * Retorna os componentes para igualdade.
     */
    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{id};
    }
    
    /**
     * Builder para Product.
     */
    public static class Builder {
        private ProductId id;
        private ProductName name;
        private Barcode barcode;
        private Description description;
        private Brand brand;
        private ProductCategory category;
        private ProductStatus status;
        private Money price;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int version;
        private boolean isNew;
        
        public Builder id(ProductId id) {
            this.id = id;
            return this;
        }
        
        public Builder name(ProductName name) {
            this.name = name;
            return this;
        }
        
        public Builder barcode(Barcode barcode) {
            this.barcode = barcode;
            return this;
        }
        
        public Builder description(Description description) {
            this.description = description;
            return this;
        }
        
        public Builder brand(Brand brand) {
            this.brand = brand;
            return this;
        }
        
        public Builder category(ProductCategory category) {
            this.category = category;
            return this;
        }
        
        public Builder status(ProductStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder price(Money price) {
            this.price = price;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder version(int version) {
            this.version = version;
            return this;
        }
        
        public Builder isNew(boolean isNew) {
            this.isNew = isNew;
            return this;
        }
        
        public Product build() {
            return new Product(this);
        }
    }
    
    /**
     * Representação em string.
     */
    @Override
    public String toString() {
        return String.format(
            "Product{id=%s, name=%s, barcode=%s, category=%s, status=%s, price=%s}",
            id.getStringValue(),
            name.getValue(),
            barcode.getValue(),
            category,
            status,
            price.getFormattedValue()
        );
    }
}