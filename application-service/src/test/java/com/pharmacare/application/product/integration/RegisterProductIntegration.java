package com.pharmacare.application.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacare.application.product.commands.CreateProductCommand;
import com.pharmacare.application.product.handlers.RegisterProductUseCase;
import com.pharmacare.application.product.responses.ProductResponse;
import com.pharmacare.domain.core.products.enums.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração para RegisterProductUseCase.
 * 
 * Testa a integração completa com banco de dados real (test container).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegisterProductIntegrationTest {
    
    @Autowired
    private RegisterProductUseCase registerProductUseCase;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private CreateProductCommand validCommand;
    
    @BeforeEach
    void setUp() {
        validCommand = new CreateProductCommand(
            "Dipirona 500mg",
            "7896541230123",
            "Analgésico e antitérmico",
            "Genérico",
            ProductCategory.MEDICINE,
            new BigDecimal("8.50"),
            new BigDecimal("3.20"),
            10,
            "un",
            false,
            true,
            "3004.90.90",
            "28.011.00",
            "Laboratório Teste",
            "Distribuidora Teste",
            true,
            new BigDecimal("18.0"),
            new BigDecimal("1.65"),
            new BigDecimal("7.6"),
            Set.of("analgésico", "antitérmico"),
            "",
            false,
            "Tomar 1 comprimido a cada 6 horas",
            "Pode causar sonolência",
            "Dipirona Monoidratada 500mg",
            "BR987654321",
            "test.user@pharmacare.com"
        );
    }
    
    @Test
    void shouldRegisterProductAndPersistInDatabase() {
        // Act
        ProductResponse response = registerProductUseCase.execute(validCommand);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.productId()).isNotBlank();
        assertThat(response.name()).isEqualTo("Dipirona 500mg");
        assertThat(response.barcode()).isEqualTo("7896541230123");
        assertThat(response.brand()).isEqualTo("Genérico");
        assertThat(response.category()).isEqualTo(ProductCategory.MEDICINE);
        assertThat(response.price()).isEqualByComparingTo("8.50");
        assertThat(response.cost()).isEqualByComparingTo("3.20");
        assertThat(response.minimumStock()).isEqualTo(10);
        assertThat(response.controlled()).isFalse();
        assertThat(response.taxable()).isTrue();
        assertThat(response.requiresPrescription()).isFalse();
        assertThat(response.createdBy()).isEqualTo("test.user@pharmacare.com");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        
        // Verifica campos calculados
        assertThat(response.totalTax()).isEqualByComparingTo("27.25"); // 18 + 1.65 + 7.6
        assertThat(response.isBelowMinimumStock()).isTrue(); // currentStock = 0, minimumStock = 10
    }
    
    @Test
    void shouldThrowExceptionWhenRegisteringDuplicateBarcode() {
        // Arrange - Primeiro registro
        registerProductUseCase.execute(validCommand);
        
        // Act & Assert - Tentativa de registro com mesmo barcode
        assertThatThrownBy(() -> registerProductUseCase.execute(validCommand))
            .isInstanceOf(com.pharmacare.application.common.exception.BusinessException.class)
            .hasMessageContaining("Código de barras já cadastrado no sistema");
    }
    
    @Test
    void shouldHandleCommandWithAllOptionalFieldsNull() {
        // Arrange
        CreateProductCommand minimalCommand = new CreateProductCommand(
            "Produto Teste",
            "7891234567891",
            null, // descrição opcional
            "Marca Teste",
            ProductCategory.GENERIC,
            new BigDecimal("100.00"),
            null, // custo opcional
            5,
            "un",
            false,
            true,
            null, // NCM opcional
            null, // CEST opcional
            null, // fabricante opcional
            null, // fornecedor opcional
            true,
            new BigDecimal("18.0"),
            new BigDecimal("1.65"),
            new BigDecimal("7.6"),
            null, // tags opcional
            null, // imageUrl opcional
            false,
            null, // instruções opcional
            null, // efeitos colaterais opcional
            null, // composição opcional
            null, // registro ANVISA opcional
            "test.user@pharmacare.com"
        );
        
        // Act
        ProductResponse response = registerProductUseCase.execute(minimalCommand);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.productId()).isNotBlank();
        assertThat(response.name()).isEqualTo("Produto Teste");
        assertThat(response.description()).isNull();
        assertThat(response.cost()).isNull();
    }
    
    @Test
    void shouldCalculateProfitMarginCorrectly() {
        // Arrange
        CreateProductCommand profitableProduct = new CreateProductCommand(
            "Produto Lucrativo",
            "7891234567892",
            "Produto com boa margem",
            "Marca Teste",
            ProductCategory.GENERIC,
            new BigDecimal("150.00"),
            new BigDecimal("50.00"),
            5,
            "un",
            false,
            true,
            null,
            null,
            null,
            null,
            true,
            new BigDecimal("18.0"),
            new BigDecimal("1.65"),
            new BigDecimal("7.6"),
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            "test.user@pharmacare.com"
        );
        
        // Act
        ProductResponse response = registerProductUseCase.execute(profitableProduct);
        
        // Assert
        assertThat(response.calculateProfitMargin())
            .isEqualByComparingTo("200.0000"); // (150-50)/50 * 100 = 200%
    }
    
    @Test
    void shouldCalculatePriceWithTaxesCorrectly() {
        // Act
        ProductResponse response = registerProductUseCase.execute(validCommand);
        
        // Assert
        // Preço: 8.50, Total tax: 27.25% = 0.2725
        // 8.50 * 1.2725 = 10.81625 ≈ 10.82
        assertThat(response.calculatePriceWithTaxes())
            .isEqualByComparingTo("10.82");
    }
    
    @Test
    void shouldSerializeAndDeserializeCommandCorrectly() throws Exception {
        // Arrange
        String jsonCommand = """
            {
                "name": "Produto JSON",
                "barcode": "7891234567893",
                "description": "Produto via JSON",
                "brand": "Marca JSON",
                "category": "MEDICINE",
                "price": 99.99,
                "cost": 50.00,
                "minimumStock": 5,
                "unit": "un",
                "controlled": false,
                "taxable": true,
                "ncmCode": "3004.90.90",
                "cestCode": "28.011.00",
                "manufacturer": "Fabricante JSON",
                "supplier": "Fornecedor JSON",
                "active": true,
                "icmsTax": 18.0,
                "pisTax": 1.65,
                "cofinsTax": 7.6,
                "tags": ["teste", "json"],
                "imageUrl": "",
                "requiresPrescription": false,
                "instructions": "",
                "sideEffects": "",
                "composition": "",
                "anvisaRegistration": "",
                "createdBy": "json.test@pharmacare.com"
            }
            """;
        
        // Act - Deserialização
        CreateProductCommand command = objectMapper.readValue(jsonCommand, CreateProductCommand.class);
        
        // Assert
        assertThat(command.name()).isEqualTo("Produto JSON");
        assertThat(command.barcode()).isEqualTo("7891234567893");
        assertThat(command.price()).isEqualByComparingTo("99.99");
        assertThat(command.tags()).containsExactly("teste", "json");
        
        // Act - Execução
        ProductResponse response = registerProductUseCase.execute(command);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Produto JSON");
        
        // Act - Serialização
        String jsonResponse = objectMapper.writeValueAsString(response);
        
        // Assert
        assertThat(jsonResponse).contains("Produto JSON");
        assertThat(jsonResponse).contains("99.99");
    }
    
    @Test
    void shouldHandleControlledMedicineWithAnvisaRegistration() {
        // Arrange
        CreateProductCommand controlledMedicine = new CreateProductCommand(
            "Diazepam 10mg",
            "7891234567894",
            "Ansiolítico e relaxante muscular",
            "Laboratório Controlado",
            ProductCategory.MEDICINE,
            new BigDecimal("45.00"),
            new BigDecimal("15.00"),
            2,
            "un",
            true, // Controlado
            true,
            "3004.90.90",
            "28.011.00",
            "Lab Controlado S.A.",
            "Distribuidora Controlada",
            true,
            new BigDecimal("18.0"),
            new BigDecimal("1.65"),
            new BigDecimal("7.6"),
            Set.of("controlado", "tarja preta"),
            "",
            true, // Necessita receita
            "Tomar conforme prescrição médica",
            "Pode causar dependência",
            "Diazepam 10mg",
            "BR-CONTROL-12345", // Registro ANVISA obrigatório
            "medico@pharmacare.com"
        );
        
        // Act
        ProductResponse response = registerProductUseCase.execute(controlledMedicine);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.controlled()).isTrue();
        assertThat(response.requiresPrescription()).isTrue();
        assertThat(response.anvisaRegistration()).isEqualTo("BR-CONTROL-12345");
        assertThat(response.isControlledMedicine()).isTrue();
    }
    
    @Test
    void shouldThrowExceptionForControlledMedicineWithoutAnvisaRegistration() {
        // Arrange
        CreateProductCommand invalidControlledMedicine = new CreateProductCommand(
            "Medicamento Controlado",
            "7891234567895",
            "Descrição",
            "Marca",
            ProductCategory.MEDICINE,
            new BigDecimal("50.00"),
            new BigDecimal("20.00"),
            5,
            "un",
            true, // Controlado
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
            true,
            "Instruções",
            "Efeitos",
            "Composição",
            "", // SEM registro ANVISA (obrigatório para controlados)
            "test@pharmacare.com"
        );
        
        // Act & Assert
        assertThatThrownBy(() -> registerProductUseCase.execute(invalidControlledMedicine))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Produtos controlados necessitam de registro ANVISA");
    }
}