-- ============================================================
-- HRMS Pro - Complete Database Schema
-- MySQL 8.x
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

-- ============================================================
-- DROP existing tables (for clean re-run)
-- ============================================================
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS holidays;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS leave_balances;
DROP TABLE IF EXISTS leave_types;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS employee_documents;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS designations;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS organizations;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS users;

-- ============================================================
-- AUTHENTICATION TABLES
-- ============================================================

CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    last_login  DATETIME,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,   -- SUPER_ADMIN, HR_ADMIN, MANAGER, EMPLOYEE
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,  -- e.g. employee:create
    module      VARCHAR(50) NOT NULL,
    description TEXT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE role_permissions (
    role_id        BIGINT NOT NULL,
    permission_id  BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  DATETIME NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- ORGANIZATION
-- ============================================================

CREATE TABLE organizations (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    email               VARCHAR(150),
    phone               VARCHAR(20),
    address             TEXT,
    logo_path           VARCHAR(500),
    website             VARCHAR(200),
    work_start_time     TIME NOT NULL DEFAULT '09:00:00',
    work_end_time       TIME NOT NULL DEFAULT '18:00:00',
    working_days        VARCHAR(100) NOT NULL DEFAULT 'MON,TUE,WED,THU,FRI',
    late_check_in_mins  INT NOT NULL DEFAULT 15,
    overtime_threshold_mins INT NOT NULL DEFAULT 480,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- DEPARTMENTS
-- ============================================================

CREATE TABLE departments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code   VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    manager_id  BIGINT,            -- FK to employees (added after)
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- DESIGNATIONS
-- ============================================================

CREATE TABLE designations (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    department_id BIGINT,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- ============================================================
-- EMPLOYEES
-- ============================================================

CREATE TABLE employees (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id         VARCHAR(20) NOT NULL UNIQUE,   -- e.g. EMP001
    user_id             BIGINT UNIQUE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    personal_email      VARCHAR(150),
    official_email      VARCHAR(150) NOT NULL UNIQUE,
    phone               VARCHAR(20),
    date_of_birth       DATE,
    gender              ENUM('MALE','FEMALE','OTHER'),
    address             TEXT,
    profile_photo_path  VARCHAR(500),
    department_id       BIGINT,
    designation_id      BIGINT,
    reporting_manager_id BIGINT,
    joining_date        DATE NOT NULL,
    employment_type     ENUM('FULL_TIME','PART_TIME','CONTRACT','INTERN') NOT NULL DEFAULT 'FULL_TIME',
    employment_status   ENUM('ACTIVE','INACTIVE','ON_NOTICE','TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    work_location       VARCHAR(200),
    professional_bio    TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_by          BIGINT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (designation_id) REFERENCES designations(id) ON DELETE SET NULL,
    FOREIGN KEY (reporting_manager_id) REFERENCES employees(id) ON DELETE SET NULL
);

-- Add manager FK to departments after employees table is created
ALTER TABLE departments
    ADD CONSTRAINT fk_dept_manager FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL;

-- ============================================================
-- EMPLOYEE DOCUMENTS
-- ============================================================

CREATE TABLE employee_documents (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id         BIGINT NOT NULL,
    document_name       VARCHAR(200) NOT NULL,
    document_category   ENUM(
                            'PROFILE_PHOTO','RESUME','OFFER_LETTER',
                            'APPOINTMENT_LETTER','EDUCATIONAL_CERTIFICATE',
                            'EXPERIENCE_CERTIFICATE','IDENTITY_DOCUMENT',
                            'SALARY_DOCUMENT','OTHER'
                        ) NOT NULL DEFAULT 'OTHER',
    original_file_name  VARCHAR(255) NOT NULL,
    stored_file_name    VARCHAR(255) NOT NULL UNIQUE,
    file_type           VARCHAR(50) NOT NULL,   -- pdf, jpg, jpeg, png
    file_size           BIGINT NOT NULL,         -- bytes
    file_path           VARCHAR(500) NOT NULL,
    uploaded_by         BIGINT NOT NULL,
    uploaded_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

-- ============================================================
-- ATTENDANCE
-- ============================================================

CREATE TABLE attendance (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id             BIGINT NOT NULL,
    attendance_date         DATE NOT NULL,
    check_in_time           DATETIME,
    check_out_time          DATETIME,
    total_working_minutes   INT DEFAULT 0,
    overtime_minutes        INT DEFAULT 0,
    status                  ENUM('PRESENT','ABSENT','HALF_DAY','LATE','ON_LEAVE') NOT NULL DEFAULT 'ABSENT',
    notes                   TEXT,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_employee_date (employee_id, attendance_date),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- ============================================================
-- LEAVE MANAGEMENT
-- ============================================================

CREATE TABLE leave_types (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    code            VARCHAR(20) NOT NULL UNIQUE,
    description     TEXT,
    max_days_per_year INT NOT NULL DEFAULT 0,
    is_paid         BOOLEAN NOT NULL DEFAULT TRUE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leave_balances (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id     BIGINT NOT NULL,
    leave_type_id   BIGINT NOT NULL,
    year            INT NOT NULL,
    total_days      DECIMAL(5,1) NOT NULL DEFAULT 0,
    used_days       DECIMAL(5,1) NOT NULL DEFAULT 0,
    pending_days    DECIMAL(5,1) NOT NULL DEFAULT 0,
    remaining_days  DECIMAL(5,1) GENERATED ALWAYS AS (total_days - used_days - pending_days) STORED,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_emp_leave_year (employee_id, leave_type_id, year),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE CASCADE
);

CREATE TABLE leave_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id         BIGINT NOT NULL,
    leave_type_id       BIGINT NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    total_days          DECIMAL(5,1) NOT NULL,
    is_half_day         BOOLEAN NOT NULL DEFAULT FALSE,
    half_day_type       ENUM('MORNING','AFTERNOON'),
    reason              TEXT NOT NULL,
    status              ENUM('PENDING','MANAGER_APPROVED','HR_APPROVED','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    manager_id          BIGINT,
    manager_action_at   DATETIME,
    manager_comment     TEXT,
    hr_id               BIGINT,
    hr_action_at        DATETIME,
    hr_comment          TEXT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(id),
    FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL,
    FOREIGN KEY (hr_id) REFERENCES employees(id) ON DELETE SET NULL
);

-- ============================================================
-- ANNOUNCEMENTS
-- ============================================================

CREATE TABLE announcements (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     TEXT NOT NULL,
    priority    ENUM('LOW','MEDIUM','HIGH','URGENT') NOT NULL DEFAULT 'MEDIUM',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at DATETIME,
    created_by  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================

CREATE TABLE notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     TEXT NOT NULL,
    type        ENUM('LEAVE_APPROVED','LEAVE_REJECTED','LEAVE_PENDING',
                     'ANNOUNCEMENT','DOCUMENT_UPLOADED','DOCUMENT_UPDATED',
                     'EMPLOYEE_ONBOARDING','GENERAL') NOT NULL DEFAULT 'GENERAL',
    reference_id BIGINT,           -- related entity ID (leave_request_id, doc_id, etc.)
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- HOLIDAYS
-- ============================================================

CREATE TABLE holidays (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    holiday_date    DATE NOT NULL,
    type            ENUM('PUBLIC','OPTIONAL','RESTRICTED') NOT NULL DEFAULT 'PUBLIC',
    description     TEXT,
    year            INT GENERATED ALWAYS AS (YEAR(holiday_date)) STORED,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_holiday_date_name (holiday_date, name)
);

-- ============================================================
-- AUDIT LOGS
-- ============================================================

CREATE TABLE audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT,
    user_email  VARCHAR(150),
    action      VARCHAR(100) NOT NULL,    -- e.g. EMPLOYEE_CREATED
    module      VARCHAR(50) NOT NULL,     -- e.g. EMPLOYEE
    description TEXT,
    entity_id   BIGINT,
    entity_type VARCHAR(50),
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_employees_dept ON employees(department_id);
CREATE INDEX idx_employees_status ON employees(employment_status);
CREATE INDEX idx_employees_manager ON employees(reporting_manager_id);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);
CREATE INDEX idx_attendance_emp_date ON attendance(employee_id, attendance_date);
CREATE INDEX idx_leave_req_status ON leave_requests(status);
CREATE INDEX idx_leave_req_employee ON leave_requests(employee_id);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_module ON audit_logs(module);
CREATE INDEX idx_announcements_published ON announcements(is_published, published_at);

SET FOREIGN_KEY_CHECKS = 1;
