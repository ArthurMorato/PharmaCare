-- Script de inicialização do PostgreSQL
-- Executado automaticamente na primeira inicialização do container

-- Banco de dados para o Keycloak (se não existir)
SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- Conectar ao banco de dados principal
\c pharmacare_transactional;

-- Criar extensões úteis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Criar schemas organizados
CREATE SCHEMA IF NOT EXISTS transactions;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS reporting;
COMMENT ON SCHEMA transactions IS 'Dados transacionais e eventos';
COMMENT ON SCHEMA audit IS 'Dados de auditoria e logs';
COMMENT ON SCHEMA reporting IS 'Dados para relatórios e BI';

-- Definir search_path para usuário pharmacare
ALTER ROLE pharmacare SET search_path TO transactions, audit, reporting, public;

-- Tabela de sagas (padrão Saga para transações distribuídas)
CREATE TABLE IF NOT EXISTS transactions.saga_instance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    saga_type VARCHAR(100) NOT NULL,
    saga_name VARCHAR(200) NOT NULL,
    state VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    step_index INTEGER DEFAULT 0,
    completed_steps JSONB DEFAULT '[]'::jsonb,
    compensation_data JSONB,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT valid_state CHECK (state IN ('STARTED', 'PROCESSING', 'COMPENSATING', 'COMPLETED', 'FAILED', 'EXPIRED')),
    CONSTRAINT valid_step_index CHECK (step_index >= 0)
);

COMMENT ON TABLE transactions.saga_instance IS 'Instâncias de sagas para transações distribuídas';
COMMENT ON COLUMN transactions.saga_instance.saga_type IS 'Tipo da saga (ex: STOCK_RESERVATION)';
COMMENT ON COLUMN transactions.saga_instance.saga_name IS 'Nome descritivo da saga';
COMMENT ON COLUMN transactions.saga_instance.state IS 'Estado atual da saga';
COMMENT ON COLUMN transactions.saga_instance.payload IS 'Dados da transação em JSON';
COMMENT ON COLUMN transactions.saga_instance.step_index IS 'Índice do passo atual';

-- Tabela para outbox pattern (para eventos de domínio)
CREATE TABLE IF NOT EXISTS transactions.outbox (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aggregate_id VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER DEFAULT 1,
    payload JSONB NOT NULL,
    metadata JSONB DEFAULT '{}'::jsonb,
    published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    retry_count INTEGER DEFAULT 0,
    last_retry_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    
    CONSTRAINT valid_event_version CHECK (event_version >= 1)
);

COMMENT ON TABLE transactions.outbox IS 'Tabela outbox para publicação assíncrona de eventos';
COMMENT ON COLUMN transactions.outbox.aggregate_id IS 'ID do agregado que gerou o evento';
COMMENT ON COLUMN transactions.outbox.aggregate_type IS 'Tipo do agregado (ex: Product, Sale)';
COMMENT ON COLUMN transactions.outbox.event_type IS 'Tipo do evento (ex: ProductCreated, SaleCompleted)';

-- Tabela para auditoria detalhada
CREATE TABLE IF NOT EXISTS audit.audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(100),
    username VARCHAR(200),
    user_ip INET,
    user_agent TEXT,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    changes JSONB GENERATED ALWAYS AS (
        CASE 
            WHEN old_values IS NULL THEN new_values
            WHEN new_values IS NULL THEN old_values
            ELSE (SELECT jsonb_object_agg(key, value) 
                  FROM jsonb_each(new_values) 
                  WHERE (old_values -> key) IS DISTINCT FROM value)
        END
    ) STORED,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    duration_ms INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE audit.audit_log IS 'Log de auditoria de todas as operações do sistema';
COMMENT ON COLUMN audit.audit_log.changes IS 'Campo gerado automaticamente com as alterações realizadas';

-- Tabela para dead letter queue (mensagens falhas)
CREATE TABLE IF NOT EXISTS transactions.dead_letter_queue (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_system VARCHAR(100) NOT NULL,
    message_type VARCHAR(100) NOT NULL,
    original_message JSONB NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    status VARCHAR(20) DEFAULT 'PENDING',
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'RETRYING', 'PROCESSED', 'FAILED'))
);

-- Criar índices para otimização
CREATE INDEX IF NOT EXISTS idx_saga_instance_type_state 
ON transactions.saga_instance(saga_type, state);

CREATE INDEX IF NOT EXISTS idx_saga_instance_created 
ON transactions.saga_instance(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished 
ON transactions.outbox(published, created_at) 
WHERE published = FALSE;

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate 
ON transactions.outbox(aggregate_type, aggregate_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_outbox_retry 
ON transactions.outbox(retry_count, created_at) 
WHERE published = FALSE;

CREATE INDEX IF NOT EXISTS idx_audit_log_resource 
ON audit.audit_log(resource_type, resource_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_log_user 
ON audit.audit_log(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_audit_log_action 
ON audit.audit_log(action, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_dead_letter_queue_status 
ON transactions.dead_letter_queue(status, created_at);

-- Criar função para atualizar timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Criar trigger para saga_instance
CREATE TRIGGER update_saga_instance_updated_at
    BEFORE UPDATE ON transactions.saga_instance
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Criar views para relatórios
CREATE OR REPLACE VIEW reporting.daily_events AS
SELECT 
    DATE(created_at) as event_date,
    aggregate_type,
    COUNT(*) as event_count,
    COUNT(DISTINCT aggregate_id) as unique_aggregates
FROM transactions.outbox
WHERE published = TRUE
GROUP BY DATE(created_at), aggregate_type;

CREATE OR REPLACE VIEW reporting.audit_summary AS
SELECT 
    DATE(created_at) as audit_date,
    action,
    resource_type,
    COUNT(*) as total_actions,
    COUNT(CASE WHEN success = TRUE THEN 1 END) as successful_actions,
    COUNT(CASE WHEN success = FALSE THEN 1 END) as failed_actions,
    AVG(duration_ms) as avg_duration_ms
FROM audit.audit_log
GROUP BY DATE(created_at), action, resource_type;

-- Conceder permissões
GRANT USAGE ON SCHEMA transactions TO pharmacare;
GRANT USAGE ON SCHEMA audit TO pharmacare;
GRANT USAGE ON SCHEMA reporting TO pharmacare;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA transactions TO pharmacare;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA audit TO pharmacare;
GRANT SELECT ON ALL TABLES IN SCHEMA reporting TO pharmacare;

GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA transactions TO pharmacare;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA audit TO pharmacare;

-- Inserir dados de exemplo para testes (opcional)
INSERT INTO audit.audit_log (
    user_id, username, action, resource_type, resource_id, success
) VALUES 
    ('system', 'System', 'INITIALIZE', 'DATABASE', 'pharmacare_transactional', TRUE),
    ('system', 'System', 'CREATE_SCHEMA', 'SCHEMA', 'transactions', TRUE),
    ('system', 'System', 'CREATE_SCHEMA', 'SCHEMA', 'audit', TRUE)
ON CONFLICT DO NOTHING;

-- Log de conclusão
DO $$
BEGIN
    RAISE NOTICE '✅ Inicialização do PostgreSQL concluída com sucesso!';
    RAISE NOTICE '   - Schemas criados: transactions, audit, reporting';
    RAISE NOTICE '   - Tabelas criadas: saga_instance, outbox, audit_log, dead_letter_queue';
    RAISE NOTICE '   - Índices otimizados para performance';
    RAISE NOTICE '   - Views de relatórios disponíveis';
END $$;