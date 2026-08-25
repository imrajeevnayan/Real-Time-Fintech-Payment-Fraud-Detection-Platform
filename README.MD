🛡️ Aegis Pay
AI-Native Real-Time Fintech Payment Fraud Detection & Auto-Remediation Platform

Aegis Pay is an AI-native, event-driven fintech fraud detection platform built to detect suspicious payment transactions in real time, perform intelligent risk assessment, automatically remediate high-risk accounts, and maintain an immutable audit trail.

Built with Java 21, Spring Boot 3.5, Spring Modulith, Spring AI, Apache Kafka, PostgreSQL, PgVector, Redis, and virtual threads.

🚀 Features
⚡ Real-time payment transaction processing
🤖 Two-stage AI-powered fraud detection
🧠 Fast LLM assessment using Ollama
🔬 Deep AI assessment using OpenAI
📚 RAG-based historical transaction analysis
🔎 PgVector similarity search
🌐 IP reputation analysis
🚨 Automatic high-risk transaction detection
🔒 Automatic account freezing
🚩 Automatic account flagging
📒 Immutable event-sourced audit trail
📦 Transactional Outbox Pattern
📨 Event-driven Kafka architecture
🧩 Modular architecture with Spring Modulith
🧵 Java 21 Virtual Threads
📡 REST / WebSocket transaction ingestion
🔄 Versioned Kafka events
🐳 Docker-based infrastructure
🏗️ Architecture
                         ┌──────────────────────┐
                         │   Payment Client     │
                         └──────────┬───────────┘
                                    │
                                    │ REST / WebSocket
                                    ▼
                         ┌──────────────────────┐
                         │      Ingestion       │
                         │                      │
                         │ Validation           │
                         │ Normalization        │
                         │ Transaction Aggregate│
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ Transactional Outbox │
                         │    outbox_event       │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │        Kafka         │
                         │                      │
                         │ aegis.transaction.v1 │
                         │ aegis.risk.v1        │
                         │ aegis.remediation.v1 │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     Risk Engine      │
                         │                      │
                         │ Ollama Fast Pass     │
                         │          ↓           │
                         │ Ambiguity Detection  │
                         │          ↓           │
                         │ OpenAI Deep Pass     │
                         │          ↓           │
                         │ PgVector RAG         │
                         │          ↓           │
                         │ IP Reputation        │
                         └──────────┬───────────┘
                                    │
                                    ▼
                              Risk Assessment
                                    │
                       ┌────────────┴────────────┐
                       │                         │
                       ▼                         ▼
                  Normal Risk              High Risk
                                                 │
                                                 ▼
                                      ┌────────────────────┐
                                      │    Remediation     │
                                      │                    │
                                      │ Score >= 90        │
                                      │ Freeze Account     │
                                      │                    │
                                      │ Score >= 75        │
                                      │ Flag Account       │
                                      └─────────┬──────────┘
                                                │
                                                ▼
                                      ┌────────────────────┐
                                      │       Ledger       │
                                      │                    │
                                      │ Immutable Audit    │
                                      │ Trail              │
                                      └────────────────────┘

🧩 Modules

Aegis Pay follows a modular architecture using Spring Modulith.

Module	Responsibility	Owns
ingestion	REST/WebSocket intake, validation and normalization	Transaction aggregate
riskengine	AI fraud scoring, RAG and historical analysis	risk_assessments
remediation	Automated account actions	Account aggregate
ledger	Immutable audit trail	ledger_events

All cross-module communication is asynchronous through versioned Kafka events.

aegis.<domain>.vN


Examples:

aegis.transaction.v1
aegis.risk.v1
aegis.remediation.v1

🔄 Transaction Flow
1. Transaction Ingestion

A payment transaction is submitted through:

POST /api/v1/transactions


The ingestion module:

Validates the request
Normalizes transaction data
Persists the transaction
Creates an outbox event

The transaction and outbox event are committed atomically.

2. Transactional Outbox

The transaction produces:

TransactionRecorded


which is stored in:

outbox_event


The OutboxRelay publishes the event to Kafka.

Database Transaction
        │
        ├── Transaction
        │
        └── Outbox Event
                 │
                 ▼
            OutboxRelay
                 │
                 ▼
               Kafka


This prevents the classic failure scenario where the database transaction succeeds but Kafka publishing fails.

3. AI Risk Assessment

The riskengine consumes the transaction event.

The first stage uses Ollama for a fast assessment.

Transaction
     │
     ▼
Ollama Fast Assessment
     │
     ├── Low Risk
     │
     └── Ambiguous / Suspicious
                │
                ▼
        OpenAI Deep Assessment


The deep assessment can use:

Historical transactions
PgVector similarity search
Customer transaction behavior
IP reputation
Transaction metadata
AI-generated reasoning
🧠 RAG-Based Fraud Detection

Aegis Pay uses PostgreSQL + PgVector to retrieve historical transaction context.

The risk engine retrieves the customer's nearest historical transactions and provides them as context for the deep AI assessment.

Incoming Transaction
        │
        ▼
Generate / Retrieve Vector
        │
        ▼
PgVector Similarity Search
        │
        ▼
Historical Transactions
        │
        ▼
OpenAI Deep Assessment
        │
        ▼
Risk Score


This allows the AI to evaluate a transaction in the context of previous behavior instead of treating every transaction independently.

🚨 Risk & Remediation

When the risk score exceeds the configured threshold, the risk engine publishes:

aegis.risk.v1.HighRiskTransactionDetected


The remediation module consumes the event.

Risk Score	Action
< 75	Allow / Monitor
>= 75	Flag Account
>= 90	Freeze Account
Example
Risk Score = 94
       │
       ▼
High Risk Transaction
       │
       ▼
Account Frozen
       │
       ▼
Remediation Event
       │
       ▼
Immutable Ledger

📒 Immutable Audit Ledger

The ledger module maintains an append-only audit trail.

Important events are recorded for traceability, including:

Transaction events
Risk assessments
Fraud decisions
High-risk detections
Account flags
Account freezes
Remediation events
Transaction
     │
     ▼
Risk Assessment
     │
     ▼
Fraud Decision
     │
     ▼
Remediation
     │
     ▼
Immutable Ledger

📦 Transactional Outbox Pattern

Aegis Pay uses the Transactional Outbox Pattern for reliable event publishing.

Instead of:

Save Transaction
      ↓
Publish Kafka Event
      ↓
Kafka Failure ❌


the system performs:

┌────────────────────────────┐
│       DB Transaction       │
│                            │
│  Save Transaction          │
│          +                 │
│  Save Outbox Event         │
│                            │
│      Atomic Commit         │
└─────────────┬──────────────┘
              │
              ▼
         OutboxRelay
              │
              ▼
            Kafka


The architecture is also designed to be Debezium-ready for future CDC-based event publishing.

🛠️ Technology Stack
Technology	Purpose
Java 21	Application runtime
Spring Boot 3.5	Backend framework
Spring Modulith 1.4	Modular architecture
Spring AI 1.0	AI integration
Ollama	Fast local LLM assessment
OpenAI	Deep fraud assessment
Apache Kafka	Event streaming
PostgreSQL 17	Primary database
PgVector	Vector similarity search
Redis 7	Caching
Virtual Threads	High-concurrency processing
Maven	Build system
Docker Compose	Local infrastructure
📁 Project Structure
aegis-pay/
│
├── ingestion/
│   ├── transaction/
│   ├── validation/
│   └── normalization/
│
├── riskengine/
│   ├── ai/
│   ├── rag/
│   ├── pgvector/
│   └── risk-assessment/
│
├── remediation/
│   ├── account/
│   ├── freezing/
│   └── flagging/
│
├── ledger/
│   ├── events/
│   └── audit/
│
├── infrastructure/
│   └── compose/
│
├── pom.xml
└── README.md

🚀 Quick Start
Prerequisites

Make sure you have installed:

Java 21+
Maven
Docker
Docker Compose
Ollama

You also need an OpenAI API key for the deep AI assessment flow.

1. Clone the Repository
git clone https://github.com/imrajeevnayan/Real-Time-Fintech-Payment-Fraud-Detection-Platform.git

cd Real-Time-Fintech-Payment-Fraud-Detection-Platform

2. Start Infrastructure
docker compose up -d


This starts the required infrastructure:

PostgreSQL 17
PgVector
Redis 7
Kafka 3.8

If your Compose file is located under infra/, run the command from the appropriate directory or specify the Compose file explicitly.

3. Configure OpenAI

Linux / macOS:

export OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxx


Windows PowerShell:

$env:OPENAI_API_KEY="sk-xxxxxxxxxxxxxxxx"

4. Start the Application
mvn spring-boot:run


The application will start on:

http://localhost:8080

📖 API Documentation

Once the application is running, open:

http://localhost:8080/swagger-ui.html


Swagger provides an interactive interface for testing the REST APIs.

💳 Submit a Transaction

Example request:

POST /api/v1/transactions
Content-Type: application/json


Example payload:

{
  "accountId": "ACC-10001",
  "amount": 24999.00,
  "currency": "INR",
  "merchant": "Example Merchant",
  "paymentMethod": "UPI"
}


The request enters the following pipeline:

POST /transactions
       │
       ▼
   Validation
       │
       ▼
   Persistence
       │
       ▼
 Transactional Outbox
       │
       ▼
      Kafka
       │
       ▼
  Risk Engine
       │
       ▼
  Risk Score
       │
       ├── Normal
       │
       └── High Risk
              │
              ▼
         Remediation
              │
              ▼
            Ledger

🧪 Testing

Run the complete test suite:

mvn test


Run Spring Modulith architecture verification:

mvn test -Dtest=ModularityTests


The modularity verification ensures that the application's module boundaries remain intact.

📡 Kafka Events
Transaction Events
aegis.transaction.v1


Example:

TransactionRecorded

Risk Events
aegis.risk.v1


Example:

HighRiskTransactionDetected

Remediation Events
aegis.remediation.v1


Examples:

AccountFlagged
AccountFrozen

🔐 Design Principles
Event-Driven

Modules communicate asynchronously through Kafka rather than tightly coupled direct calls.

Modular

Business capabilities are isolated using Spring Modulith.

AI-Native

AI is part of the fraud decision pipeline rather than an external afterthought.

Context-Aware

Historical transaction behavior is incorporated into risk analysis using RAG.

Auditable

Fraud decisions and remediation actions are recorded in an immutable ledger.

Reliable

The Transactional Outbox Pattern ensures domain state and events are persisted atomically.

Scalable

Java 21 virtual threads and asynchronous event processing support high-concurrency workloads.

🗺️ Roadmap
 Real-time fraud monitoring dashboard
 Fraud investigation dashboard
 Explainable AI fraud reports
 Device fingerprint analysis
 Geolocation anomaly detection
 Transaction velocity rules
 Fraud-ring / graph detection
 Model evaluation pipeline
 Model drift monitoring
 Human-in-the-loop fraud review
 OpenTelemetry integration
 Prometheus + Grafana monitoring
 Kubernetes deployment
 Debezium CDC integration
 Multi-tenant fintech support
⚠️ Disclaimer

This project is intended for educational, research, and engineering purposes.

It should not be used directly in a production financial environment without appropriate:

Security review
Compliance validation
Fraud-model validation
Privacy controls
Load testing
Reliability testing
Operational monitoring
Human oversight

AI-generated fraud decisions should be independently validated before being used to automatically block real financial transactions.

⭐ Contributing

Contributions, ideas, bug reports, and architecture improvements are welcome.

If you find this project useful, consider giving the repository a ⭐.

📄 License

See the repository license for applicable usage and distribution terms.

<div align="center">
🛡️ Aegis Pay

Detect → Explain → Respond → Audit

Built with ❤️ for intelligent fintech security.

</div>