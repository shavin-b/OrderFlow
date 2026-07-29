# OrderFlow SaaS — Installation & Local Development Guide

This guide provides step-by-step instructions for installing and running **OrderFlow Backend** (Spring Boot 3.x) and **OrderFlow Admin** (React 19) locally for development.

---

## 📋 Prerequisites

| Tool | Version Required | Download / Command |
|------|-----------------|--------------------|
| **Java JDK** | 21+ | Eclipse Adoptium / Temurin |
| **Node.js** | 20+ | `node -v` |
| **npm** | 10+ | `npm -v` |
| **Maven** | 3.9+ | `mvn -v` |
| **MySQL** | 8.0 or 8.4 | `mysql --version` (or via Docker) |
| **Docker & Compose** | Docker 24+, Compose 2.x | `docker compose version` |

---

## 🚀 Quick Setup (Docker One-Command Launch)

To spin up MySQL, Spring Boot Backend, and React Admin Frontend in containers:

```bash
# 1. Clone repository
git clone https://github.com/your-org/orderflow.git
cd orderflow

# 2. Copy environment file
cp .env.example .env

# 3. Launch Docker Compose stack
docker-compose up --build -d
```

- **React Admin Frontend**: http://localhost:80
- **Spring Boot Backend REST API**: http://localhost:8080/api/v1
- **Swagger OpenAPI Documentation**: http://localhost:8080/api/v1/swagger-ui.html

---

## 💻 Manual Local Development Setup

### 1. Database Setup (MySQL)

Start MySQL via Docker or local installation:

```bash
docker-compose up mysql -d
```

Verify MySQL connection:
- Host: `localhost:3306`
- Database: `orderflow`
- User: `orderflow_user`
- Password: `orderflow_pass`

### 2. Spring Boot Backend Setup

```bash
# Set environment variables (PowerShell example)
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="orderflow"
$env:DB_USERNAME="orderflow_user"
$env:DB_PASSWORD="orderflow_pass"
$env:SECURITY_API_KEYS="dev-api-key-change-in-production"

# Run Maven compile & test suite
mvn clean test

# Start Backend Server
mvn spring-boot:run
```

The backend server starts on **http://localhost:8080**.

### 3. React Admin Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install Node dependencies
npm install

# Run Vite local dev server
npm run dev
```

The frontend dev server starts on **http://localhost:3000** with automatic proxying to backend on port 8080.

---

## 🧪 Testing Verification Commands

```bash
# Backend Test Suite
mvn test

# Frontend Production Build Test
cd frontend
npm run build
```
