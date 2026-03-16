package com.pharmacare.infrastructure.persistence.mongodb.adapter;

import com.pharmacare.domain.core.products.aggregates.Product;
import com.pharmacare.domain.core.products.valueobjects.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@Testcontainers
@DataMongoTest
@Import(ProductMongoRepositoryAdapter.class)
class ProductMongoRepositoryAdapterIT {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0")
            .withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
    }

    @Autowired
    private ProductMongoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        // Limpa todos os dados antes de cada teste
        adapter.findAll(PageRequest.of(0, 1000)).getContent()
                .forEach(product -> adapter.deleteById(product.getId()));
    }

    @Test
    void shouldSaveAndRetrieveProduct() {
        // Given
        Product product = Product.create(
                "PROD001",
                "Paracetamol",
                "Analgésico e antitérmico",
                "Medley",
                Category.MEDICINE,
                new BigDecimal("10.50"),
                100,
                "Fornecedor A"
        );

        // When
        Product saved = adapter.save(product);
        Optional<Product> found = adapter.findById(saved.getId());

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("PROD001");
        assertThat(saved.getName()).isEqualTo("Paracetamol");
        assertThat(saved.getCategory()).isEqualTo(Category.MEDICINE);
        assertThat(saved.getUnitPrice()).isEqualByComparingTo("10.50");
        assertThat(saved.getQuantityInStock()).isEqualTo(100);
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();

        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("PROD001");
    }

    @Test
    void shouldFindByCode() {
        // Given
        Product product = Product.create(
                "UNIQUE001",
                "Ibuprofeno",
                "Anti-inflamatório",
                "Genérico",
                Category.MEDICINE,
                new BigDecimal("15.00"),
                50,
                "Fornecedor B"
        );
        adapter.save(product);

        // When
        Optional<Product> found = adapter.findByCode("UNIQUE001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("UNIQUE001");
        assertThat(found.get().getName()).isEqualTo("Ibuprofeno");
    }

    @Test
    void shouldUpdateProductStock() {
        // Given
        Product product = Product.create(
                "PROD002",
                "Dipirona",
                "Analgésico",
                "EMS",
                Category.MEDICINE,
                new BigDecimal("5.00"),
                100,
                "Fornecedor C"
        );
        Product saved = adapter.save(product);

        // When - Update stock
        saved.updateStock(75);
        Product updated = adapter.save(saved);

        // Then
        assertThat(updated.getQuantityInStock()).isEqualTo(75);
        assertThat(updated.getUpdatedAt()).isAfter(saved.getCreatedAt());
    }

    @Test
    void shouldUpdateProductPrice() {
        // Given
        Product product = Product.create(
                "PROD003",
                "Omeprazol",
                "Inibidor de bomba de prótons",
                "Aché",
                Category.MEDICINE,
                new BigDecimal("25.90"),
                200,
                "Fornecedor D"
        );
        Product saved = adapter.save(product);

        // When - Update price
        saved.updatePrice(new BigDecimal("22.50"));
        Product updated = adapter.save(saved);

        // Then
        assertThat(updated.getUnitPrice()).isEqualByComparingTo("22.50");
        assertThat(updated.getUpdatedAt()).isAfter(saved.getCreatedAt());
    }

    @Test
    void shouldDeactivateAndActivateProduct() {
        // Given
        Product product = Product.create(
                "PROD004",
                "Produto Teste",
                "Descrição teste",
                "Marca Teste",
                Category.OTHER,
                new BigDecimal("100.00"),
                10,
                "Fornecedor Teste"
        );
        Product saved = adapter.save(product);

        // When - Deactivate
        saved.deactivate();
        Product deactivated = adapter.save(saved);

        // Then
        assertThat(deactivated.getActive()).isFalse();

        // When - Activate
        deactivated.activate();
        Product activated = adapter.save(deactivated);

        // Then
        assertThat(activated.getActive()).isTrue();
    }

    @Test
    void shouldFindAllProductsPaginated() {
        // Given - Criar 20 produtos
        for (int i = 1; i <= 20; i++) {
            Product product = Product.create(
                    String.format("PROD%03d", i),
                    String.format("Produto %d", i),
                    String.format("Descrição %d", i),
                    "Marca",
                    i % 2 == 0 ? Category.MEDICINE : Category.MEDICAL_SUPPLIES,
                    new BigDecimal(10 + i),
                    i * 5,
                    String.format("Fornecedor %d", i % 3)
            );
            adapter.save(product);
        }

        // When
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = adapter.findAll(pageable);

        // Then
        assertThat(page.getTotalElements()).isEqualTo(20);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void shouldFindProductsWithLowStock() {
        // Given
        List<Product> products = List.of(
                Product.create("LOW001", "Produto Baixo 1", "Descrição", "Marca",
                        Category.MEDICINE, new BigDecimal("10.00"), 5, "Fornecedor"),
                Product.create("LOW002", "Produto Baixo 2", "Descrição", "Marca",
                        Category.MEDICINE, new BigDecimal("20.00"), 3, "Fornecedor"),
                Product.create("NORMAL001", "Produto Normal", "Descrição", "Marca",
                        Category.MEDICINE, new BigDecimal("30.00"), 50, "Fornecedor")
        );

        products.forEach(adapter::save);

        // When
        List<Product> lowStockProducts = adapter.findProductsWithLowStock(10);

        // Then
        assertThat(lowStockProducts).hasSize(2);
        assertThat(lowStockProducts)
                .extracting(Product::getCode)
                .containsExactlyInAnyOrder("LOW001", "LOW002");
    }

    @Test
    void shouldSearchProductsByNameOrCode() {
        // Given
        List<Product> products = List.of(
                Product.create("ASP001", "Aspirina", "Analgésico", "Bayer",
                        Category.MEDICINE, new BigDecimal("8.50"), 100, "Bayer"),
                Product.create("PAR001", "Paracetamol", "Antitérmico", "Genérico",
                        Category.MEDICINE, new BigDecimal("5.50"), 80, "Genérico"),
                Product.create("IBU001", "Ibuprofeno", "Anti-inflamatório", "Medley",
                        Category.MEDICINE, new BigDecimal("12.00"), 30, "Medley")
        );

        products.forEach(adapter::save);

        // When - Search by name
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> resultsByName = adapter.searchProducts("paracetamol", pageable);

        // When - Search by code
        Page<Product> resultsByCode = adapter.searchProducts("ASP001", pageable);

        // Then
        assertThat(resultsByName.getTotalElements()).isEqualTo(1);
        assertThat(resultsByName.getContent().get(0).getName()).isEqualTo("Paracetamol");

        assertThat(resultsByCode.getTotalElements()).isEqualTo(1);
        assertThat(resultsByCode.getContent().get(0).getCode()).isEqualTo("ASP001");
    }

    @Test
    void shouldCheckLowStockStatus() {
        // Given
        Product lowStockProduct = Product.create(
                "LOW", "Produto Baixo", "Descrição", "Marca",
                Category.MEDICINE, new BigDecimal("10.00"), 5, "Fornecedor"
        );

        Product normalStockProduct = Product.create(
                "NORMAL", "Produto Normal", "Descrição", "Marca",
                Category.MEDICINE, new BigDecimal("10.00"), 50, "Fornecedor"
        );

        // When & Then
        assertThat(lowStockProduct.isLowStock()).isTrue();
        assertThat(normalStockProduct.isLowStock()).isFalse();
    }

    @Test
    void shouldCalculateTotalValue() {
        // Given
        Product product = Product.create(
                "VALUE001",
                "Produto para Calcular Valor",
                "Descrição",
                "Marca",
                Category.MEDICINE,
                new BigDecimal("25.50"),
                40,
                "Fornecedor"
        );

        // When
        BigDecimal totalValue = product.calculateTotalValue();

        // Then
        assertThat(totalValue).isEqualByComparingTo("1020.00"); // 25.50 * 40
    }

    @Test
    void shouldValidateProduct() {
        // Given
        Product validProduct = Product.create(
                "VALID001",
                "Produto Válido",
                "Descrição válida",
                "Marca Válida",
                Category.MEDICINE,
                new BigDecimal("100.00"),
                50,
                "Fornecedor Válido"
        );

        // When & Then - Should not throw exception
        assertThatCode(validProduct::validate).doesNotThrowAnyException();
    }
}
