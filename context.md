# **Bíblia do Projeto - Sistema de Gerenciamento de Filiais Farmacêuticas**

## **1. VISÃO GERAL DO SISTEMA**

### **1.1. Propósito e Escopo**
Sistema de gerenciamento de rede de farmácias com **estoque compartilhado centralizado**, permitindo:
- Controle unificado de produtos em todas as filiais
- Dashboard em tempo real de estoque e movimentações
- Gestão de transferências entre filiais
- Controle de validade e lote de medicamentos
- Múltiplos níveis de acesso (farmacêutico, gerente, administrador)

### **1.2. Princípios Fundamentais**
1. **Single Source of Truth**: Estoque central como única fonte de verdade
2. **Eventual Consistency**: Dados distribuídos com consistência eventual
3. **Domain-Driven Design**: Modelagem centrada no domínio farmacêutico
4. **Clean Architecture**: Separação rigorosa de responsabilidades
5. **Test-First**: Desenvolvimento guiado por testes

## **2. DOMÍNIO DE NEGÓCIO (CORE)**

### **2.1. Contextos Delimitados (Bounded Contexts)**
```
1. Inventory Context        → Gestão de estoque e produtos
2. Branch Context          → Gestão de filiais e colaboradores
3. Transfer Context        → Transferências entre filiais
4. Notification Context    → Alertas e notificações
5. Reporting Context       → Dashboard e relatórios
```

### **2.2. Entidades de Domínio (Aggregates)**

#### **Product (Aggregate Root)**
```java
// Exemplo de estrutura conceitual
Product {
    - id: ProductId (UUID)
    - name: ProductName
    - description: ProductDescription
    - category: ProductCategory (MEDICINE, COSMETIC, EQUIPMENT)
    - requiresPrescription: boolean
    - barcode: Barcode
    - manufacturer: Manufacturer
    - activeIngredients: List<ActiveIngredient>
    - dosage: Dosage
    - unitOfMeasure: UnitOfMeasure
}
```

#### **InventoryItem (Aggregate Root)**
```java
InventoryItem {
    - id: InventoryItemId
    - productId: ProductId
    - branchId: BranchId
    - batchNumber: BatchNumber
    - expirationDate: LocalDate
    - quantity: Quantity (Value Object)
    - minimumStockLevel: Quantity
    - maximumStockLevel: Quantity
    - lastUpdated: Timestamp
}
```

#### **Branch (Aggregate Root)**
```java
Branch {
    - id: BranchId
    - code: BranchCode (único)
    - name: BranchName
    - address: Address (Value Object)
    - phone: Phone
    - email: Email
    - managerId: EmployeeId
    - openingHours: OpeningHours
    - status: BranchStatus (ACTIVE, INACTIVE, MAINTENANCE)
}
```

### **2.3. Eventos de Domínio**
```java
// Eventos publicados pelo domínio
public interface DomainEvent {
    String aggregateId();
    LocalDateTime occurredOn();
}

// Exemplos:
class ProductRegistered implements DomainEvent
class StockLevelChanged implements DomainEvent
class TransferRequested implements DomainEvent
class ExpirationAlertTriggered implements DomainEvent
class LowStockAlertTriggered implements DomainEvent
```

## **3. ARQUITETURA HEXAGONAL**

### **3.1. Estrutura de Camadas**
```
┌─────────────────────────────────────────────────┐
│                 ADAPTERS (INFRASTRUCTURE)       │
├─────────────────────────────────────────────────┤
│   REST Controllers │ Message Consumers │ Web UI │
├─────────────────────────────────────────────────┤
│                 PORTS (APPLICATION)             │
├─────────────────────────────────────────────────┤
│   Use Cases │ Services │ DTOs │ Mappers         │
├─────────────────────────────────────────────────┤
│                 DOMAIN (CORE)                   │
├─────────────────────────────────────────────────┤
│   Entities │ Value Objects │ Domain Services    │
│   Repositories │ Domain Events │ Specifications │
└─────────────────────────────────────────────────┘
```

### **3.2. Portas (Ports)**
```java
// Portas Primárias (Driving Ports)
public interface ProductServicePort {
    Product registerProduct(RegisterProductCommand command);
    Product updateProduct(UpdateProductCommand command);
    Product findProductById(ProductId id);
}

public interface InventoryServicePort {
    InventoryItem addStock(AddStockCommand command);
    InventoryItem removeStock(RemoveStockCommand command);
    List<InventoryItem> checkStockLevels(CheckStockQuery query);
}

// Portas Secundárias (Driven Ports)
public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    List<Product> findByCriteria(ProductSpecification spec);
}

public interface InventoryRepositoryPort {
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> findByProductAndBranch(ProductId, BranchId);
    List<InventoryItem> findExpiringSoon(LocalDate threshold);
}
```

### **3.3. Adaptadores (Adapters)**

#### **Primary Adapters (Entrada)**
- `ProductController` (REST)
- `InventoryController` (REST)
- `TransferController` (REST)
- `WebSocketController` (WebSocket para dashboard)

#### **Secondary Adapters (Saída)**
- `ProductRepositoryAdapter` (Spring Data MongoDB)
- `InventoryRepositoryAdapter` (Spring Data MongoDB)
- `EventPublisherAdapter` (Spring Cloud Stream/Kafka)
- `EmailNotificationAdapter` (JavaMailSender)
- `SmsNotificationAdapter` (Twilio/WhatsApp)
- `ReportGeneratorAdapter` (JasperReports/PDF)

## **4. SPEC-DRIVEN DEVELOPMENT**

### **4.1. Especificações Técnicas**

#### **4.1.1. API REST Specifications (OpenAPI 3.0)**
```yaml
openapi: 3.0.0
info:
  title: Pharmacy Management System API
  version: 1.0.0
  description: API para gerenciamento de rede de farmácias

paths:
  /api/v1/products:
    post:
      summary: Registrar novo produto
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterProductRequest'
      responses:
        '201':
          description: Produto registrado com sucesso
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProductResponse'
```

#### **4.1.2. Mensageria (Event-Driven Architecture)**
```java
@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic stockEventsTopic() {
        return TopicBuilder.name("stock-events")
                .partitions(3)
                .replicas(2)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 dias
                .build();
    }
}
```

### **4.2. Especificações de Domínio (Specification Pattern)**
```java
public interface Specification<T> {
    boolean isSatisfiedBy(T entity);
    Specification<T> and(Specification<T> other);
    Specification<T> or(Specification<T> other);
}

// Exemplo:
public class ProductInStockSpecification implements Specification<Product> {
    private final BranchId branchId;
    private final int minimumQuantity;
    
    @Override
    public boolean isSatisfiedBy(Product product) {
        return inventoryRepository
            .findByProductAndBranch(product.getId(), branchId)
            .map(item -> item.getQuantity().getValue() >= minimumQuantity)
            .orElse(false);
    }
}
```

## **5. TEST-DRIVEN DEVELOPMENT**

### **5.1. Estratégia de Testes**
```
📁 src/test/java
├── 📁 unit/           → Testes unitários (isolados)
├── 📁 integration/    → Testes de integração
├── 📁 acceptance/     → Testes de aceitação (BDD)
└── 📁 contract/       → Testes de contrato (Consumer-Driven)
```

### **5.2. Pirâmide de Testes**
```java
// 1. Testes Unitários (70%)
@Test
void should_register_product_with_valid_data() {
    // GIVEN
    RegisterProductCommand command = new RegisterProductCommand(
        "Paracetamol 500mg",
        "Analgésico e antitérmico",
        "MEDICINE",
        true
    );
    
    // WHEN
    Product product = productService.registerProduct(command);
    
    // THEN
    assertThat(product).isNotNull();
    assertThat(product.getName()).isEqualTo("Paracetamol 500mg");
}

// 2. Testes de Integração (20%)
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {
    @Test
    void should_return_201_when_creating_valid_product() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }
}

// 3. Testes End-to-End (10%)
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {
}

Feature: Gestão de Estoque
  Scenario: Adicionar produto ao estoque
    Given o produto "Paracetamol 500mg" está cadastrado
    And a filial "Farmácia Central" está ativa
    When adiciono 100 unidades ao estoque
    Then o sistema deve mostrar 100 unidades disponíveis
```

### **5.3. Test Containers para Testes de Integração**
```java
@Testcontainers
@DataMongoTest
@Import(TestConfig.class)
class InventoryRepositoryTest {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @Test
    void should_persist_inventory_item() {
        // Teste com MongoDB real em container
    }
}
```

## **6. ESTRUTURA DO PROJETO**

```
pharmacy-management-system/
├── 📁 .github/workflows/          → CI/CD pipelines
├── 📁 config/                     → Configurações
├── 📁 docs/                       → Documentação
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/pharmacy/
│   │   │   ├── 📁 application/    → Casos de uso e serviços de aplicação
│   │   │   │   ├── 📁 ports/      → Interfaces (Ports)
│   │   │   │   ├── 📁 services/   → Serviços de aplicação
│   │   │   │   ├── 📁 dtos/       → Data Transfer Objects
│   │   │   │   └── 📁 mappers/    → Mapeadores
│   │   │   ├── 📁 domain/         → Domínio de negócio
│   │   │   │   ├── 📁 models/     → Entidades e Aggregates
│   │   │   │   ├── 📁 valueobjects/ → Value Objects
│   │   │   │   ├── 📁 services/   → Serviços de domínio
│   │   │   │   ├── 📁 repositories/ → Interfaces de repositório
│   │   │   │   ├── 📁 events/     → Eventos de domínio
│   │   │   │   └── 📁 specs/      → Specifications
│   │   │   ├── 📁 infrastructure/ → Implementações concretas
│   │   │   │   ├── 📁 adapters/   → Adapters (Primários e Secundários)
│   │   │   │   ├── 📁 persistence/ → Repositórios JPA/MongoDB
│   │   │   │   ├── 📁 messaging/  → Kafka/RabbitMQ
│   │   │   │   ├── 📁 web/        → Controllers REST
│   │   │   │   └── 📁 config/     → Configurações Spring
│   │   │   └── PharmacyApplication.java
│   │   └── 📁 resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── 📁 db/migration/   → Flyway migrations
│   └── 📁 test/
│       ├── 📁 unit/
│       ├── 📁 integration/
│       ├── 📁 acceptance/
│       └── 📁 contract/
├── 📁 docker/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── 📁 monitoring/             → Prometheus, Grafana
├── pom.xml                        → Maven dependencies
└── README.md
```

## **7. TECNOLOGIAS E VERSÕES**

### **7.1. Stack Principal**
- **Java**: 17 LTS (Amazon Corretto)
- **Spring Boot**: 3.2.x
- **Spring Cloud**: 2023.0.x
- **Build Tool**: Maven/Gradle

### **7.2. Persistência**
- **MongoDB**: 6.0+ (para dados não relacionais)
- **PostgreSQL**: 15+ (para dados transacionais)
- **Redis**: 7.0+ (cache e sessões)
- **Elasticsearch**: 8.x (para busca e analytics)

### **7.3. Mensageria e Eventos**
- **Apache Kafka**: 3.5+ com Schema Registry
- **Spring Cloud Stream**
- **Avro** para serialização

### **7.4. Observabilidade**
- **Micrometer** + **Prometheus** (métricas)
- **OpenTelemetry** (tracing distribuído)
- **Grafana** (dashboards)
- **ELK Stack** (logs)

### **7.5. Segurança**
- **Spring Security 6** + **JWT**
- **Keycloak** (SSO e Identity Management)
- **Vault** (gerenciamento de segredos)

## **8. DESIGN PATTERNS E BOAS PRÁTICAS**

### **8.1. Padrões de Projeto**
- **Factory** para criação complexa de Aggregates
- **Specification** para queries complexas
- **CQRS** para separação de leitura/escrita
- **Saga Pattern** para transações distribuídas
- **Circuit Breaker** para resiliência

### **8.2. Clean Code**
```java
// RUIM
public void updateStock(Long pId, Long bId, Integer q) {
    // ...
}

// BOM
public InventoryItem adjustStockLevel(AdjustStockCommand command) {
    validateCommand(command);
    InventoryItem item = findInventoryItem(command);
    item.adjustQuantity(command.getAdjustment());
    publishStockAdjustedEvent(item);
    return inventoryRepository.save(item);
}
```

### **8.3. Princípios SOLID**
1. **Single Responsibility**: Cada classe tem uma única responsabilidade
2. **Open/Closed**: Aberto para extensão, fechado para modificação
3. **Liskov Substitution**: Subtipos substituíveis por seus tipos base
4. **Interface Segregation**: Interfaces pequenas e específicas
5. **Dependency Inversion**: Depender de abstrações, não implementações

## **9. PIPELINE DE CI/CD**

### **9.1. Workflow GitHub Actions**
```yaml
name: CI/CD Pipeline
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      mongodb:
        image: mongo:6.0
      kafka:
        image: confluentinc/cp-kafka:latest
    steps:
      - name: Run Unit Tests
        run: mvn test
        
      - name: Run Integration Tests
        run: mvn verify -Pintegration
        
      - name: SonarQube Analysis
        run: mvn sonar:sonar
        
  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - name: Build Docker Image
        run: docker build -t pharmacy-system:latest .
        
  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to Kubernetes
        run: kubectl apply -f k8s/
```

## **10. MONITORAMENTO E HEALTH CHECKS**

### **10.1. Endpoints de Saúde**
```java
@RestController
@RequestMapping("/actuator")
public class HealthController {
    
    @GetMapping("/health")
    public Health health() {
        return Health.up()
            .withDetail("database", checkDatabase())
            .withDetail("kafka", checkKafka())
            .withDetail("cache", checkRedis())
            .build();
    }
    
    @GetMapping("/metrics")
    @Timed(value = "pharmacy.metrics", description = "Métricas do sistema")
    public MetricsResponse getMetrics() {
        // Custom metrics
    }
}
```

### **10.2. Dashboard Grafana**
- Taxa de sucesso das transações
- Tempo médio de resposta
- Utilização de recursos
- Alertas de estoque baixo
- Tendências de vendas

## **11. POLÍTICAS DE DEPLOYMENT**

### **11.1. Versionamento Semântico**
```
MAJOR.MINOR.PATCH
1.0.0 → Release inicial
1.1.0 → Novas features compatíveis
1.1.1 → Bug fixes
```

### **11.2. Estratégia de Deployment**
- **Blue-Green Deployment** para zero downtime
- **Feature Flags** para release gradual
- **Database Migrations** com Flyway
- **Rollback Automático** em caso de falha

## **12. DOCUMENTAÇÃO**

### **12.1. Documentação Técnica**
- **Architecture Decision Records (ADRs)**
- **API Documentation** (Swagger/OpenAPI)
- **Domain Glossary** (ubiquitous language)
- **Deployment Guide**
- **Troubleshooting Guide**

### **12.2. Exemplo de ADR**
```
# ADR 001: Escolha do MongoDB para dados de estoque

## Status
Aceito

## Contexto
Necessidade de alta performance em operações de estoque com dados semi-estruturados

## Decisão
Usar MongoDB como banco principal para dados de inventário

## Consequências
✅ Alta performance em operações de leitura/escrita
✅ Schema flexível para produtos variados
✅ Suporte nativo a agregações complexas
❄️  Eventual consistency (planejado)
🛠  Necessidade de monitoramento de índices
```

## **13. GOVERNANÇA DE CÓDIGO**

### **13.1. Code Review Checklist**
- [ ] Segue princípios de DDD?
- [ ] Testes unitários adequados?
- [ ] Cobertura mínima de 80%?
- [ ] Tratamento de exceções apropriado?
- [ ] Logging adequado?
- [ ] Documentação atualizada?
- [ ] Performance considerada?

### **13.2. Métricas de Qualidade**
- **Complexidade Ciclomática**: < 10 por método
- **Cobertura de Testes**: > 80%
- **Débito Técnico**: Monitorado via SonarQube
- **Code Smells**: Zero críticos
- **Security Vulnerabilities**: Zero

- ---

## **MANIFESTO DO PROJETO**

> "Este projeto é mais do que código - é uma representação digital do domínio farmacêutico. Cada linha de código deve refletir o conhecimento do domínio, cada teste deve validar regras de negócio, cada decisão arquitetural deve servir ao propósito do sistema."

### **Regras de Ouro**
1. **O domínio é o rei** - Tudo começa e termina no domínio de negócio
2. **Testes são documentação executável** - Escreva testes que documentem o comportamento
3. **Arquitetura é sobre decisões** - Documente todas as decisões arquiteturais (ADRs)
4. **Simplicidade acima de sofisticação** - Resista à sobregeneralização
5. **Feedback contínuo** - Use métricas para guiar melhorias

### **Próximos Passos Imediatos**
1. [ ] Configurar projeto Spring Boot com estrutura hexagonal
2. [ ] Implementar primeiro Aggregate (Product) com TDD
3. [ ] Configurar MongoDB com repositórios adaptados
4. [ ] Criar pipeline CI/CD básico
5. [ ] Documentar primeiras ADRs

---

**Este documento é vivo** - deve evoluir com o projeto. Revisar e atualizar a cada sprint, garantindo que permaneça a fonte única de verdade arquitetural.

*"Primeiro, faça funcionar. Então, faça certo. Por fim, faça rápido." - Kent Beck*


  
