-- Aegis Pay foundation schema. All schema owned by Flyway (never by Spring AI / Hibernate).

-- Transactional Outbox (Debezium reads this table via CDC; the relay is the fallback publisher)
CREATE TABLE outbox_event (
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(64)  NOT NULL,
    aggregate_id    VARCHAR(64)  NOT NULL,
    event_type      VARCHAR(128) NOT NULL,          -- e.g. aegis.transaction.v1.TransactionRecorded
    topic           VARCHAR(128) NOT NULL,          -- e.g. aegis.transaction.v1
    payload         JSONB        NOT NULL,
    headers         JSONB        NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ,
    attempts        INT          NOT NULL DEFAULT 0,
    CONSTRAINT outbox_unpublished_idx UNIQUE (id, published_at)
);
CREATE INDEX idx_outbox_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;

-- Ingestion write model (Transaction aggregate)
CREATE TABLE transactions (
    id              UUID PRIMARY KEY,
    user_id         VARCHAR(64)  NOT NULL,
    merchant_id     VARCHAR(64),
    amount_minor    BIGINT       NOT NULL,
    currency        CHAR(3)      NOT NULL,
    channel         VARCHAR(32)  NOT NULL,
    country         CHAR(2),
    ip_address      VARCHAR(45),
    device_id       VARCHAR(128),
    status          VARCHAR(32)  NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL,
    recorded_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version         BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_transactions_user_time ON transactions (user_id, occurred_at DESC);

-- Risk engine read model (CQRS-lite projection)
CREATE TABLE risk_assessments (
    transaction_id  UUID PRIMARY KEY,
    user_id         VARCHAR(64) NOT NULL,
    risk_score      SMALLINT    NOT NULL CHECK (risk_score BETWEEN 0 AND 100),
    risk_band       VARCHAR(16) NOT NULL,
    reasons         JSONB       NOT NULL DEFAULT '[]',
    model_used      VARCHAR(32) NOT NULL,
    evaluated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_risk_user_score ON risk_assessments (user_id, risk_score DESC);

-- Remediation write model (Account aggregate)
CREATE TABLE accounts (
    id          UUID PRIMARY KEY,
    user_id     VARCHAR(64) NOT NULL UNIQUE,
    status      VARCHAR(32) NOT NULL,
    version     BIGINT      NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cards (
    id          UUID PRIMARY KEY,
    account_id  UUID NOT NULL REFERENCES accounts (id),
    status      VARCHAR(32) NOT NULL,
    version     BIGINT      NOT NULL DEFAULT 0
);

-- Ledger: append-only event-sourced record
CREATE TABLE ledger_events (
    seq           BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(64)  NOT NULL,
    aggregate_id  VARCHAR(64)   NOT NULL,
    event_type    VARCHAR(128)  NOT NULL,
    payload       JSONB         NOT NULL,
    occurred_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_ledger_aggregate ON ledger_events (aggregate_type, aggregate_id, seq);

-- Spring AI PgVector store (matches spring-ai-starter-vector-store-pgvector schema)
CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE IF NOT EXISTS vector_store (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content     TEXT,
    metadata    JSON,
    embedding   vector(1536)
);
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
    ON vector_store USING hnsw (embedding vector_cosine_ops);
