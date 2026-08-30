# HRMS Pro – Enterprise Human Resource Management System

A full-stack, enterprise-grade, production-ready HR Management System built with **HTML5/CSS3/Vanilla JS (Frontend)**, **Java 17 & Spring Boot 3.x (Backend)**, and **MySQL 8.x (Database)** with **JWT Authentication** and **Role-Based Access Control (RBAC)**.

---

## 🌟 Architecture & Highlights

```
┌─────────────────────────────────────────────────────────────┐
│                    HRMS Pro Web Portal                      │
│      (Responsive Vanilla JS SPA • Google Inter Font)        │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTPS / JSON REST APIs / JWT
┌──────────────────────────────▼──────────────────────────────┐
│                  Spring Boot 3.x REST Backend               │
│   ├── Security & JWT Filter (Spring Security 6)             │
│   ├── Controllers, DTOs & Validation Layer                  │
│   ├── Business Services (Workflow, Working Hours, Overtime) │
│   └── Local / Cloud-Ready Document Storage Engine           │
└──────────────────────────────┬──────────────────────────────┘
                               │ Spring Data JPA / Hibernate
┌──────────────────────────────▼──────────────────────────────┐
│                    MySQL 8 Relational Database              │
│       (15+ Normalized Tables with Cascades & Indexes)       │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔐 Default Demo Accounts

All pre-seeded test accounts use the password: `Admin@123`

| Role | Email | Password | Allowed Scope |
|---|---|---|---|
| **Super Admin** | `admin@hrms.com` | `Admin@123` | Full enterprise control, settings, audit trail, user permissions |
| **HR Admin** | `hr@hrms.com` | `Admin@123` | Employee onboarding, documents, leave approvals, reports |
| **Manager** | `manager@hrms.com` | `Admin@123` | Team dashboard, team attendance, manager-tier leave approvals |
| **Employee** | `employee@hrms.com` | `Admin@123` | Self check-in/out, leave apply, document vault, directory |

---

## 🚀 Local Development Setup

### 1. Database Setup
1. Install MySQL 8.x and start your MySQL server.
2. Create database:
   ```sql
   CREATE DATABASE hrms_db;
   ```
3. Run the schema and seed scripts:
   ```bash
   mysql -u root -p hrms_db < database/schema.sql
   mysql -u root -p hrms_db < database/seed.sql
   ```

### 2. Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build and run the Spring Boot application:
   ```bash
   mvn clean spring-boot:run
   ```
   *The backend REST API server will start on `http://localhost:8080`.*

### 3. Frontend Setup
1. Serve the `frontend/` folder using any static server:
   ```bash
   npx serve frontend
   # or with Python
   python -m http.server 3000 --directory frontend
   # or use Live Server in VS Code
   ```
2. Open `http://localhost:3000` in your browser.

---

## ☁️ Deployment Guide (Render & GitHub)

### Step 1: Initialize Git and Push to GitHub
```bash
git init
git add .
git commit -m "feat: complete production-level HRMS Pro system"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo-name>.git
git push -u origin main
```

### Step 2: Deploy to Render via Blueprint (1-Click)
1. Log in to [Render](https://render.com).
2. Click **New +** → **Blueprint**.
3. Connect your GitHub repository.
4. Render will read `render.yaml` and automatically provision:
   - **`hrms-pro-db`**: Managed MySQL Database.
   - **`hrms-pro-backend`**: Dockerized Spring Boot Web Service.
   - **`hrms-pro-frontend`**: Static Site hosting the frontend.
5. In your Render Dashboard, run the database seed queries from `database/schema.sql` and `database/seed.sql` on the provisioned MySQL instance.

---

## 📁 Repository Structure

```
HR/
├── backend/
│   ├── src/main/java/com/hrms/
│   │   ├── config/             # Spring Security, CORS
│   │   ├── controller/         # REST Controllers
│   │   ├── dto/                # DTOs & Validation
│   │   ├── exception/          # Global Exception Handler
│   │   ├── model/              # JPA Entities
│   │   ├── repository/         # Spring Data JPA Repositories
│   │   ├── security/           # JWT Utility & Filters
│   │   ├── service/            # Business Logic & Workflows
│   │   └── HrmsApplication.java
│   ├── src/main/resources/     # application.properties
│   ├── Dockerfile              # Multi-stage Docker Container
│   └── pom.xml
├── frontend/
│   ├── css/                    # Design System & Page Styles
│   ├── js/                     # API Layer, Auth & Utilities
│   ├── index.html              # Authentication & Quick-Login
│   ├── dashboard.html          # Role-Based Dashboard
│   ├── employees.html          # Employee Directory & CRUD
│   ├── employee-profile.html   # 6-Tab Profile with Document Vault
│   ├── directory.html          # Privacy-Preserving Public Directory
│   ├── attendance.html         # Live Punch Clock & Monthly Logs
│   ├── leave.html              # Multi-Tier Approval Workflow
│   ├── documents.html          # Secure File Repository
│   ├── departments.html        # Department & Org Unit Management
│   ├── announcements.html      # Broadcast Center
│   ├── notifications.html      # In-App Notification Center
│   ├── reports.html            # Analytics & Distribution
│   ├── settings.html           # Shift & Grace Period Configuration
│   └── audit-logs.html         # Compliance Audit Trail
├── database/
│   ├── schema.sql              # Normalized MySQL Schema
│   └── seed.sql                # Pre-populated Default Data
├── .github/workflows/          # CI/CD Deployment Pipeline
├── render.yaml                 # 1-Click Render Cloud Specification
└── README.md
```
