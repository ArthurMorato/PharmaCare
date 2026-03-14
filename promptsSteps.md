# **Guia de Prompts Adaptado para aichat (DeepSeek CLI) - PharmaCare ERP**

## **CONTEXTO INICIAL PARA TODAS AS INTERAÇÕES**

```markdown
📋 **CONTEXTO DO PROJETO PHARMACARE ERP**

Estamos desenvolvendo um sistema ERP para farmácias com:
- Arquitetura Hexagonal (Ports & Adapters)
- Backend: Java 17 + Spring Boot 3.2 + MongoDB + PostgreSQL
- Frontend: React 18 + TypeScript + Vite
- Desenvolvimento: TDD, Git Flow, CI/CD

Para cada solicitação, forneça:
1. TODOS os arquivos necessários para o módulo
2. Código limpo, comentado e seguro
3. Testes unitários e de integração
4. Instruções de integração com o projeto existente
5. Considerações de backward compatibility
```

---

## **FASE 1: CONFIGURAÇÃO INICIAL DO PROJETO**

### **1.1 Estrutura Maven Multi-módulo**
```prompt
Crie a estrutura completa do projeto Maven multi-módulo para o PharmaCare ERP.

Requisitos:
1. Parent POM com versões centralizadas
2. Módulos: domain-core, application-service, infrastructure-persistence, infrastructure-web, shared-kernel
3. Java 17, Spring Boot 3.2.5
4. Plugins: compiler, surefire, jacoco, spotbugs
5. Dependências: spring-boot-starter, validation, mongodb, postgresql, mapstruct, lombok

Saída esperada:
- pom.xml (parent)
- pom.xml para cada módulo
- Estrutura de diretórios recomendada
- Docker-compose.yml com serviços necessários
```

### **1.2 Docker Compose**
```prompt
Crie o arquivo docker-compose.yml completo para o ambiente de desenvolvimento PharmaCare ERP.

Serviços necessários:
- MongoDB 6.0 (porta 27017)
- PostgreSQL 15 (porta 5432)
- Redis 7 (porta 6379)
- Kafka + Zookeeper
- Keycloak 21 (porta 8081)
- Elasticsearch 8 + Kibana

Configurações:
- Networks separadas
- Volumes persistentes
- Health checks
- Variáveis de ambiente
- Dependências entre serviços
```

---

## **FASE 2: MÓDULO DE DOMÍNIO (CORE)**

### **2.1 Value Objects**
```prompt
Crie os Value Objects para o módulo de produtos do PharmaCare ERP:

1. ProductName: imutável, validação 3-100 chars, apenas letras/números/espaços
2. Barcode: validação formato EAN-13
3. Description: máximo 500 caracteres
4. ProductId: UUID baseado

Para cada VO, forneça:
- Classe Java (record) com validações
- Testes unitários completos
- Exceções customizadas (DomainException)
- Exemplos de uso
```

### **2.2 Aggregate Root - Product**
```prompt
Crie a entidade Product (Aggregate Root) seguindo DDD puro:

Atributos:
- ProductId (VO)
- ProductName (VO)
- Barcode (VO)
- Description (VO)
- Category (enum: MEDICINE, COSMETIC, EQUIPMENT)
- Status (enum: ACTIVE, INACTIVE, DISCONTINUED)
- Price (Money VO)
- CreatedAt, UpdatedAt

Comportamentos:
- register(): factory method estático
- update(): valida e atualiza
- markAsDiscontinued(): marca como descontinuado
- changePrice(): atualiza preço com validação

Regras:
- Nenhum setter público
- Invariantes: nome único, barcode válido
- Eventos de domínio: ProductRegistered, ProductUpdated, ProductDiscontinued

Forneça:
1. Entidade Product completa
2. Eventos de domínio
3. Factory methods
4. Testes unitários (100% cobertura)
5. Specification pattern para consultas
```

### **2.3 Aggregate Root - InventoryItem**
```prompt
Crie o Aggregate InventoryItem para gestão de estoque:

Identidade composta: ProductId + BranchId
Value Objects:
- Quantity (não negativo, validações)
- BatchNumber (formato específico)
- ExpirationDate (futura)

Métodos:
- adjustStock(quantity): ajusta quantidade
- reserve(quantity): reserva para venda
- expire(): marca como expirado
- transferTo(branch, quantity): transfere entre filiais

Regras:
- Não pode ajustar estoque expirado
- Quantidade mínima: 5 unidades
- Evento: StockLevelChanged

Forneça:
1. Entidade completa com invariantes
2. Value Objects relacionados
3. Testes unitários (casos de borda)
4. Exceções de negócio
```

---

## **FASE 3: CAMADA DE APLICAÇÃO**

### **3.1 Use Case - RegisterProduct**
```prompt
Crie o RegisterProductUseCase completo:

Interface:
```java
public interface RegisterProductUseCase {
    ProductResponse execute(CreateProductCommand command);
}
```

Componentes necessários:
1. CreateProductCommand (record com validações)
2. ProductResponse (record apenas leitura)
3. Implementação com @Transactional
4. ProductMapper (MapStruct)
5. Validação de unicidade de barcode
6. Publicação de evento ProductRegistered
7. Tratamento de exceções (BusinessException, ValidationException)

Forneça todos os arquivos:
- Use case interface e implementação
- DTOs (Command, Response)
- Mapper configuration
- Testes unitários e de integração
```

### **3.2 Service - InventoryService**
```prompt
Crie o InventoryService com os seguintes métodos:

1. addStock(AddStockCommand): adiciona ao estoque existente ou cria novo
2. removeStock(RemoveStockCommand): valida estoque suficiente antes de remover
3. getStockLevel(ProductId, BranchId): retorna quantidade disponível
4. getLowStockAlerts(): retorna produtos com estoque < mínimo
5. transferStock(TransferStockCommand): transfere entre filiais com saga pattern

Regras:
- Sempre validar existência de produto e filial
- Publicar eventos de estoque
- Implementar retry para operações de rede
- Cache com Redis para consultas frequentes

Forneça:
- Service interface e implementação
- Commands e Responses
- Configuração de cache
- Testes com Testcontainers
```



---

## **FASE 4: INFRAESTRUTURA E ADAPTERS**

### **4.1 Repository Adapter - MongoDB**
```prompt
Crie o ProductMongoRepository implementando ProductRepositoryPort:

Requisitos:
1. Estender MongoRepository<ProductDocument, String>
2. Métodos custom: findByBarcode(), findActiveProducts()
3. Índices: @CompoundIndex para barcode (único), category, status
4. Converter: Product <-> ProductDocument
5. Queries com @Query para consultas complexas
6. Implementar paginação e sorting

Forneça:
1. Repository interface e implementação
2. ProductDocument (entidade MongoDB)
3. Converter/Mapper
4. Configuração de índices
5. Testes de integração com MongoDB Testcontainers
```

### **4.2 REST Controller - ProductController**
```prompt
Crie o ProductController REST completo:

Endpoints:
- POST /api/v1/products (criação)
- GET /api/v1/products/{id} (busca por ID)
- PUT /api/v1/products/{id} (atualização)
- GET /api/v1/products (listagem com paginação, filtros)
- DELETE /api/v1/products/{id} (exclusão lógica)

Especificações:
- Versionamento: /api/v1
- Validação: @Valid nos DTOs
- Documentação: Swagger/OpenAPI com exemplos
- Segurança: @PreAuthorize("hasRole('PHARMACIST')")
- Tratamento de erros: GlobalExceptionHandler
- Media type: application/json
- HATEOAS: links para recursos relacionados

Forneça:
1. Controller completo
2. GlobalExceptionHandler
3. ErrorResponse padronizado
4. Configuração Swagger
5. Testes de integração com MockMvc
```

### **4.3 Event Publisher - Kafka Adapter**
```prompt
Crie o EventPublisherKafkaAdapter para publicação de eventos de domínio:

Requisitos:
1. Implementar EventPublisherPort
2. Usar KafkaTemplate<String, DomainEvent>
3. Serialização JSON com ObjectMapper
4. Tópicos: product.events, inventory.events
5. Retry com @Retryable (3 tentativas)
6. Dead Letter Queue para falhas permanentes
7. MDC correlationId para rastreamento
8. Métricas: contador de eventos publicados

Forneça:
1. Adapter implementation
2. Configuração Kafka
3. Serializador customizado
4. Health indicator
5. Testes com EmbeddedKafka
```
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++PARAMOS AQUI!! +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++=
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
---

## **FASE 5: TESTES E QUALIDADE**

### **5.1 Testes Unitários - Product Aggregate**
```prompt
Crie testes unitários completos para a entidade Product:

Cenários a testar:
1. Criação com dados válidos
2. Exceção ao criar com nome nulo
3. Exceção ao criar com barcode inválido
4. Atualização bem-sucedida
5. Marcar como descontinuado
6. Mudança de preço com validação
7. Publicação de eventos

Técnicas:
- JUnit 5 + AssertJ
- @DisplayName descritivo
- Parameterized tests para casos de validação
- Test factory para cenários complexos
- Cobertura: 100% métodos, 90% branches

Forneça:
1. Classe de teste completa
2. Test data builders
3. Custom assertions
4. Relatório de cobertura
```

### **5.2 Testes de Integração - ProductController**
```prompt
Crie testes de integração para ProductController:

Configuração:
- @SpringBootTest
- @AutoConfigureMockMvc
- Testcontainers: MongoDB, PostgreSQL
- @TestConfiguration para mocks

Testes:
1. POST /api/v1/products → 201 Created
2. POST com barcode duplicado → 400 Bad Request
3. GET /api/v1/products/{id} → 200 OK
4. GET com ID inexistente → 404 Not Found
5. PUT /api/v1/products/{id} → 200 OK
6. DELETE /api/v1/products/{id} → 204 No Content
7. GET /api/v1/products com paginação → 200 OK

Forneça:
1. Classe de integração
2. Database initializer
3. JSON assertions
4. Test utilities
```

---

## **FASE 6: FRONTEND REACT**

### **6.1 Componente - ProductList**
```prompt
Crie o componente React ProductList para listagem de produtos:

Requisitos:
- TypeScript com tipagem forte
- React 18 com hooks
- Vite como build tool
- Material-UI ou Ant Design
- Paginação client-side
- Filtros: nome, categoria, status
- Ordenação por colunas
- Loading states
- Error handling
- Responsivo

Funcionalidades:
1. Buscar produtos da API (/api/v1/products)
2. Exibir em tabela com colunas: ID, Nome, Categoria, Preço, Estoque, Ações
3. Botões: Editar, Excluir, Ver Detalhes
4. Modal de confirmação para exclusão
5. Notificações com react-toastify

Forneça:
1. Componente ProductList.tsx
2. Tipos TypeScript (Product, ProductResponse)
3. Service layer (API client)
4. Custom hooks (useProducts, useProductMutations)
5. Testes com Jest + React Testing Library
6. Storybook stories
```

### **6.2 Componente - ProductForm**
```prompt
Crie o componente ProductForm para criação/edição de produtos:

Campos:
- Nome (text, required)
- Barcode (text, required, validation)
- Descrição (textarea)
- Categoria (select: MEDICINE, COSMETIC, EQUIPMENT)
- Preço (number, min: 0)
- Status (radio: ACTIVE, INACTIVE)
- Imagem (file upload)

Validações:
- Formik + Yup schema
- Validação em tempo real
- Mensagens de erro claras
- Submit apenas se válido

Funcionalidades:
- Modo criação vs edição
- Preview de imagem
- Auto-complete para categorias
- Draft saving
- Success/error feedback

Forneça:
1. ProductForm.tsx
2. Validation schema
3. Form utilities
4. Image upload component
5. Tests com user-event
```

### **6.3 Configuração do Projeto React**
```prompt
Crie a configuração completa do projeto React:

Estrutura:
- src/
  - components/ (reutilizáveis)
  - pages/ (rotas)
  - services/ (API clients)
  - hooks/ (custom hooks)
  - utils/ (helpers)
  - types/ (TypeScript)
  - styles/ (CSS modules)
  - assets/ (imagens, fonts)

Configurações:
- Vite config (tsconfig, vite.config.ts)
- ESLint + Prettier
- Husky + lint-staged
- Absolute imports
- Environment variables
- Proxy para API backend
- PWA config (opcional)

Forneça:
1. package.json com scripts
2. Configurações Vite
3. ESLint/Prettier config
4. Dockerfile para frontend
5. Nginx config para produção
6. GitHub Actions para CI/CD
```

---

## **FASE 7: SEGURANÇA E OBSERVABILIDADE**

### **7.1 Security Config - Spring Security**
```prompt
Crie a configuração completa de segurança:

Requisitos:
- Keycloak como OAuth2 provider
- JWT validation
- Role-based access control
- CORS configuration
- CSRF disabled (APIs stateless)
- Method security: @PreAuthorize, @PostAuthorize
- Password encoder: BCrypt

Roles:
- ADMIN: todas as operações
- MANAGER: gestão de produtos e estoque
- PHARMACIST: vendas e consultas
- AUDITOR: apenas leitura

Forneça:
1. SecurityConfig.java
2. KeycloakJwtAuthenticationConverter
3. Security utilities
4. Testes de segurança
5. Postman collection para auth
```

### **7.2 Observabilidade - Micrometer + Grafana**
```prompt
Configure observabilidade completa:

Métricas:
- @Timed em controllers e services
- Custom metrics: produtos_criados_total, estoque_baixo_total
- JVM metrics: memory, threads, GC
- Database metrics: query duration, connections
- HTTP metrics: latency, status codes

Logging:
- Logback com JSON layout
- MDC: correlationId, userId
- @Aspect para logging de serviços
- Níveis: INFO (negócio), DEBUG (técnico)

Forneça:
1. application-observability.yml
2. logback-spring.xml
3. LoggingAspect.java
4. Custom metrics configuration
5. Grafana dashboard JSON
6. Prometheus alerts
```

---

## **FASE 8: DEPLOY E DEVOPS**

### **8.1 Kubernetes Manifests**
```prompt
Crie os manifests Kubernetes para deploy:

Recursos:
1. Deployment (backend Spring Boot)
2. Deployment (frontend React)
3. Service (ClusterIP para interno, LoadBalancer para externo)
4. Ingress (Nginx com TLS)
5. ConfigMap (application.properties)
6. Secret (senhas, tokens)
7. HorizontalPodAutoscaler
8. PodDisruptionBudget
9. NetworkPolicy

Configurações:
- Liveness e readiness probes
- Resource limits/requests
- Rolling update strategy
- Affinity/anti-affinity rules
- Pod security context

Forneça:
1. k8s/backend-deployment.yaml
2. k8s/frontend-deployment.yaml
3. k8s/ingress.yaml
4. k8s/configmap.yaml
5. k8s/hpa.yaml
6. Helm chart (opcional)
```

### **8.2 CI/CD Pipeline - GitHub Actions**
```prompt
Crie pipeline CI/CD completa:

Workflows:
1. Build and Test (push, PR):
   - Checkout
   - Setup Java 17 + Node 18
   - Build backend (Maven)
   - Build frontend (npm)
   - Run tests (unit, integration)
   - SonarCloud analysis
   - Build Docker images
   - Push to registry

2. Deploy to Staging (main branch):
   - Deploy to Kubernetes
   - Run smoke tests
   - Notify Slack

3. Deploy to Production (tag):
   - Manual approval
   - Blue-green deployment
   - Rollback automation

Forneça:
1. .github/workflows/build.yml
2. .github/workflows/deploy-staging.yml
3. .github/workflows/deploy-production.yml
4. Deployment scripts
5. Rollback procedures
```

---

## **PADRÕES DE PROMPT CLI**

### **Para Criar Novo Módulo:**
```prompt
MÓDULO: [Nome do Módulo]
CONTEXTO: PharmaCare ERP - [Contexto específico]

REQUISITOS:
1. [Funcionalidade 1]
2. [Funcionalidade 2]
3. [Integração com módulos: X, Y, Z]
4. [Regras de negócio específicas]

FORNECER:
- [ ] Backend: Entities, Repositories, Services, Controllers
- [ ] Frontend: Components, Pages, Services, Types
- [ ] Testes: Unitários, Integração, E2E
- [ ] Configurações: Database, Security, API
- [ ] Documentação: README, API docs, Deployment

NOTAS:
- Manter backward compatibility
- Seguir arquitetura hexagonal
- Incluir logging e tratamento de erros
- Adicionar métricas e observabilidade
```

### **Para Análise de Código Existente:**
```prompt
ANÁLISE DE CÓDIGO:
Arquivo: [caminho/arquivo.ext]

CRITÉRIOS:
1. Conformidade com arquitetura hexagonal
2. Qualidade de código (SOLID, clean code)
3. Segurança (OWASP, validações)
4. Performance (complexidade, queries)
5. Testabilidade (cobertura, mocks)

PROBLEMAS IDENTIFICADOS:
1. [Problema 1] - Severidade: [Alta/Média/Baixa]
2. [Problema 2] - Severidade: [Alta/Média/Baixa]

SUGESTÕES DE MELHORIA:
1. [Refatoração 1] com exemplo
2. [Refatoração 2] com exemplo

CÓDIGO CORRIGIDO:
[Fornecer código completo corrigido]

IMPACTO:
- Backward compatibility: [Sim/Não]
- Testes afetados: [Listar]
- Deployment notes: [Observações]
```

### **Para Debugging:**
```prompt
DEBUG REQUEST:
Sintoma: [Descrição do erro]
Stack trace: [Trecho relevante]
Contexto: [O que estava sendo executado]

ANÁLISE SOLICITADA:
1. Root cause identification
2. Data flow analysis
3. State at failure
4. Possible race conditions
5. External dependencies status

SOLUÇÕES:
- Hotfix (crítico): [Código]
- Permanent fix: [Código + testes]
- Monitoring improvements: [Métricas]
- Prevention: [Code review checklist]

TESTES DE REGRESSÃO:
[Novos testes para prevenir recorrência]
```

---

## **CHECKLIST FINAL POR FEATURE**

```prompt
CHECKLIST FINAL - FEATURE: [Nome]

BACKEND (Spring Boot):
✅ Entities com invariantes de domínio
✅ Repositories com queries otimizadas
✅ Services com regras de negócio
✅ Controllers com validações
✅ DTOs e Mappers
✅ Eventos e handlers
✅ Testes unitários (>90%)
✅ Testes de integração
✅ Security annotations
✅ Logging e métricas

FRONTEND (React):
✅ Components TypeScript
✅ API services
✅ State management
✅ Form validations
✅ Error handling
✅ Loading states
✅ Responsive design
✅ Tests (Jest + RTL)
✅ Accessibility (a11y)

INFRAESTRUTURA:
✅ Docker images
✅ Kubernetes manifests
✅ CI/CD pipeline
✅ Database migrations
✅ Environment configs
✅ Monitoring setup
✅ Backup procedures
✅ Rollback plan

DOCUMENTAÇÃO:
✅ API documentation (OpenAPI)
✅ Component documentation (Storybook)
✅ Deployment guide
✅ Rollback procedures
✅ ADR (se mudança arquitetural)
```

---

## **FLUXO DE TRABALHO RECOMENDADO**

1. **Iniciar Sessão:**
```prompt
CONTEXTO ATUAL: PharmaCare ERP
MÓDULO EM DESENVOLVIMENTO: [Nome do módulo]
TAREFA ATUAL: [Descrição da tarefa]
ARQUIVOS ENVOLVIDOS: [Listar arquivos relevantes]
```

2. **Desenvolver Feature:**
```prompt
NOVA FEATURE: [Nome]
REQUISITOS: [Listar]
ENTREGÁVEIS: [Backend, Frontend, Testes, Docs]
PRAZO: [Estimativa]
```

3. **Revisar e Testar:**
```prompt
CODE REVIEW SOLICITADO:
Arquivos: [Listar]
Checklist: [Arquitetura, Segurança, Performance, Testes]
```

4. **Finalizar:**
```prompt
FEATURE COMPLETA:
Resumo: [O que foi desenvolvido]
Testes passando: [Sim/Não]
Documentação atualizada: [Sim/Não]
Próximos passos: [O que vem depois]
```

---

## **DICAS PARA CLI EFETIVA**

1. **Sempre fornecer contexto completo**
2. **Separar backend/frontend em prompts distintos**
3. **Pedir validações específicas (security, performance)**
4. **Solicitar exemplos de uso para componentes complexos**
5. **Manter um log das decisões arquiteturais**
6. **Testar integrações entre módulos**

**Exemplo de interação:**
```
Usuário: "CONTEXTO: PharmaCare ERP - Módulo de Produtos"
Usuário: "TAREFA: Criar CRUD completo de produtos"
Usuário: "FORNECER: Backend (Spring Boot) + Frontend (React) + Testes"
```

---

## **REGRA DE OURO CLI**

```prompt
⚠️ CONTEXTO CONSTANTE:
- PharmaCare ERP: Sistema de gestão para farmácias
- Arquitetura: Hexagonal com bounded contexts
- Stack: Java 17/Spring Boot 3 + React 18/TypeScript
- Metodologia: TDD, Git Flow, CI/CD

REGRAS:
1. NÃO introduzir tecnologias não aprovadas
2. NÃO quebrar contratos existentes
3. SEMPRE manter backward compatibility
4. SEMPRE incluir testes e documentação
5. SEMPRE considerar segurança e performance

INTERAÇÃO CLI:
- Fornecer código completo e comentado
- Explicar decisões arquiteturais
- Incluir exemplos de uso
- Validar com testes automatizados
