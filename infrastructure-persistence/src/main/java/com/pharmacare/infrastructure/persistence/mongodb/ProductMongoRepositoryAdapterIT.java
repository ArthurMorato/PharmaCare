package com.pharmacare.infrastructure.persistence.mongodb;

import com.pharmacare.domain.core.model.*;
import com.pharmacare.infrastructure.persistence.mongodb.adapter.ProductMongoRepositoryAdapter;
import com.pharmacare.infrastructure.persistence.mongodb.document.ProductDocument;
import com.pharmacare.infrastructure.persistence.mongodb.repository.ProductMongoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para ProductMongoRepositoryAdapter.
 * Usa Testcontainers para levantar um MongoDB real.
 */
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductMongoRepositoryAdapterIT {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(
        DockerImageName.parse("mongo:6.0")
    );
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @Autowired
    private ProductMongoRepositoryAdapter repositoryAdapter;
    
    @Autowired
    private ProductMongoRepository mongoRepository;
    
    private Product testProduct;
    private ProductId testProductId;
    
    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        mongoRepository.deleteAll();
        
        // Criar produto de teste
        testProduct = Product.builder()
            .name(ProductName.of("Paracetamol 500mg"))
            .barcode("7891234567890")
            .description("Analgésico e antitérmico")
            .category(Category.MEDICINE)
            .price(Money.of(new BigDecimal("12.50"), "BRL"))
            .minimumStock(50)
            .manufacturer("EMS")
            .activePrinciple("Paracetamol")
            .concentration("500mg")
            .unit("Comprimido")
            .requiresPrescription(false)
            .build();
        
        testProduct = repositoryAdapter.save(testProduct);
        testProductId = testProduct.getId();
    }
    
    @Test
    @Order(1)
    @DisplayName("Deve salvar produto com sucesso")
    void shouldSaveProductSuccessfully() {
        // Arrange
        Product newProduct = Product.builder()
            .name(ProductName.of("Ibuprofeno 400mg"))
            .barcode("7899876543210")
            .description("Anti-inflamatório")
            .category(Category.MEDICINE)
            .price(Money.of(new BigDecimal("18.90"), "BRL"))
            .minimumStock(30)
            .requiresPrescription(false)
            .build();
        
        // Act
        Product savedProduct = repositoryAdapter.save(newProduct);
        
        // Assert
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName().getValue()).isEqualTo("Ibuprofeno 400mg");
        assertThat(savedProduct.getBarcode().getValue()).isEqualTo("7899876543210");
        
        // Verifica persistência
        Optional<Product> retrieved = repositoryAdapter.findById(savedProduct.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName().getValue()).isEqualTo("Ibuprofeno 400mg");
    }
    
    @Test
    @Order(2)
    @DisplayName("Deve buscar produto por ID")
    void shouldFindProductById() {
        // Act
        Optional<Product> found = repositoryAdapter.findById(testProductId);
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testProductId);
        assertThat(found.get().getName().getValue()).isEqualTo("Paracetamol 500mg");
    }
    
    @Test
    @Order(3)
    @DisplayName("Deve buscar produto por código de barras")
    void shouldFindProductByBarcode() {
        // Act
        Optional<Product> found = repositoryAdapter.findByBarcode("7891234567890");
        
        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getBarcode().getValue()).isEqualTo("7891234567890");
        assertThat(found.get().getId()).isEqualTo(testProductId);
    }
    
    @Test
    @Order(4)
    @DisplayName("Deve verificar existência por código de barras")
    void shouldCheckExistenceByBarcode() {
        // Act & Assert
        assertThat(repositoryAdapter.existsByBarcode("7891234567890")).isTrue();
        assertThat(repositoryAdapter.existsByBarcode("9999999999999")).isFalse();
    }
    
    @Test
    @Order(5)
    @DisplayName("Deve buscar produtos ativos com paginação")
    void shouldFindActiveProductsWithPagination() {
        // Arrange
        // Criar mais produtos para testar paginação
        for (int i = 1; i <= 15; i++) {
            Product product = Product.builder()
                .name(ProductName.of("Produto Teste " + i))
                .barcode("789000000000" + i)
                .description("Descrição " + i)
                .category(Category.MEDICINE)
                .price(Money.of(new BigDecimal("10.00"), "BRL"))
                .requiresPrescription(false)
                .build();
            repositoryAdapter.save(product);
        }
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // Act
        var page = repositoryAdapter.findActiveProducts(pageable);
        
        // Assert
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(16); // 15 novos + 1 inicial
        assertThat(page.getContent()).hasSize(10); // Page size
        assertThat(page.getContent()).allMatch(p -> 
            p.getStatus() == ProductStatus.ACTIVE
        );
    }
    
    @Test
    @Order(6)
    @DisplayName("Deve atualizar produto existente")
    void shouldUpdateExistingProduct() {
        // Arrange
        Product toUpdate = repositoryAdapter.findById(testProductId).orElseThrow();
        
        // Act
        Product updated = toUpdate.toBuilder()
            .name(ProductName.of("Paracetamol 500mg - Nova Formulação"))
            .price(Money.of(new BigDecimal("13.75"), "BRL"))
            .build();
        
        Product saved = repositoryAdapter.save(updated);
        
        // Assert
        assertThat(saved.getId()).isEqualTo(testProductId);
        assertThat(saved.getName().getValue()).isEqualTo("Paracetamol 500mg - Nova Formulação");
        assertThat(saved.getPrice().getAmount()).isEqualByComparingTo("13.75");
        
        // Verifica no banco
        Optional<Product> retrieved = repositoryAdapter.findById(testProductId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName().getValue())
            .isEqualTo("Paracetamol 500mg - Nova Formulação");
    }
    
    @Test
    @Order(7)
    @DisplayName("Deve excluir produto por ID")
    void shouldDeleteProductById() {
        // Act
        repositoryAdapter.deleteById(testProductId);
        
        // Assert
        Optional<Product> found = repositoryAdapter.findById(testProductId);
        assertThat(found).isEmpty();
        
        // Verifica no repositório MongoDB
        Optional<ProductDocument> document = mongoRepository.findById(testProductId.getValue());
        assertThat(document).isEmpty();
    }
    
    @Test
    @Order(8)
    @DisplayName("Deve lançar exceção ao buscar produto inexistente por ID")
    void shouldThrowExceptionWhenProductNotFoundById() {
        // Arrange
        ProductId nonExistentId = ProductId.from("non-existent-id");
        
        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
            com.pharmacare.sharedkernel.exception.EntityNotFoundException.class,
            () -> repositoryAdapter.findByIdOrThrow(nonExistentId)
        );
    }
    
    @Test
    @Order(9)
    @DisplayName("Deve manter consistência de dados após operações")
    void shouldMaintainDataConsistency() {
        // Arrange
        String barcode = "7891112223334";
        
        Product product1 = Product.builder()
            .name(ProductName.of("Produto A"))
            .barcode(barcode)
            .category(Category.MEDICINE)
            .price(Money.of(new BigDecimal("20.00"), "BRL"))
            .build();
        
        // Act & Assert - Não deve permitir barcode duplicado
        repositoryAdapter.save(product1);
        
        Product product2 = Product.builder()
            .name(ProductName.of("Produto B"))
            .barcode(barcode) // Mesmo barcode
            .category(Category.MEDICINE)
            .price(Money.of(new BigDecimal("25.00"), "BRL"))
            .build();
        
        // Tentar salvar deve falhar por constraint única
        Assertions.assertThrows(Exception.class, () -> {
            repositoryAdapter.save(product2);
        });
        
        // Verificar que apenas um produto com o barcode existe
        long count = mongoRepository.findAll().stream()
            .filter(doc -> barcode.equals(doc.getBarcode()))
            .count();
        assertThat(count).isEqualTo(1);
    }
}