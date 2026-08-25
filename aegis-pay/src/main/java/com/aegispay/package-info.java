/**
 * Aegis Pay — AI-native real-time fraud detection & auto-remediation.
 *
 * Modulith modules:
 *  - ingestion:    REST/WS intake, validation, normalization (write model: Transaction)
 *  - riskengine:   AI scoring (Ollama fast pass + OpenAI RAG deep pass), PgVector history
 *  - remediation:  reacts to risk events, mutates Account aggregate
 *  - ledger:       append-only event-sourced audit trail (projection)
 *
 * Cross-module communication happens ONLY via versioned Kafka topics
 * (aegis.&lt;domain&gt;.vN) published through the transactional outbox.
 */
@org.springframework.modulith.ApplicationModule
package com.aegispay;
