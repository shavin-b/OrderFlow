# OrderFlow SaaS — Testing & Quality Assurance Guide

This guide describes how to run automated unit tests, integration tests, and build verifications for both backend and frontend applications.

---

## 🧪 Spring Boot Backend Testing

### 1. Run Complete Test Suite
```bash
mvn clean test
```

### 2. Run Specific Test Class
```bash
# Run AutomationEngineServiceTest
mvn test -Dtest=AutomationEngineServiceTest

# Run Controller Integration Tests
mvn test -Dtest=AnalyticsControllerTest
```

### 3. Test Classes Summary
- `AuthServiceTest`: JWT token creation, password hashing, and user authentication tests.
- `AutomationEngineServiceTest`: Keyword matching, rule priority evaluation, multi-reply sequence delays, and business hours tests.
- `KeywordMatcherServiceTest`: Tests for 5 match types (`CONTAINS`, `EXACT`, `STARTS_WITH`, `ENDS_WITH`, `REGEX`).
- `AnalyticsControllerTest`: Tests for analytics summary APIs and CSV/Excel/PDF export endpoints.

---

## 🎨 React Admin Frontend Testing & Build

```bash
# 1. Navigate to frontend directory
cd frontend

# 2. Run Vite Production Build Test
npm run build
```
