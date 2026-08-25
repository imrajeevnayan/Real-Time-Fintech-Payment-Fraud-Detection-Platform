# Aegis Pay

AI-native real-time fraud detection & auto-remediation platform.
Spring Boot 3.5 · Spring Modulith 1.4 · Spring AI 1.0 · Java 21 (virtual threads).

## Modules

| Module        | Responsibility                                                        | Owns                        |
|---------------|-----------------------------------------------------------------------|-----------------------------|
| `ingestion`   | REST/WS intake, validation, normalization                             | `Transaction` aggregate     |
| `riskengine`  | Two-stage LLM scoring (Ollama → OpenAI+RAG), PgVector history         | `risk_assessments` read model |
| `remediation` | Freezes / reviews accounts on high-risk events                        | `Account` aggregate         |
| `ledger`      | Append-only event-sourced audit trail                                  | `ledger_events`             |

All cross-module communication is asynchronous via strictly versioned Kafka
topics (`aegis.<domain>.vN`), published exclusively through the transactional
outbox (`outbox_event` table drained by `OutboxRelay`, Debezium-ready).

## Quick start

```bash
docker compose up -d   # postgres 17 + pgvector, redis 7, kafka 3.8 (needs infra/compose)
export OPENAI_API_KEY=sk-...
mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Submit a transaction: `POST /api/v1/transactions`
- Modulith verification: `mvn test -Dtest=ModularityTests`

## Flow

1. `POST /api/v1/transactions` → validated, normalized, saved + outboxed (one transaction).
2. Outbox relay publishes `aegis.transaction.v1.TransactionRecorded`.
3. `riskengine` consumes it: Ollama fast pass → (if ambiguous) OpenAI deep pass
   with the user's 10 nearest historical transactions from PgVector + IP-reputation tool call.
4. Score ≥ threshold → `aegis.risk.v1.HighRiskTransactionDetected` via outbox.
5. `remediation` freezes (≥90) or flags (≥75) the account; emits `aegis.remediation.v1`.
6. `ledger` appends everything to the immutable audit trail.
