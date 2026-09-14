# HRMS Pro – Render Deployment Guide

This guide details how to deploy HRMS Pro to **Render** with a cloud MySQL database.

---

## 1. Push Code to GitHub

```bash
# 1. Initialize git (if not already done)
git add .
git commit -m "Fix all issues and prepare for Render deployment"

# 2. Add your GitHub repository remote
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPO_NAME>.git

# 3. Rename branch to main (recommended) and push
git branch -M main
git push -u origin main
```

---

## 2. Setup a Cloud MySQL Database (Free Options)

Render does not offer a free managed MySQL instance directly, but you can create a free cloud MySQL database in 1 minute using any of the following:

- **[Aiven for MySQL](https://aiven.io/)** (Free tier available, 5GB storage)
- **[Clever Cloud](https://www.clever-cloud.com/)** (Free MySQL addon)
- **[TiDB Cloud](https://tidbcloud.com/)** (Free Serverless MySQL-compatible, 5GB)
- **[Railway](https://railway.app/)** (MySQL template)

### After creating the database:
1. Import the schema and seed data:
   - Run the contents of `database/seed.sql` in your cloud database console / phpMyAdmin / MySQL Workbench / DBeaver.
2. Copy your connection parameters:
   - **Host** (e.g. `mysql-xxxx.aivencloud.com`)
   - **Port** (e.g. `12345`)
   - **Database Name** (e.g. `defaultdb` or `hrms_db`)
   - **User** (e.g. `avnadmin` or `root`)
   - **Password**

---

## 3. Deploy to Render

### Option A: Using Render Blueprint (`render.yaml`) - Recommended
1. Log in to [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** -> **Blueprint**.
3. Select your GitHub repository.
4. Render will detect `render.yaml`.
5. Enter your Cloud MySQL credentials in the environment variables prompt:
   - `DB_URL`: `jdbc:mysql://<HOST>:<PORT>/<DB_NAME>?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true`
   - `DB_USERNAME`: `<YOUR_DB_USER>`
   - `DB_PASSWORD`: `<YOUR_DB_PASSWORD>`
6. Click **Apply**. Render will automatically build the Docker image and deploy!

---

### Option B: Manual Web Service Setup
1. On Render Dashboard, click **New +** -> **Web Service**.
2. Connect your GitHub repository.
3. Choose:
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `./Dockerfile`
   - **Instance Type**: `Free`
4. In the **Environment Variables** section, add:

| Key | Value |
|---|---|
| `PORT` | `8080` |
| `DB_URL` | `jdbc:mysql://<HOST>:<PORT>/<DB_NAME>?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | `<YOUR_DB_USER>` |
| `DB_PASSWORD` | `<YOUR_DB_PASSWORD>` |
| `JWT_SECRET` | `hrmsProSuperSecretKeyForJWTTokenGenerationMustBe256BitsLongAtLeast2024` |
| `FILE_UPLOAD_DIR` | `/app/uploads` |

5. Click **Create Web Service**.

---

## 4. Accessing Your App

Once Render finishes the deployment:
- Open your Render URL: `https://<your-app-name>.onrender.com`
- Log in with the default accounts:
  - **Super Admin**: `admin@hrms.com` / `Admin@123`
  - **HR Admin**: `hr@hrms.com` / `Admin@123`
  - **Manager**: `manager@hrms.com` / `Admin@123`
  - **Employee**: `employee@hrms.com` / `Admin@123`
