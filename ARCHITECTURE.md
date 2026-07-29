# OrderFlow SaaS — System Architecture & Design

This document details the enterprise architecture, system components, data flows, and database entity relationships for **OrderFlow**.

---

## 🏗️ High-Level System Architecture

```
                       [ WhatsApp Cloud API / Meta Webhook ]
                                         │
                                         ▼
                             [ Nginx Reverse Proxy ]
                            (Port 443 / SSL TLS 1.3)
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 ▼                                               ▼
     [ OrderFlow Admin SPA ]                         [ Spring Boot Backend ]
   (React 19 / Material UI)                        (Java 21 / Spring Boot 3.3)
                                                                 │
                                         ┌───────────────────────┼───────────────────────┐
                                         ▼                       ▼                       ▼
                                   [ Security ]          [ Engine Core ]        [ MySQL Database ]
                               (Spring Security/JWT) (Async Execution Scheduler) (HikariCP / Flyway)
```

---

## 🗄️ Database Entity Relationship Diagram (ERD)

```
 [ USERS ] 1 ────────── 1 [ SUBSCRIPTIONS ]
    │
    └── 1 ────────── * [ AUDIT_LOGS ]

 [ CUSTOMERS ] 1 ────────── * [ CONVERSATIONS ] 1 ────────── * [ MESSAGES ] 1 ────────── * [ ATTACHMENTS ]

 [ AUTOMATION_RULES ] 1 ────────── * [ KEYWORDS ]
         │
         └── 1 ────────── * [ REPLIES ]

 [ DAILY_STATISTICS ]
 [ MONTHLY_STATISTICS ]
 [ ANALYTICS ]
 [ REPORTS ]
```

---

## ⚡ Non-Blocking Webhook Data Flow

```
Meta Webhook ──> GET/POST /webhook ──> WebhookSignatureValidator (HMAC-SHA256)
                                               │
                                               ▼
                                      Return 200 OK (Instant)
                                               │
                                               ▼ (Async ThreadPool)
                                  AutomationEngineService
                                               │
                               ┌───────────────┴───────────────┐
                               ▼                               ▼
                     KeywordMatcherService            BusinessHoursCheck
                               │                               │
                               └───────────────┬───────────────┘
                                               ▼
                                  WhatsAppApiClient (WebClient)
```
