# OrderFlow SaaS — Operational Troubleshooting Guide

This guide provides diagnostic procedures and resolution steps for common deployment or runtime issues.

---

## 🔍 Diagnostics Checklist

| Symptom | Probable Cause | Resolution |
|---------|----------------|------------|
| **HTTP 401 Unauthorized** | Missing or invalid `X-API-Key` or expired JWT token | Verify `X-API-Key` header or login again via `/api/v1/auth/login` |
| **HTTP 402 Payment Required** | Subscription plan is `EXPIRED` or `SUSPENDED` | Upgrade subscription plan via `/api/v1/subscriptions/upgrade` |
| **HTTP 403 Forbidden** | User role does not have authority for requested resource | Ensure user account has `ROLE_ADMIN` or `ROLE_MANAGER` authority |
| **Webhook Validation Failure** | `WHATSAPP_VERIFY_TOKEN` mismatch or invalid HMAC signature | Verify `WHATSAPP_VERIFY_TOKEN` and `WHATSAPP_APP_SECRET` in `.env` |
| **Database Connection Refused** | MySQL container starting or incorrect `DB_HOST` | Ensure MySQL is healthy (`docker-compose ps`) |

---

## 🪵 Log Inspection Commands

```bash
# Backend Application Logs
docker-compose logs -f backend

# Frontend Nginx Access Logs
docker-compose logs -f frontend

# MySQL Logs
docker-compose logs -f mysql
```
