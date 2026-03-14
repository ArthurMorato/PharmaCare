package com.pharmacare.application.product.handlers.impl;

import com.pharmacare.application.common.exception.BusinessException;
import com.pharmacare.application.common.exception.ValidationException;
import com.pharmacare.application.common.validation.CommandValidator;
import com.pharmacare.application.product.commands.CreateProductCommand;
import com.pharmacare.application.product.mappers.ProductMapper;
import com.pharmacare.application.product.responses.ProductResponse;
import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.products.enums.ProductCategory;
import com.pharmacare.domain.core.products.ports.ProductRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RegisterProductService.
 */
@ExtendWith(MockitoExtension.class)
class RegisterProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Spy
    private CommandValidator commandValidator;
    
    @InjectMocks
    private RegisterProductService registerProductService;
    
    private CreateProductCommand validCommand;
    private Product mockProduct;
    private ProductResponse mockResponse;
    
    @BeforeEach
    void setUp() {
        // Inicializa o validador com um Validator real
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        commandValidator = new CommandValidator(validator);
        MockitoAnnotations.openMocks(this);
        registerProductService = new RegisterProductService(
            productRepository, productMapper, commandValidator, eventPublisher);
        
        // Command válido para testes
        validCommand = new CreateProductCommand(
            "Paracetamol 500mg",
            "7891234567890",
            "Analgésico e antitérmico",
            "Genérico",
            ProductCategory.MEDICINE,
            new BigDecimal("12.50"),
            new BigDecimal("5.00"),
            5,
            "un",
            false,
            true,
            "3004.90.90",
            "28.011.00",
            "Laboratório XYZ",
            "Distribuidora ABC",
            true,
            new BigDecimal("18.0"),
            new BigDecimal("1.65"),
            new BigDecimal("7.6"),
            Set.of("analgésico", "antitérmico"),
            "http://imagem.com/paracetamol.jpg",
            false,
            "Tomar 1 comprimido a cada 6 horas",
            "Pode causar sonolência",
            "Paracetamol 500mg",
            "BR123456789",
            "usuario@email.com"
        );
        
        // Mock do Product
        mockProduct = mock(Product.class);
        when(mockProduct.getId()).thenReturn("PROD-12345");
        when(mockProduct.isActive()).thenReturn(true);
        when(mockProduct.isTaxable()).thenReturn(true);
        
        // Mock do Response
        mockResponse = new ProductResponse(
            "PROD-12345",
            "Paracetamol 500mg",
            "7891234567890",
            "Analgésico e antitérmico",
            "Genérico",
            ProductCategory.MEDICINE,
            "Medicamento",
            new BigDecimal("12.50"),
            new BigDecimal("5.00"),
            0,
            5,
            "un",
            false,
            true,
            "3004.90.90",
            "28.011.00",
            "Laboratório XYZ",
            "Distribuidora ABC",
            com.pharmacare.domain.core.products.enums.ProductStatus.ACTIVE,
            "Ativo",
            new BigDecimal("18.0"),
            new BigDecimal("1.65"),
            new BigDecimal("7.6"),
            new BigDecimal("27.25"),
            Set.of("analgésico", "antitérmico"),
            "http://imagem.com/paracetamol.jpg",
            false,
            "Tomar 1 comprimido a cada 6 horas",
            "Pode causar sonolência",
            "Paracetamol 500mg",
            "BR123456789",
            java.time.LocalDateTime.now(),
            java.time.LocalDateTime.now(),
            "usuario@email.com",
            "usuario@email.com"
        );
    }
    
    @Test
    void shouldRegisterProductSuccessfully() {
        // Arrange
        when(productRepository.existsByBarcode(validCommand.barcode())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(mockResponse);
        
        // Act
        ProductResponse response = registerProductService.execute(validCommand);
        
        // Assert
        assertNotNull(response);
        assertEquals("PROD-12345", response.productId());
        assertEquals("Paracetamol 500mg", response.name());
        assertEquals("7891234567890", response.barcode());
        
        verify(productRepository).existsByBarcode(validCommand.barcode());
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toResponse(any(Product.class));
        verify(eventPublisher, atLeastOnce()).publishEvent(any());
    }
    
    @Test
    void shouldThrowBusinessExceptionWhenBarcodeExists() {
        // Arrange
        when(productRepository.existsByBarcode(validCommand.barcode())).thenReturn(true);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registerProductService.execute(validCommand);
        });
        
        assertEquals("BARCODE_ALREADY_REGISTERED", exception.getErrorCode());
        assertEquals("Código de barras já cadastrado no sistema", exception.getMessage());
        
        verify(productRepository).existsByBarcode(validCommand.barcode());
        verify(productRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowValidationExceptionWhenNameIsBlank() {
        // Arrange
        CreateProductCommand invalidCommand = new CreateProductCommand(
            "", // Nome vazio
            validCommand.barcode(),
            validCommand.description(),
            validCommand.brand(),
            validCommand.category(),
            validCommand.price(),
            validCommand.cost(),
            validCommand.minimumStock(),
            validCommand.unit(),
            validCommand.controlled(),
            validCommand.taxable(),
            validCommand.ncmCode(),
            validCommand.cestCode(),
            validCommand.manufacturer(),
            validCommand.supplier(),
            validCommand.active(),
            validCommand.icmsTax(),
            validCommand.pisTax(),
            validCommand.cofinsTax(),
            validCommand.tags(),
            validCommand.imageUrl(),
            validCommand.requiresPrescription(),
            validCommand.instructions(),
            validCommand.sideEffects(),
            validCommand.composition(),
            validCommand.anvisaRegistration(),
            validCommand.createdBy()
        );
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registerProductService.execute(invalidCommand);
        });
        
        assertTrue(exception.hasErrors());
        verify(productRepository, never()).existsByBarcode(any());
        verify(productRepository, never()).save(any());
    }
    
    @Test
    void shouldThrowValidationExceptionWhenBarcodeInvalid() {
        // Arrange
        CreateProductCommand invalidCommand = new CreateProductCommand(
            validCommand.name(),
            "123", // Barcode muito curto
            validCommand.description(),
            validCommand.brand(),
            validCommand.category(),
            validCommand.price(),
            validCommand.cost(),
            validCommand.minimumStock(),
            validCommand.unit(),
            validCommand.controlled(),
            validCommand.taxable(),
            validCommand.ncmCode(),
            validCommand.cestCode(),
            validCommand.manufacturer(),
            validCommand.supplier(),
            validCommand.active(),
            validCommand.icmsTax(),
            validCommand.pisTax(),
            validCommand.cofinsTax(),
            validCommand.tags(),
            validCommand.imageUrl(),
            validCommand.requiresPrescription(),
            validCommand.instructions(),
            validCommand.sideEffects(),
            validCommand.composition(),
            validCommand.anvisaRegistration(),
            validCommand.createdBy()
        );
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registerProductService.execute(invalidCommand);
        });
        
        assertTrue(exception.hasErrors());
    }
    
    @Test
    void shouldThrowValidationExceptionWhenPriceIsZero() {
        // Arrange
        CreateProductCommand invalidCommand = new CreateProductCommand(
            validCommand.name(),
            validCommand.barcode(),
            validCommand.description(),
            validCommand.brand(),
            validCommand.category(),
            BigDecimal.ZERO, // Preço zero
            validCommand.cost(),
            validCommand.minimumStock(),
            validCommand.unit(),
            validCommand.controlled(),
            validCommand.taxable(),
            validCommand.ncmCode(),
            validCommand.cestCode(),
            validCommand.manufacturer(),
            validCommand.supplier(),
            validCommand.active(),
            validCommand.icmsTax(),
            validCommand.pisTax(),
            validCommand.cofinsTax(),
            validCommand.tags(),
            validCommand.imageUrl(),
            validCommand.requiresPrescription(),
            validCommand.instructions(),
            validCommand.sideEffects(),
            validCommand.composition(),
            validCommand.anvisaRegistration(),
            validCommand.createdBy()
        );
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registerProductService.execute(invalidCommand);
        });
        
        assertTrue(exception.hasErrors());
    }
    
    @Test
    void shouldThrowIllegalArgumentExceptionWhenCostGreaterThanPrice() {
        // Arrange
        CreateProductCommand invalidCommand = new CreateProductCommand(
            validCommand.name(),
            validCommand.barcode(),
            validCommand.description(),
            validCommand.brand(),
            validCommand.category(),
            new BigDecimal("10.00"), // Preço
            new BigDecimal("15.00"), // Custo maior que preço
            validCommand.minimumStock(),
            validCommand.unit(),
            validCommand.controlled(),
            validCommand.taxable(),
            validCommand.ncmCode(),
            validCommand.cestCode(),
            validCommand.manufacturer(),
            validCommand.supplier(),
            validCommand.active(),
            validCommand.icmsTax(),
            validCommand.pisTax(),
            validCommand.cofinsTax(),
            validCommand.tags(),
            validCommand.imageUrl(),
            validCommand.requiresPrescription(),
            validCommand.instructions(),
            validCommand.sideEffects(),
            validCommand.composition(),
            validCommand.anvisaRegistration(),
            validCommand.createdBy()
        );
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            registerProductService.execute(invalidCommand);
        });
        
        assertEquals("Custo não pode ser maior que o preço de venda", exception.getMessage());
    }
    
    @Test
    void shouldHandleRepositoryExceptionGracefully() {
        // Arrange
        when(productRepository.existsByBarcode(validCommand.barcode())).thenReturn(false);
        when(productRepository.save(any(Product.class)))
            .thenThrow(new RuntimeException("Erro de conexão com o banco"));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registerProductService.execute(validCommand);
        });
        
        assertEquals("OPERATION_NOT_ALLOWED", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Erro ao registrar produto"));
        
        verify(productRepository).existsByBarcode(validCommand.barcode());
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    void shouldExecuteAsyncAndReturnProductId() {
        // Arrange
        when(productRepository.existsByBarcode(validCommand.barcode())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(mockResponse);
        
        // Act
        String productId = registerProductService.executeAsync(validCommand);
        
        // Assert
        assertEquals("PROD-12345", productId);
    }
    
    @Test
    void shouldRegisterBatchSuccessfully() {
        // Arrange
        var commands = java.util.List.of(validCommand, validCommand);
        when(productRepository.existsByBarcode(validCommand.barcode())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(mockResponse);
        
        // Act
        int successCount = registerProductService.registerBatch(commands, "batch-user@email.com");
        
        // Assert
        assertEquals(2, successCount);
        verify(productRepository, times(2)).existsByBarcode(validCommand.barcode());
        verify(productRepository, times(2)).save(any(Product.class));
    }
    
    @Test
    void shouldHandlePartialFailureInBatchRegistration() {
        // Arrange
        var validCommand2 = new CreateProductCommand(
            "Ibuprofeno 400mg",
            "7899876543210",
            "Anti-inflamatório",
            "Genérico",
            ProductCategory.MEDICINE,
            new BigDecimal("15.00"),
            new BigDecimal("6.00"),
            5,
            "un",
            false,
            true,
            "",
            "",
            "",
            "",
            true,
            new BigDecimal("18.0"),
            new BigDecimal("1.65"),
            new BigDecimal("7.6"),
            Set.of(),
            "",
            false,
            "Tomar 1 comprimido a cada 8 horas",
            "",
            "",
            "",
            "usuario@email.com"
        );
        
        var commands = java.util.List.of(validCommand, validCommand2);
        
        // Primeiro comando funciona, segundo falha (barcode duplicado)
        when(productRepository.existsByBarcode(validCommand.barcode())).thenReturn(false);
        when(productRepository.existsByBarcode(validCommand2.barcode())).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(mockResponse);
        
        // Act
        int successCount = registerProductService.registerBatch(commands, "batch-user@email.com");
        
        // Assert
        assertEquals(1, successCount); // Apenas um deve ter sucesso
        verify(productRepository, times(2)).existsByBarcode(anyString());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}