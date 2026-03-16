# PharmaCare ERP - Ambiente Docker

Este documento descreve o ambiente Docker para desenvolvimento do PharmaCare ERP.

## 🚀 Serviços Disponíveis

| Serviço | Porta | URL | Propósito |
|---------|-------|-----|-----------|
| MongoDB | 27017 | `mongodb://localhost:27017` | Dados de domínio (produtos, estoque) |
| PostgreSQL | 5432 | `jdbc:postgresql://localhost:5432/pharmacare_transactional` | Dados transacionais |
| Redis | 6379 | `redis://localhost:6379` | Cache e sessões |
| Kafka | 9093 | `localhost:9093` | Mensageria (acesso do host) |
| Kafka (interno) | 9092 | `kafka:9092` | Mensageria (acesso entre containers) |
| Kafka UI | 8082 | `http://localhost:8082` | Interface de monitoramento |
| Keycloak | 8081 | `http://localhost:8081` | Autenticação e autorização |
| Elasticsearch | 9200 | `http://localhost:9200` | Busca e análise |
| Kibana | 5601 | `http://localhost:5601` | Visualização de logs |
| Zipkin | 9411 | `http://localhost:9411` | Tracing distribuído |

## 📋 Requisitos

- Docker Engine 20.10+
- Docker Compose 2.20+
- 8GB RAM disponível
- 10GB de espaço em disco

## 🛠️ Comandos Úteis

### Iniciar todos os serviços
```bash
docker-compose up -d
```

### Parar todos os serviços
```bash
docker-compose down
```

### Parar e remover volumes
```bash
docker-compose down -v
```

### Verificar status dos serviços
```bash
docker-compose ps
```

### Visualizar logs de um serviço
```bash
docker-compose logs -f mongodb
docker-compose logs -f postgresql
docker-compose logs -f kafka
```

### Acessar serviços via CLI

**MongoDB:**
```bash
docker exec -it pharmacare-mongodb mongosh -u pharmacare_app -p app123456
--authenticationDatabase pharmacare
```

**PostgreSQL:**
```bash
docker exec -it pharmacare-postgres psql -U pharmacare -d pharmacare_transactional
```

**Redis:**
```bash
docker exec -it pharmacare-redis redis-cli -a redis123
```

### Reiniciar um serviço específico
```bash
docker-compose restart kafka
```

## 🔧 Configurações Específicas

### MongoDB
- Banco: `pharmacare`
- Usuário: `pharmacare_app`
- Senha: `app123456`
- Índices: Criados automaticamente no init

### PostgreSQL
- Banco principal: `pharmacare_transactional`
- Banco Keycloak: `keycloak`
- Usuário: `pharmacare`
- Senha: `pharmacare123`
- Schemas: `transactions`, `audit`, `reporting`

### Keycloak
- Admin: `admin` / `Admin@123`
- Realm: `pharmacare`
- Usuários pré-criados: `admin@pharmacare.com`, `farmacia@pharmacare.com`

### Kafka
- Tópicos serão criados dinamicamente pela aplicação
- Kafka UI disponível em `http://localhost:8082`

## 🧪 Dados de Teste

### Usuários disponíveis no Keycloak:

1. **Administrador**
   - Email: `admin@pharmacare.com`
   - Senha: `Admin@123`
   - Funções: `admin`, `auditor`

2. **Farmacêutico**
   - Email: `farmacia@pharmacare.com`
   - Senha: `Farm@123`
   - Funções: `pharmacist`, `stock_manager`

## 🔐 Segurança

### Credenciais Padrão
As credenciais padrão são para **desenvolvimento apenas**. Em produção:

1. Altere todas as senhas
2. Use variáveis de ambiente para credenciais sensíveis
3. Implemente secret management (HashiCorp Vault, AWS Secrets Manager)

### Volumes
Os dados são persistentes através de volumes Docker. Para limpar completamente:

```bash
docker-compose down -v
docker volume prune -f
```

## 🐛 Troubleshooting

### Serviço não inicia
```bash
# Verificar logs específicos
docker-compose logs --tail=50 <servico>

# Verificar health check
docker inspect pharmacare-mongodb --format='{{json .State.Health}}'

# Reiniciar serviço específico
docker-compose restart <servico>
```

### Problemas de conexão
1. Verifique se as portas não estão em uso:
   ```bash
   lsof -i :27017
   lsof -i :5432
   ```

2. Verifique network:
   ```bash
   docker network inspect pharmacare-erp_pharmacare-network
   ```

### Kafka não produz/consome
```bash
# Listar tópicos
docker exec pharmacare-kafka kafka-topics --list --bootstrap-server localhost:9092

# Verificar brokers
docker exec pharmacare-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

## 📊 Monitoramento

### Kibana Dashboard
Acesse `http://localhost:5601` para:
- Visualizar logs do Elasticsearch
- Criar dashboards
- Configurar visualizações

### Kafka UI
Acesse `http://localhost:8082` para:
- Monitorar tópicos
- Ver consumidores
- Produzir/consumir mensagens de teste

### Health Checks
Todos os serviços possuem health checks configurados:
```bash
# Verificar saúde de todos os serviços
docker-compose ps
```

## 🚨 Limitações de Desenvolvimento

1. **Memória**: Kafka e Elasticsearch consomem mais recursos
2. **Performance**: Use `.env` para ajustar recursos por serviço
3. **Segurança**: Credenciais em claro no compose file (apenas dev)

## 🔄 Atualização

Para atualizar versões das imagens:
```bash
docker-compose pull
docker-compose up -d
```

## 📝 Notas

- O ambiente leva 2-3 minutos para inicializar completamente
- Health checks garantem que serviços só são considerados prontos quando operacionais
- Volumes são persistidos mesmo após `docker-compose down`
- Use `docker-compose down -v` para limpar dados completamente
```

## **5. INSTRUÇÕES DE INTEGRAÇÃO**

### **Passo 1: Criar estrutura de diretórios**
```bash
# Na raiz do projeto pharmacare-erp
mkdir -p infra/keycloak
```

### **Passo 2: Copiar arquivos**
1. Copie `docker-compose.yml` para a raiz do projeto
2. Copie `mongo-init.js` para `infra/`
3. Copie `postgres-init.sql` para `infra/`
4. Copie `realm-export.json` para `infra/keycloak/`
5. Copie `README-docker.md` para a raiz

### **Passo 3: Iniciar ambiente**
```bash
# Iniciar todos os serviços
docker-compose up -d

# Verificar status
docker-compose ps

# Aguardar inicialização completa (2-3 minutos)
sleep 180 && docker-compose ps
```

### **Passo 4: Verificar serviços**
```bash
# Verificar MongoDB
curl http://localhost:27017

# Verificar PostgreSQL
docker exec pharmacare-postgres pg_isready -U pharmacare

# Verificar Keycloak
curl -f http://localhost:8081/realms/pharmacare

# Verificar Elasticsearch
curl http://localhost:9200/_cluster/health

# Verificar Kafka
docker exec pharmacare-kafka kafka-topics --list --bootstrap-server localhost:9092