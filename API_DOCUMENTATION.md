# OrderFlow SaaS — Complete REST API Documentation

This document provides detailed API documentation for all REST endpoints exposed by the **OrderFlow Backend** (Spring Boot 3.x).

---

## 🔐 Authentication & Headers

### 1. JWT Bearer Token (User / SaaS API Access)
```http
Authorization: Bearer <access_token>
```
Required for user profile, CRM customers, analytics, subscriptions, and settings.

### 2. API Key Authentication (System-to-System Access)
```http
X-API-Key: <your_api_key>
```
Required for backend automation endpoints when accessed by external client scripts.

---

## 📌 Standard Response Structure

All API responses follow a unified JSON wrapper format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-07-30T01:36:00.000Z"
}
```

### Error Response Structure (HTTP 4xx / 5xx)
```json
{
  "success": false,
  "message": "Detailed error message",
  "data": null,
  "timestamp": "2026-07-30T01:36:00.000Z"
}
```

---

## 🔑 Authentication Endpoints (`/api/v1/auth`)

### 1. User Login
- **Method**: `POST`
- **Path**: `/api/v1/auth/login`
- **Request Body**:
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
- **Response** (`200 OK`):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "userId": 1,
    "email": "john@example.com",
    "roles": ["ROLE_ADMIN"],
    "subscriptionStatus": "ACTIVE"
  }
}
```

### 2. Refresh JWT Token
- **Method**: `POST`
- **Path**: `/api/v1/auth/refresh`
- **Request Body**:
```json
{
  "refreshToken": "eyJhbGci..."
}
```

---

## 👥 Customer CRM Endpoints (`/api/v1/customers`)

### 1. Search & List Customers
- **Method**: `GET`
- **Path**: `/api/v1/customers?page=0&size=10`
- **Query Params**: `query` (optional name/phone/email search filter)
- **Response** (`200 OK`):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "waId": "15550001111",
        "phone": "+15550001111",
        "name": "Alex Johnson",
        "email": "alex@example.com",
        "status": "ACTIVE",
        "createdAt": "2026-07-30T00:00:00.000Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 🤖 Automation Rules Endpoints (`/api/v1/automation`)

### 1. Get Automation Rules
- **Method**: `GET`
- **Path**: `/api/v1/automation/rules`
- **Response** (`200 OK`):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Order Tracking Rule",
      "priority": 10,
      "active": true,
      "cooldownSeconds": 60,
      "keywords": [
        {
          "pattern": "track",
          "matchType": "CONTAINS",
          "ignoreCase": true
        }
      ],
      "replies": [
        {
          "messageBody": "Your order is in transit.",
          "replyOrder": 0,
          "delaySeconds": 0
        }
      ]
    }
  ]
}
```

---

## 📊 Analytics & Reports Endpoints (`/api/v1/analytics`)

### 1. Summary Metrics
- **Method**: `GET`
- **Path**: `/api/v1/analytics/summary?startDate=2026-07-01&endDate=2026-07-30`

### 2. Export CSV / Excel / PDF
- `GET /api/v1/analytics/reports/export/csv`
- `GET /api/v1/analytics/reports/export/excel`
- `GET /api/v1/analytics/reports/export/pdf`
