package com.pharmacare.infrastructure.adapter.input.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacare.application.service.product.ProductService;
import com.pharmacare.application.service.product.dto.ProductResult;
import com.pharmacare.infrastructure.adapter.input.rest.assembler.ProductAssembler;
import com.pharmacare.infrastructure.adapter.input.rest.controller.v1.ProductControllerV1;
import com.pharmacare.infrastructure.adapter.input.rest.dto.request.CreateProductRequest;
import com.pharmacare.infrastructure.adapter.input.rest.dto.request.UpdateProductRequest;
import com.pharmacare.sharedkernel.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários do ProductController usando MockMvc.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductControllerV1.class)
class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ProductService productService;
    
    @MockBean
    private ProductAssembler productAssembler;
    
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;
    private ProductResult productResult;
    private String productId;
    
    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID().toString();
        
        createRequest = CreateProductRequest.builder()
            .name("Paracetamol 500mg")
            .barcode("7891234567890")
            .description("Analgésico e antitérmico")
            .category("MEDICINE")
            .price(new BigDecimal("12.50"))
            .minimumStock(50)
            .manufacturer("EMS")
            .activePrinciple("Paracetamol")
            .concentration("500mg")
            .unit("Comprimido")
            .requiresPrescription(false)
            .taxable(true)
            .icmsTaxRate(new BigDecimal("18.0"))
            .build();
        
        updateRequest = UpdateProductRequest.builder()
            .name("Paracetamol 500mg - Nova Formulação")
            .description("Analgésico e antitérmico - Nova embalagem")
            .price(new BigDecimal("13.75"))
            .minimumStock(60)
            .manufacturer("EMS")
            .status("ACTIVE")
            .requiresPrescription(false)
            .build();
        
        productResult = ProductResult.builder()
            .id(productId)
            .name("Paracetamol 500mg")
            .barcode("7891234567890")
            .description("Analgésico e antitérmico")
            .category("MEDICINE")
            .price(new BigDecimal("12.50"))
            .currency("BRL")
            .minimumStock(50)
            .currentStock(150)
            .manufacturer("EMS")
            .activePrinciple("Paracetamol")
            .concentration("500mg")
            .unit("Comprimido")
            .status("ACTIVE")
            .requiresPrescription(false)
            .taxable(true)
            .icmsTaxRate(new BigDecimal("18.0"))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .version(1L)
            .build();
    }
    
    @Test
    @DisplayName("POST /api/v1/products - Deve criar produto com sucesso")
    @WithMockUser(roles = {"PHARMACIST"})
    void shouldCreateProductSuccessfully() throws Exception {
        // Arrange
        when(productService.createProduct(any()))
            .thenReturn(productResult);
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Paracetamol 500mg"))
            .andExpect(jsonPath("$.barcode").value("7891234567890"));
        
        verify(productService, times(1)).createProduct(any());
    }
    
    @Test
    @DisplayName("POST /api/v1/products - Deve falhar sem autenticação")
    void shouldFailCreateProductWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isUnauthorized());
        
        verify(productService, never()).createProduct(any());
    }
    
    @Test
    @DisplayName("POST /api/v1/products - Deve falhar com dados inválidos")
    @WithMockUser(roles = {"PHARMACIST"})
    void shouldFailCreateProductWithInvalidData() throws Exception {
        // Arrange
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
            .name("")  // Nome vazio
            .barcode("123")  // Barcode inválido
            .price(BigDecimal.ZERO)  // Preço zero
            .build();
        
        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[0].field").exists());
        
        verify(productService, never()).createProduct(any());
    }
    
    @Test
    @DisplayName("GET /api/v1/products/{id} - Deve buscar produto por ID")
    @WithMockUser(roles = {"CASHIER"})
    void shouldGetProductByIdSuccessfully() throws Exception {
        // Arrange
        when(productService.getProductById(productId))
            .thenReturn(productResult);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Paracetamol 500mg"));
        
        verify(productService, times(1)).getProductById(productId);
    }
    
    @Test
    @DisplayName("GET /api/v1/products/{id} - Deve retornar 404 para produto não encontrado")
    @WithMockUser(roles = {"CASHIER"})
    void shouldReturn404WhenProductNotFound() throws Exception {
        // Arrange
        String nonExistentId = "non-existent-id";
        when(productService.getProductById(nonExistentId))
            .thenThrow(new EntityNotFoundException("Produto não encontrado"));
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"))
            .andExpect(jsonPath("$.message").exists());
        
        verify(productService, times(1)).getProductById(nonExistentId);
    }
    
    @Test
    @DisplayName("PUT /api/v1/products/{id} - Deve atualizar produto com sucesso")
    @WithMockUser(roles = {"PHARMACIST"})
    void shouldUpdateProductSuccessfully() throws Exception {
        // Arrange
        ProductResult updatedResult = productResult.toBuilder()
            .name("Paracetamol 500mg - Nova Formulação")
            .price(new BigDecimal("13.75"))
            .build();
        
        when(productService.updateProduct(any()))
            .thenReturn(updatedResult);
        
        // Act & Assert
        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Paracetamol 500mg - Nova Formulação"))
            .andExpect(jsonPath("$.price").value(13.75));
        
        verify(productService, times(1)).updateProduct(any());
    }
    
    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Deve excluir produto com sucesso")
    @WithMockUser(roles = {"MANAGER"})
    void shouldDeleteProductSuccessfully() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(productId);
        
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(csrf()))
            .andExpect(status().isNoContent());
        
        verify(productService, times(1)).deleteProduct(productId);
    }
    
    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Deve falhar sem permissão de gerente")
    @WithMockUser(roles = {"PHARMACIST"})
    void shouldFailDeleteProductWithoutManagerRole() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .with(csrf()))
            .andExpect(status().isForbidden());
        
        verify(productService, never()).deleteProduct(any());
    }
    
    @Test
    @DisplayName("GET /api/v1/products - Deve buscar produtos com filtros")
    @WithMockUser(roles = {"CASHIER"})
    void shouldSearchProductsWithFilters() throws Exception {
        // Arrange
        Page<ProductResult> page = new PageImpl<>(
            List.of(productResult),
            PageRequest.of(0, 20),
            1
        );
        
        when(productService.searchProducts(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .param("name", "paracetamol")
                .param("category", "MEDICINE")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].name").value("Paracetamol 500mg"))
            .andExpect(jsonPath("$.totalElements").value(1));
        
        verify(productService, times(1))
            .searchProducts(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("GET /api/v1/products/barcode/{barcode} - Deve buscar produto por código de barras")
    @WithMockUser(roles = {"CASHIER"})
    void shouldGetProductByBarcode() throws Exception {
        // Arrange
        when(productService.getProductByBarcode("7891234567890"))
            .thenReturn(productResult);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/barcode/{barcode}", "7891234567890"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.barcode").value("7891234567890"));
        
        verify(productService, times(1)).getProductByBarcode("7891234567890");
    }
    
    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Deve listar produtos por categoria")
    @WithMockUser(roles = {"CASHIER"})
    void shouldGetProductsByCategory() throws Exception {
        // Arrange
        Page<ProductResult> page = new PageImpl<>(
            List.of(productResult),
            PageRequest.of(0, 20),
            1
        );
        
        when(productService.getProductsByCategory(any(), any()))
            .thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", "MEDICINE")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.products").isArray())
            .andExpect(jsonPath("$._embedded.products[0].category").value("MEDICINE"));
        
        verify(productService, times(1)).getProductsByCategory(any(), any());
    }
}