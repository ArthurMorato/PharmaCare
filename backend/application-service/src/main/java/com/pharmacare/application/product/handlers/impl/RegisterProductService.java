package com.pharmacare.application.product.handlers.impl;

import com.pharmacare.application.common.exception.BusinessException;
import com.pharmacare.application.common.validation.CommandValidator;
import com.pharmacare.application.product.commands.CreateProductCommand;
import com.pharmacare.application.product.handlers.RegisterProductUseCase;
import com.pharmacare.application.product.mappers.ProductMapper;
import com.pharmacare.application.product.responses.ProductResponse;
import com.pharmacare.domain.core.inventory.valueobjects.Money;
import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.products.ports.ProductRepository;
import com.pharmacare.domain.core.products.valueobjects.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação do use case para registro de produto.
 * 
 * Responsabilidades:
 * 1. Validar command
 * 2. Verificar unicidade de barcode
 * 3. Criar aggregate
 * 4. Persistir aggregate
 * 5. Publicar eventos de domínio
 * 6. Retornar response
 */
@Slf4j
@Service
public class RegisterProductService implements RegisterProductUseCase {
    
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CommandValidator commandValidator;
    private final ApplicationEventPublisher eventPublisher;
    
    @Autowired
    public RegisterProductService(
            ProductRepository productRepository,
            ProductMapper productMapper,
            CommandValidator commandValidator,
            ApplicationEventPublisher eventPublisher) {
        
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.commandValidator = commandValidator;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Executa o registro de um novo produto de forma transacional.
     */
    @Override
    @Transactional
    public ProductResponse execute(CreateProductCommand command) {
        log.info("Iniciando registro de produto: {}", command.name());
        
        try {
            // 1. Validação do command
            validateCommand(command);
            
            // 2. Verificar unicidade do barcode
            validateBarcodeUniqueness(command.barcode());
            
            // 3. Mapear command para value objects
            ProductName productName = ProductName.from(command.name());
            Barcode barcode = Barcode.from(command.barcode());
            Description description = Description.from(command.description());
            Brand brand = Brand.from(command.brand());
            Money price = Money.of(command.price());
            Money cost = Money.of(command.cost());
            
            // 4. Criar o aggregate
            Product product = Product.register(
                productName,
                barcode,
                description,
                brand,
                command.category(),
                price,
                cost,
                command.minimumStock(),
                command.unit(),
                command.controlled(),
                command.taxable(),
                command.ncmCode(),
                command.cestCode(),
                command.manufacturer(),
                command.supplier(),
                command.icmsTax(),
                command.pisTax(),
                command.cofinsTax(),
                command.tags(),
                command.imageUrl(),
                command.requiresPrescription(),
                command.instructions(),
                command.sideEffects(),
                command.composition(),
                command.anvisaRegistration(),
                command.createdBy()
            );
            
            // 5. Persistir o produto
            Product savedProduct = productRepository.save(product);
            log.info("Produto persistido com ID: {}", savedProduct.getId());
            
            // 6. Publicar eventos de domínio
            publishDomainEvents(savedProduct);
            
            // 7. Mapear para response e retornar
            ProductResponse response = productMapper.toResponse(savedProduct);
            log.info("Produto registrado com sucesso: {}", response.productId());
            
            return response;
            
        } catch (BusinessException e) {
            log.error("Erro de negócio ao registrar produto: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao registrar produto: {}", e.getMessage(), e);
            throw new BusinessException(
                BusinessException.BusinessErrorCode.OPERATION_NOT_ALLOWED,
                "Erro ao registrar produto: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Valida o command usando Bean Validation e regras customizadas.
     */
    private void validateCommand(CreateProductCommand command) {
        log.debug("Validando command para produto: {}", command.name());
        commandValidator.validate(command);
    }
    
    /**
     * Verifica se o barcode já está cadastrado no sistema.
     */
    private void validateBarcodeUniqueness(String barcode) {
        log.debug("Verificando unicidade do barcode: {}", barcode);
        
        boolean exists = productRepository.existsByBarcode(barcode);
        if (exists) {
            log.warn("Tentativa de cadastro com barcode duplicado: {}", barcode);
            throw new BusinessException(
                BusinessException.BusinessErrorCode.BARCODE_ALREADY_REGISTERED,
                "Código de barras já cadastrado no sistema",
                "barcode"
            );
        }
    }
    
    /**
     * Publica eventos de domínio do aggregate.
     */
    private void publishDomainEvents(Product product) {
        log.debug("Publicando eventos de domínio para produto: {}", product.getId());
        
        product.getDomainEvents().forEach(event -> {
            try {
                eventPublisher.publishEvent(event);
                log.debug("Evento publicado: {} - {}", 
                    event.getEventType(), event.getEventId());
            } catch (Exception e) {
                log.error("Erro ao publicar evento: {}", event.getEventType(), e);
                // Não propaga o erro para não interromper o fluxo principal
                // Em produção, considerar Dead Letter Queue
            }
        });
        
        product.clearDomainEvents();
    }
    
    /**
     * Método para registro em lote (opcional).
     */
    @Transactional
    public int registerBatch(java.util.List<CreateProductCommand> commands, String createdBy) {
        log.info("Iniciando registro em lote de {} produtos", commands.size());
        
        int successCount = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        for (int i = 0; i < commands.size(); i++) {
            try {
                CreateProductCommand command = commands.get(i);
                
                // Sobrescreve o createdBy se necessário
                var updatedCommand = new CreateProductCommand(
                    command.name(),
                    command.barcode(),
                    command.description(),
                    command.brand(),
                    command.category(),
                    command.price(),
                    command.cost(),
                    command.minimumStock(),
                    command.unit(),
                    command.controlled(),
                    command.taxable(),
                    command.ncmCode(),
                    command.cestCode(),
                    command.manufacturer(),
                    command.supplier(),
                    command.active(),
                    command.icmsTax(),
                    command.pisTax(),
                    command.cofinsTax(),
                    command.tags(),
                    command.imageUrl(),
                    command.requiresPrescription(),
                    command.instructions(),
                    command.sideEffects(),
                    command.composition(),
                    command.anvisaRegistration(),
                    createdBy
                );
                
                execute(updatedCommand);
                successCount++;
                
            } catch (Exception e) {
                String errorMsg = String.format("Produto %d: %s", i + 1, e.getMessage());
                errors.add(errorMsg);
                log.error("Erro ao registrar produto em lote: {}", errorMsg);
            }
        }
        
        if (!errors.isEmpty()) {
            log.warn("Registro em lote concluído com {} erros: {}", 
                errors.size(), String.join("; ", errors));
        }
        
        log.info("Registro em lote concluído: {}/{} produtos registrados com sucesso",
            successCount, commands.size());
        
        return successCount;
    }
}