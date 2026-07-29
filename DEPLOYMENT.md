# OrderFlow SaaS — Enterprise Production Deployment Guide

This guide covers deploying OrderFlow to production with Docker, Nginx, HTTPS/SSL certificates, health checks, rate limiting, and environment variable configuration.

---

## 🏗️ Production Architecture

```
                       [ Internet Users / Webhook ]
                                    │
                                    ▼
                         [ Nginx Reverse Proxy ]
                         (HTTPS :443 / SSL TLS 1.3)
                                    │
               ┌────────────────────┴────────────────────┐
               ▼                                         ▼
   [ React Admin SPA Container ]             [ Spring Boot Backend Container ]
      (Port 80 / Static HTML)                     (Java 21 / Port 8080)
                                                         │
                                                         ▼
                                               [ MySQL 8.4 Container ]
                                               (Persistent Storage)
```

---

## 🔐 Environment Variables (`.env`)

Create a production `.env` file on your cloud server:

```dotenv
# MySQL Credentials
MYSQL_ROOT_PASSWORD=SuperStrongRootPassword2026!
DB_NAME=orderflow_prod
DB_USERNAME=orderflow_app
DB_PASSWORD=SecureAppDbPassword2026!
DB_PORT=3306

# WhatsApp Cloud API Credentials
WHATSAPP_ACCESS_TOKEN=EAAG...YOUR_PERMANENT_META_ACCESS_TOKEN
WHATSAPP_PHONE_NUMBER_ID=389472910482103
WHATSAPP_VERIFY_TOKEN=your_custom_webhook_verify_token
WHATSAPP_APP_SECRET=your_meta_app_secret_for_hmac_validation

# Security & Authentication
SECURITY_API_KEYS=prod-secret-api-key-1,prod-secret-api-key-2
JWT_SECRET=super_secret_jwt_key_minimum_256_bits_length_for_hmac_sha512
SPRING_PROFILES_ACTIVE=production
```

---

## 🔒 SSL Certificate Setup (Let's Encrypt / Certbot)

Install Certbot and request SSL certificates for your production domain:

```bash
sudo apt update && sudo apt install -y certbot
sudo certbot certonly --standalone -d orderflow.yourdomain.com
```

Certificates will be generated at `/etc/letsencrypt/live/orderflow.yourdomain.com/`.

---

## 🚀 Launching Production Container Stack

```bash
# 1. Pull / Build containers in detached mode
docker-compose up --build -d

# 2. Verify running services
docker-compose ps

# 3. Inspect application logs
docker-compose logs -f backend
```

---

## 🩺 Health Check & Monitoring Verification

```bash
# Backend Actuator Health Check
curl https://orderflow.yourdomain.com/api/v1/management/health

# Frontend Nginx Health Check
curl -I https://orderflow.yourdomain.com/
```
