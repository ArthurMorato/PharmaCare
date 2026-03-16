package com.pharmacare.infrastructure.adapter.input.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacare.domain.core.model.ProductStatus;
import com.pharmacare.infrastructure.adapter.input.rest.dto.request.CreateProductRequest;
import com.pharmacare.sharedkernel.test.TestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração completos do ProductController.
 * Usa banco de dados real (testcontainers) e testa o fluxo completo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@ContextConfiguration(classes = TestContainersConfig.class)
class ProductControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private CreateProductRequest createRequest;
    
    @BeforeEach
    void setUp() {
        createRequest = CreateProductRequest.builder()
            .name("Ibuprofeno 400mg")
            .barcode("7899876543210")
            .description("Anti-inflamatório não esteroidal")
            .category("MEDICINE")
            .price(new BigDecimal("18.90"))
            .minimumStock(30)
            .manufacturer("EMS")
            .activePrinciple("Ibuprofeno")
            .concentration("400mg")
            .unit("Comprimido")
            .requiresPrescription(false)
            .taxable(true)
            .icmsTaxRate(new BigDecimal("18.0"))
            .build();
    }
    
    @Test
    @DisplayName("Fluxo completo CRUD de produto")
    @WithMockUser(username = "farmacista", roles = {"PHARMACIST", "MANAGER"})
    void shouldCompleteProductCrudFlow() throws Exception {
        // 1. Criar produto
        String productId = createProductAndGetId();
        
        // 2. Buscar produto criado
        getProductById(productId);
        
        // 3. Atualizar produto
        updateProduct(productId);
        
        // 4. Buscar por código de barras
        getProductByBarcode();
        
        // 5. Buscar com filtros
        searchProducts();
        
        // 6. Excluir produto (lógico)
        deleteProduct(productId);
        
        // 7. Verificar que produto foi desativado
        verifyProductDeactivated(productId);
    }
    
    private String createProductAndGetId() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.name").value("Ibuprofeno 400mg"))
            .andExpect(jsonPath("$.barcode").value("7899876543210"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$._links.self").exists())
            .andExpect(jsonPath("$._links.update").exists())
            .andExpect(jsonPath("$._links.delete").exists());
        
        // Extrai o ID do produto criado
        String response = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }
    
    private void getProductById(String productId) throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", productId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Ibuprofeno 400mg"))
            .andExpect(jsonPath("$.barcode").value("7899876543210"))
            .andExpect(jsonPath("$.price").value(18.90))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$._links.self").exists());
    }
    
    private void updateProduct(String productId) throws Exception {
        var updateRequest = com.pharmacare.infrastructure.adapter.input.rest.dto.request.UpdateProductRequest.builder()
            .name("Ibuprofeno 400mg - Nova Apresentação")
            .description("Anti-inflamatório - Embalagem com 20 comprimidos")
            .price(new BigDecimal("21.50"))
            .minimumStock(40)
            .manufacturer("EMS")
            .status("ACTIVE")
            .requiresPrescription(false)
            .build();
        
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Ibuprofeno 400mg - Nova Apresentação"))
            .andExpect(jsonPath("$.price").value(21.50))
            .andExpect(jsonPath("$.minimumStock").value(40))
            .andExpect(jsonPath("$._links.self").exists());
    }
    
    private void getProductByBarcode() throws Exception {
        mockMvc.perform(get("/api/v1/products/barcode/{barcode}", "7899876543210"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.barcode").value("7899876543210"))
            .andExpect(jsonPath("$.name").value("Ibuprofeno 400mg - Nova Apresentação"));
    }
    
    private void searchProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("name", "ibuprofeno")
                .param("category", "MEDICINE")
                .param("page", "0")
                .param("size", "10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].name", containsString("Ibuprofeno")))
            .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))
            .andExpect(jsonPath("$._links.next").doesNotExist()); // Apenas uma página
    }
    
    private void deleteProduct(String productId) throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isNoContent());
    }
    
    private void verifyProductDeactivated(String productId) throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", productId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE")); // Exclusão lógica
    }
    
    @Test
    @DisplayName("Deve falhar ao criar produto com código de barras duplicado")
    @WithMockUser(roles = {"PHARMACIST"})
    void shouldFailWhenCreatingDuplicateBarcode() throws Exception {
        // Cria primeiro produto
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated());
        
        // Tenta criar segundo produto com mesmo barcode
        CreateProductRequest duplicateRequest = createRequest.toBuilder()
            .name("Produto Duplicado")
            .build();
        
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"))
            .andExpect(jsonPath("$.message", containsString("código de barras")));
    }
    
    @Test
    @DisplayName("Deve validar todos os campos obrigatórios")
    @WithMockUser(roles = {"PHARMACIST"})
    void shouldValidateAllRequiredFields() throws Exception {
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
            .name("")  // Nome vazio
            .barcode("123")  // Barcode inválido
            .category("INVALID_CATEGORY")  // Categoria inválida
            .price(BigDecimal.valueOf(-10))  // Preço negativo
            .icmsTaxRate(BigDecimal.valueOf(200))  // ICMS > 100%
            .build();
        
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[*].field", 
                hasItems("name", "barcode", "price", "icmsTaxRate")))
            .andExpect(jsonPath("$.validationErrors[*].message", 
                everyItem(not(emptyString()))));
    }
    
    @Test
    @DisplayName("Deve implementar HATEOAS corretamente")
    @WithMockUser(roles = {"CASHIER"})
    void shouldImplementHateoasCorrectly() throws Exception {
        // Cria produto
        String productId = createProductAndGetId();
        
        // Verifica links no recurso individual
        mockMvc.perform(get("/api/v1/products/{id}", productId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.self.href", containsString(productId)))
            .andExpect(jsonPath("$._links.update.href", containsString(productId)))
            .andExpect(jsonPath("$._links.delete.href", containsString(productId)))
            .andExpect(jsonPath("$._links.products.href", containsString("/api/v1/products")));
        
        // Verifica links na coleção
        mockMvc.perform(get("/api/v1/products")
                .param("size", "5"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.create.href", containsString("/api/v1/products")))
            .andExpect(jsonPath("$._links.search.href", containsString("/api/v1/products")));
    }
}