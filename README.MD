# 🛡️ Aegis Pay

### AI-Native Real-Time Fintech Payment Fraud Detection & Auto-Remediation Platform

Aegis Pay is an **AI-native, event-driven fintech fraud detection platform** designed to detect suspicious payment transactions in real time, perform intelligent risk assessment, automatically remediate high-risk accounts, and maintain an immutable audit trail.

Built with **Java 21, Spring Boot 3.5, Spring Modulith, Spring AI, Apache Kafka, PostgreSQL, PgVector, Redis, and Virtual Threads**.

---

## 🚀 Features

- ⚡ Real-time payment transaction processing
- 🤖 AI-powered fraud detection
- 🧠 Two-stage LLM risk assessment
- 🔬 Fast fraud assessment using Ollama
- 🧠 Deep fraud assessment using OpenAI
- 📚 RAG-based historical transaction analysis
- 🔎 PgVector similarity search
- 🌐 IP reputation analysis
- 🚨 Automatic high-risk transaction detection
- 🔒 Automatic account freezing
- 🚩 Automatic account flagging
- 📒 Immutable audit trail
- 📦 Transactional Outbox Pattern
- 📨 Event-driven Kafka architecture
- 🧩 Modular architecture using Spring Modulith
- 🧵 Java 21 Virtual Threads
- 📡 REST / WebSocket transaction ingestion
- 🔄 Versioned Kafka events
- 🐳 Docker-based infrastructure

---

## 🏗️ Architecture

```text
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
                         │     outbox_event     │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │        Kafka         │
                         │                      │
                         │ aegis.transaction.v1│
                         │ aegis.risk.v1        │
                         │ aegis.remediation.v1│
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
```

### 🧩 Modules

Aegis Pay follows a modular architecture using Spring Modulith.

| Module | Responsibility | Owns |
|---|---|---|
| ingestion | Transaction intake, validation and normalization | Transaction aggregate |
| riskengine | AI fraud scoring, RAG and historical analysis | risk_assessments |
| remediation | Automated account actions | Account aggregate |
| ledger | Immutable audit trail | ledger_events |

Modules communicate asynchronously through versioned Kafka events.

`aegis.<domain>.vN`

Examples:

```text
aegis.transaction.v1
aegis.risk.v1
aegis.remediation.v1
```

## 🔄 Transaction Flow

### 1. Transaction Ingestion

A payment transaction is submitted through:

`POST /api/v1/transactions`

The ingestion module:

- Validates the request
- Normalizes transaction data
- Persists the transaction
- Creates an outbox event

The transaction and outbox event are committed atomically.

### 2. Transactional Outbox

The transaction produces:

`TransactionRecorded`

which is stored in:

`outbox_event`

#### The OutboxRelay publishes the event to Kafka.

```text
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
```

### 3. AI Risk Assessment

The riskengine consumes the transaction event.

The first stage uses Ollama for a fast assessment.

```text
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
```

The deep assessment can use:

- Historical transactions
- PgVector similarity search
- Customer transaction behavior
- IP reputation
- Transaction metadata
- AI-generated reasoning

## 🧠 RAG-Based Fraud Detection

Aegis Pay uses PostgreSQL + PgVector to retrieve historical transaction context.

```text
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
```

This allows the AI to evaluate a transaction in the context of previous customer behavior.

## 🚨 Risk & Remediation

When the risk score exceeds the configured threshold, the risk engine publishes:

`aegis.risk.v1.HighRiskTransactionDetected`

The remediation module consumes the event.

| Risk Score | Action |
|---|---|
| < 75 | Allow / Monitor |
| >= 75 | Flag Account |
| >= 90 | Freeze Account |

Example:

```text
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
```

## 📒 Immutable Audit Ledger

The ledger module maintains an append-only audit trail.

Important events include:

- Transaction events
- Risk assessments
- Fraud decisions
- High-risk detections
- Account flags
- Account freezes
- Remediation events

```text
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
```

## 📦 Transactional Outbox Pattern

Aegis Pay uses the Transactional Outbox Pattern for reliable event publishing.

Instead of:

```text
Save Transaction
      ↓
Publish Kafka Event
      ↓
Kafka Failure ❌
```

The system performs:

```text
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
```

This prevents inconsistencies between database state and published events.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application runtime |
| Spring Boot 3.5 | Backend framework |
| Spring Modulith 1.4 | Modular architecture |
| Spring AI 1.0 | AI integration |
| Ollama | Local LLM assessment |
| OpenAI | Deep AI assessment |
| Apache Kafka | Event streaming |
| PostgreSQL 17 | Primary database |
| PgVector | Vector similarity search |
| Redis 7 | Caching |
| Virtual Threads | High-concurrency processing |
| Maven | Build system |
| Docker Compose | Infrastructure |

## 📁 Project Structure

```text
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
```

## 🚀 Quick Start

### Prerequisites

Install:

- Java 21+
- Maven
- Docker
- Docker Compose
- Ollama

You also need an OpenAI API key for the deep AI assessment flow.

### 1. Clone the Repository

```bash
git clone https://github.com/imrajeevnayan/Real-Time-Fintech-Payment-Fraud-Detection-Platform.git

cd Real-Time-Fintech-Payment-Fraud-Detection-Platform
```

### 2. Start Infrastructure

```bash
docker compose up -d
```

### 3. Configure OpenAI

Linux / macOS:

```bash
export OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxx
```

Windows PowerShell:

```powershell
$env:OPENAI_API_KEY="sk-xxxxxxxxxxxxxxxx"
```

### 4. Start the Application

```bash
mvn spring-boot:run
```

Application:

`http://localhost:8080`

---

## 📖 API Documentation

Swagger UI:

`http://localhost:8080/swagger-ui.html`

### 💳 Submit a Transaction

```text
POST /api/v1/transactions
Content-Type: application/json
```

Example:

```json
{
  "accountId": "ACC-10001",
  "amount": 24999.00,
  "currency": "INR",
  "merchant": "Example Merchant",
  "paymentMethod": "UPI"
}
```

Transaction processing flow:

```text
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
```

---

## 🧪 Testing

Run the complete test suite:

```bash
mvn test
```

Run Spring Modulith architecture verification:

```bash
mvn test -Dtest=ModularityTests
```

---

## 📡 Kafka Events

### Transaction Events

`aegis.transaction.v1`

Example:

`TransactionRecorded`

### Risk Events

`aegis.risk.v1`

Example:

`HighRiskTransactionDetected`

### Remediation Events

`aegis.remediation.v1`

Examples:

`AccountFlagged`

`AccountFrozen`

---

## 🔐 Design Principles

**Event-Driven**

Modules communicate asynchronously through Kafka.

**Modular**

Business capabilities are isolated using Spring Modulith.

**AI-Native**

AI is integrated directly into the fraud decision pipeline.

**Context-Aware**

Historical transaction behavior is incorporated using RAG.

**Auditable**

Fraud decisions and remediation actions are recorded in an immutable ledger.

**Reliable**

The Transactional Outbox Pattern ensures reliable event publishing.

**Scalable**

Java 21 Virtual Threads and asynchronous processing support high-concurrency workloads.

---

## 🗺️ Roadmap

- Real-time fraud monitoring dashboard
- Fraud investigation dashboard
- Explainable AI fraud reports
- Device fingerprint analysis
- Geolocation anomaly detection
- Transaction velocity rules
- Fraud-ring / graph detection
- Model evaluation pipeline
- Model drift monitoring
- Human-in-the-loop fraud review
- OpenTelemetry integration
- Prometheus + Grafana monitoring
- Kubernetes deployment
- Debezium CDC integration
- Multi-tenant fintech support

---

## ⚠️ Disclaimer

This project is intended for educational, research, and engineering purposes.

It should not be used directly in a production financial environment without appropriate:

- Security review
- Compliance validation
- Fraud-model validation
- Privacy controls
- Load testing
- Reliability testing
- Operational monitoring
- Human oversight

AI-generated fraud decisions should be independently validated before being used to automatically block real financial transactions.

---

## ⭐ Contributing

Contributions, ideas, bug reports, and architecture improvements are welcome.

If you find this project useful, consider giving the repository a ⭐.

---

## 📄 License

See the repository license for applicable usage and distribution terms.

<div align="center">

🛡️ Aegis Pay

Detect → Explain → Respond → Audit

Built with ❤️ for intelligent fintech security.

</div>