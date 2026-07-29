# OrderFlow Backend

> **Production-ready Spring Boot 3.x SaaS backend with WhatsApp Cloud API integration.**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.4-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Quick Start (Docker)](#quick-start-docker)
5. [Local Development](#local-development)
6. [Environment Variables](#environment-variables)
7. [WhatsApp Cloud API Setup](#whatsapp-cloud-api-setup)
8. [REST API Reference](#rest-api-reference)
9. [Webhook Events](#webhook-events)
10. [Security](#security)
11. [Database Migrations](#database-migrations)
12. [Project Structure](#project-structure)

---

## Overview

**OrderFlow Backend** provides:

- ✅ Full WhatsApp Cloud API integration (inbound + outbound)
- ✅ Webhook verification and HMAC-SHA256 signature validation
- ✅ All message types: text, image, audio, video, document, interactive, status
- ✅ Customer, Conversation, and Message management
- ✅ REST APIs with API-key authentication
- ✅ OpenAPI / Swagger UI documentation
- ✅ Flyway database migrations
- ✅ Docker + Docker Compose ready
- ✅ Structured JSON error responses
- ✅ Asynchronous webhook processing (200 OK returned immediately)

---

## Architecture

```
src/main/java/com/orderflow/
├── config/              # Spring beans (WebClient, Jackson, OpenAPI, Audit)
├── controller/          # REST controllers
├── dto/                 # Request/Response/Webhook DTOs
│   ├── request/         # Inbound request DTOs
│   └── webhook/         # WhatsApp webhook payload DTOs
├── entity/              # JPA entities (Customer, Conversation, Message, Attachment)
├── exception/           # Custom exceptions + GlobalExceptionHandler
├── mapper/              # MapStruct mappers
├── repository/          # Spring Data JPA repositories
├── security/            # API key filter + Spring Security config
├── service/             # Service interfaces + implementations
│   └── impl/
├── util/                # PhoneNumberUtil, DateTimeUtil, WebhookSignatureValidator
└── whatsapp/            # WhatsApp Cloud API client + @ConfigurationProperties
```

---

## Prerequisites

| Tool | Minimum Version |
|------|----------------|
| Java | 21 |
| Maven | 3.9+ |
| MySQL | 8.0+ (or Docker) |
| Docker | 24+ (optional) |
| Docker Compose | 2.x (optional) |

---

## Quick Start (Docker)

### 1. Clone and configure

```bash
git clone https://github.com/your-org/orderflow-backend.git
cd orderflow-backend
cp .env.example .env
```

### 2. Edit `.env`

Fill in your WhatsApp credentials and choose strong passwords:

```dotenv
WHATSAPP_ACCESS_TOKEN=EAAxxxxx
WHATSAPP_PHONE_NUMBER_ID=1234567890
WHATSAPP_VERIFY_TOKEN=my_secret_verify_token
WHATSAPP_APP_SECRET=my_app_secret
API_KEYS=my-strong-api-key
DB_PASSWORD=my-db-password
```

### 3. Build and start

```bash
docker-compose up --build -d
```

The app starts on **http://localhost:8080**.

### 4. Verify

```bash
# Health check
curl http://localhost:8080/api/v1/management/health

# Swagger UI
open http://localhost:8080/api/v1/swagger-ui.html
```

---

## Local Development

### 1. Start MySQL only

```bash
docker-compose up mysql -d
```

### 2. Configure environment

Set the environment variables (or create `application-local.yml`):

```bash
export DB_HOST=localhost
export DB_NAME=orderflow
export DB_USERNAME=orderflow_user
export DB_PASSWORD=orderflow_pass
export WHATSAPP_ACCESS_TOKEN=your_token
export WHATSAPP_PHONE_NUMBER_ID=your_phone_number_id
export WHATSAPP_VERIFY_TOKEN=your_verify_token
export WHATSAPP_APP_SECRET=your_app_secret
export API_KEYS=dev-api-key
```

### 3. Build and run

```bash
mvn clean spring-boot:run
```

### 4. Compile check only

```bash
mvn clean compile -q
```

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `SERVER_PORT` | No | HTTP port (default: 8080) |
| `DB_HOST` | Yes | MySQL host |
| `DB_PORT` | No | MySQL port (default: 3306) |
| `DB_NAME` | Yes | Database name |
| `DB_USERNAME` | Yes | DB user |
| `DB_PASSWORD` | Yes | DB password |
| `MYSQL_ROOT_PASSWORD` | Docker only | MySQL root password |
| `WHATSAPP_ACCESS_TOKEN` | Yes | WhatsApp API access token |
| `WHATSAPP_PHONE_NUMBER_ID` | Yes | WhatsApp phone number ID |
| `WHATSAPP_VERIFY_TOKEN` | Yes | Webhook verify token |
| `WHATSAPP_APP_SECRET` | Yes | App secret (HMAC validation) |
| `API_KEYS` | Yes | Comma-separated valid API keys |
| `SPRING_PROFILES_ACTIVE` | No | Spring profile (default: production) |

---

## WhatsApp Cloud API Setup

### Step 1 — Create a Meta App

1. Go to [developers.facebook.com](https://developers.facebook.com)
2. Create a new app → Business type
3. Add **WhatsApp** product
4. Note your **Access Token**, **Phone Number ID**, and **App Secret**

### Step 2 — Configure Webhook

1. In Meta App Dashboard → WhatsApp → Configuration
2. Set **Callback URL**: `https://your-domain.com/api/v1/webhook`
3. Set **Verify Token**: same value as your `WHATSAPP_VERIFY_TOKEN` env var
4. Subscribe to: `messages`

### Step 3 — Test Verification

```bash
curl "http://localhost:8080/api/v1/webhook?\
hub.mode=subscribe&\
hub.verify_token=YOUR_VERIFY_TOKEN&\
hub.challenge=test123"
# Expected: test123
```

### Step 4 — Test Inbound Message (simulate)

```bash
curl -X POST http://localhost:8080/api/v1/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "object": "whatsapp_business_account",
    "entry": [{
      "id": "WABA_ID",
      "changes": [{
        "value": {
          "messaging_product": "whatsapp",
          "metadata": {
            "display_phone_number": "15550000001",
            "phone_number_id": "PHONE_ID"
          },
          "contacts": [{
            "profile": {"name": "Alice"},
            "wa_id": "15551234567"
          }],
          "messages": [{
            "id": "wamid.test001",
            "from": "15551234567",
            "timestamp": "1700000000",
            "type": "text",
            "text": {"body": "Hello, OrderFlow!"}
          }]
        },
        "field": "messages"
      }]
    }]
  }'
```

---

## REST API Reference

All REST endpoints (except `/webhook` and `/management/health`) require the `X-API-Key` header.

### Swagger UI

```
http://localhost:8080/api/v1/swagger-ui.html
```

### Customers

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/customers` | List all customers (paginated) |
| `GET` | `/customers/search?q=alice` | Search by name/phone/email |
| `GET` | `/customers/{id}` | Get by ID |
| `GET` | `/customers/wa/{waId}` | Get by WhatsApp ID |
| `POST` | `/customers` | Create customer |
| `PUT` | `/customers/{id}` | Update customer |
| `PATCH` | `/customers/{id}/status?status=BLOCKED` | Update status |
| `DELETE` | `/customers/{id}` | Delete customer |

### Conversations

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/conversations` | List all conversations |
| `GET` | `/conversations/customer/{customerId}` | List for a customer |
| `GET` | `/conversations/{id}` | Get by ID |
| `POST` | `/conversations/{id}/close` | Close a conversation |
| `DELETE` | `/conversations/{id}` | Delete conversation |

### Messages

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/messages/conversation/{conversationId}` | List messages |
| `GET` | `/messages/{id}` | Get by ID |
| `POST` | `/messages/conversation/{conversationId}/send-text` | Send text message |

### Webhook

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/webhook` | Meta verification challenge | Public |
| `POST` | `/webhook` | Inbound events | Public (HMAC) |

---

## Webhook Events

Supported inbound message types stored in the database:

| Type | Entity Field | Notes |
|------|-------------|-------|
| `text` | `body` | Plain text body |
| `image` | `body` = caption | Media ID stored in `attachments` |
| `audio` | — | Media ID stored in `attachments` |
| `video` | `body` = caption | Media ID stored in `attachments` |
| `document` | `body` = caption | Media ID + filename in `attachments` |
| `sticker` | — | Media ID stored in `attachments` |
| `interactive` | `body` | `BUTTON:id|title` or `LIST:id|title` |
| Status update | Updates `messages.status` | `sent/delivered/read/failed` |

---

## Security

### API Key Authentication

All REST endpoints (except webhook/actuator/swagger) require:

```http
X-API-Key: your-api-key
```

Configure multiple keys (comma-separated) in `API_KEYS` env var.
Generate a strong key: `openssl rand -hex 32`

### Webhook Security

- **Verification**: `GET /webhook` checks `hub.verify_token`
- **Signature**: `POST /webhook` validates `X-Hub-Signature-256` (HMAC-SHA256 with your App Secret)

---

## Database Migrations

Flyway automatically runs migrations on startup from `src/main/resources/db/migration/`:

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__create_customers.sql` | `customers` table |
| V2 | `V2__create_conversations.sql` | `conversations` table |
| V3 | `V3__create_messages.sql` | `messages` table |
| V4 | `V4__create_attachments.sql` | `attachments` table |

To manually run migrations:

```bash
mvn flyway:migrate -Dflyway.url=jdbc:mysql://localhost:3306/orderflow \
    -Dflyway.user=orderflow_user -Dflyway.password=orderflow_pass
```

---

## Project Structure

```
orderflow-backend/
├── src/
│   ├── main/
│   │   ├── java/com/orderflow/
│   │   │   ├── OrderFlowApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   ├── util/
│   │   │   └── whatsapp/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__create_customers.sql
│   │           ├── V2__create_conversations.sql
│   │           ├── V3__create_messages.sql
│   │           └── V4__create_attachments.sql
├── docker/
│   └── mysql/init/
├── .env.example
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## License

Proprietary — © OrderFlow Inc. All rights reserved.
