-- ============================================================
-- HRMS Pro – Seed Data
-- Run AFTER schema.sql
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- ORGANIZATION
-- ============================================================
INSERT INTO organizations (name, email, phone, address, work_start_time, work_end_time, working_days, late_check_in_mins, overtime_threshold_mins)
VALUES ('HRMS Pro Corporation', 'info@hrmspro.com', '+91-9999999999',
        '123 Business Park, Chennai, Tamil Nadu, India - 600001',
        '09:00:00', '18:00:00', 'MON,TUE,WED,THU,FRI', 15, 480);

-- ============================================================
-- ROLES
-- ============================================================
INSERT INTO roles (name, display_name, description) VALUES
('SUPER_ADMIN', 'Super Admin', 'Full system access with all permissions'),
('HR_ADMIN',    'HR Admin',    'HR operations: employee management, documents, attendance, leave'),
('MANAGER',     'Manager',     'Team management: view team, approve leave'),
('EMPLOYEE',    'Employee',    'Self-service: profile, attendance, leave, directory');

-- ============================================================
-- PERMISSIONS
-- ============================================================
INSERT INTO permissions (name, module, description) VALUES
-- Employee
('employee:create',     'EMPLOYEE', 'Create new employees'),
('employee:read',       'EMPLOYEE', 'View employee details'),
('employee:update',     'EMPLOYEE', 'Update employee information'),
('employee:delete',     'EMPLOYEE', 'Deactivate / delete employees'),
('employee:read_all',   'EMPLOYEE', 'View all employees'),
-- Department
('department:create',   'DEPARTMENT', 'Create departments'),
('department:read',     'DEPARTMENT', 'View departments'),
('department:update',   'DEPARTMENT', 'Update departments'),
('department:delete',   'DEPARTMENT', 'Delete departments'),
-- Designation
('designation:create',  'DESIGNATION', 'Create designations'),
('designation:read',    'DESIGNATION', 'View designations'),
('designation:update',  'DESIGNATION', 'Update designations'),
('designation:delete',  'DESIGNATION', 'Delete designations'),
-- Document
('document:upload',     'DOCUMENT', 'Upload documents'),
('document:read_own',   'DOCUMENT', 'View own documents'),
('document:read_all',   'DOCUMENT', 'View all employee documents'),
('document:delete_own', 'DOCUMENT', 'Delete own documents'),
('document:delete_all', 'DOCUMENT', 'Delete any employee document'),
-- Attendance
('attendance:check_in',  'ATTENDANCE', 'Check in'),
('attendance:check_out', 'ATTENDANCE', 'Check out'),
('attendance:read_own',  'ATTENDANCE', 'View own attendance'),
('attendance:read_all',  'ATTENDANCE', 'View all attendance'),
('attendance:update',    'ATTENDANCE', 'Update attendance records'),
-- Leave
('leave:apply',          'LEAVE', 'Apply for leave'),
('leave:read_own',       'LEAVE', 'View own leave requests'),
('leave:read_all',       'LEAVE', 'View all leave requests'),
('leave:approve_manager','LEAVE', 'Approve/reject leave as manager'),
('leave:approve_hr',     'LEAVE', 'Final HR approval/rejection'),
('leave:cancel',         'LEAVE', 'Cancel own leave request'),
-- Announcement
('announcement:create',  'ANNOUNCEMENT', 'Create announcements'),
('announcement:read',    'ANNOUNCEMENT', 'View announcements'),
('announcement:update',  'ANNOUNCEMENT', 'Edit announcements'),
('announcement:delete',  'ANNOUNCEMENT', 'Delete announcements'),
-- Report
('report:view',          'REPORT', 'View reports and analytics'),
-- Audit
('audit:read',           'AUDIT', 'View audit logs'),
-- Settings
('settings:manage',      'SETTINGS', 'Manage organization settings'),
-- User management
('user:manage',          'USER', 'Manage users and roles'),
-- Directory
('directory:read',       'DIRECTORY', 'View employee directory');

-- ============================================================
-- ROLE_PERMISSIONS (Super Admin gets ALL)
-- ============================================================

-- SUPER_ADMIN - all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- HR_ADMIN permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name IN (
    'employee:create','employee:read','employee:update','employee:read_all',
    'department:create','department:read','department:update',
    'designation:create','designation:read','designation:update',
    'document:upload','document:read_own','document:read_all','document:delete_all',
    'attendance:read_all','attendance:update',
    'leave:read_all','leave:approve_hr',
    'announcement:create','announcement:read','announcement:update','announcement:delete',
    'report:view',
    'directory:read'
);

-- MANAGER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE name IN (
    'employee:read','employee:read_all',
    'department:read',
    'designation:read',
    'attendance:read_all',
    'leave:read_all','leave:approve_manager',
    'announcement:read',
    'directory:read'
);

-- EMPLOYEE permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions WHERE name IN (
    'employee:read',
    'document:upload','document:read_own','document:delete_own',
    'attendance:check_in','attendance:check_out','attendance:read_own',
    'leave:apply','leave:read_own','leave:cancel',
    'announcement:read',
    'directory:read'
);

-- ============================================================
-- DEPARTMENTS
-- ============================================================
INSERT INTO departments (dept_code, name, description, is_active) VALUES
('HR',   'Human Resources', 'HR department managing people operations', TRUE),
('ENG',  'Engineering',     'Software development and technical operations', TRUE),
('FIN',  'Finance',         'Financial operations and accounting', TRUE),
('MKT',  'Marketing',       'Brand, marketing, and growth', TRUE),
('SLS',  'Sales',           'Sales and business development', TRUE),
('OPS',  'Operations',      'Operational excellence and processes', TRUE);

-- ============================================================
-- DESIGNATIONS
-- ============================================================
INSERT INTO designations (name, department_id, is_active) VALUES
('HR Director',       1, TRUE),
('HR Executive',      1, TRUE),
('HR Manager',        1, TRUE),
('Software Engineer', 2, TRUE),
('Senior Software Engineer', 2, TRUE),
('Tech Lead',         2, TRUE),
('Engineering Manager', 2, TRUE),
('Finance Manager',   3, TRUE),
('Accountant',        3, TRUE),
('Marketing Manager', 4, TRUE),
('Marketing Executive', 4, TRUE),
('Sales Manager',     5, TRUE),
('Sales Executive',   5, TRUE),
('Operations Manager', 6, TRUE),
('Team Lead',         2, TRUE);

-- ============================================================
-- USERS (password is BCrypt of the plaintext shown)
-- Admin@123   → $2a$10$...
-- All passwords use BCrypt strength 10
-- Admin@123
-- ============================================================
INSERT INTO users (username, email, password, is_active) VALUES
('superadmin',  'admin@hrms.com',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIom', TRUE),
('hradmin',     'hr@hrms.com',       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIom', TRUE),
('jsmith',      'manager@hrms.com',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIom', TRUE),
('alee',        'employee@hrms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIom', TRUE);

-- user_roles
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1), (2, 2), (3, 3), (4, 4);

-- ============================================================
-- EMPLOYEES
-- ============================================================
INSERT INTO employees (employee_id, user_id, first_name, last_name, official_email, phone,
                       date_of_birth, gender, department_id, designation_id, joining_date,
                       employment_type, employment_status, work_location, is_active, created_by)
VALUES
('EMP001', 1, 'System',  'Administrator', 'admin@hrms.com',    '+91-9000000001',
 '1985-01-15', 'MALE', 1, 1, '2020-01-01', 'FULL_TIME', 'ACTIVE', 'HQ - Chennai', TRUE, 1),

('EMP002', 2, 'Priya',   'Sharma',        'hr@hrms.com',       '+91-9000000002',
 '1990-05-20', 'FEMALE', 1, 2, '2021-03-01', 'FULL_TIME', 'ACTIVE', 'HQ - Chennai', TRUE, 1),

('EMP003', 3, 'John',    'Smith',         'manager@hrms.com',  '+91-9000000003',
 '1988-08-10', 'MALE', 2, 7, '2020-06-15', 'FULL_TIME', 'ACTIVE', 'HQ - Chennai', TRUE, 1),

('EMP004', 4, 'Ananya',  'Lee',           'employee@hrms.com', '+91-9000000004',
 '1995-11-25', 'FEMALE', 2, 4, '2023-01-10', 'FULL_TIME', 'ACTIVE', 'HQ - Chennai', TRUE, 1);

-- Set reporting managers
UPDATE employees SET reporting_manager_id = 1 WHERE id = 2;
UPDATE employees SET reporting_manager_id = 1 WHERE id = 3;
UPDATE employees SET reporting_manager_id = 3 WHERE id = 4;

-- Set dept managers
UPDATE departments SET manager_id = 2 WHERE dept_code = 'HR';
UPDATE departments SET manager_id = 3 WHERE dept_code = 'ENG';

-- ============================================================
-- LEAVE TYPES
-- ============================================================
INSERT INTO leave_types (name, code, description, max_days_per_year, is_paid, is_active) VALUES
('Casual Leave',  'CL',  'General purpose casual leave',  12, TRUE,  TRUE),
('Sick Leave',    'SL',  'Medical / health related leave', 10, TRUE,  TRUE),
('Paid Leave',    'PL',  'Annual paid leave',             15, TRUE,  TRUE),
('Unpaid Leave',  'UL',  'Leave without pay',              0, FALSE, TRUE);

-- ============================================================
-- LEAVE BALANCES (current year)
-- ============================================================
INSERT INTO leave_balances (employee_id, leave_type_id, year, total_days, used_days, pending_days)
VALUES
(2, 1, YEAR(CURDATE()), 12, 2, 0),
(2, 2, YEAR(CURDATE()), 10, 1, 0),
(2, 3, YEAR(CURDATE()), 15, 5, 0),
(3, 1, YEAR(CURDATE()), 12, 0, 0),
(3, 2, YEAR(CURDATE()), 10, 0, 0),
(3, 3, YEAR(CURDATE()), 15, 3, 0),
(4, 1, YEAR(CURDATE()), 12, 1, 0),
(4, 2, YEAR(CURDATE()), 10, 0, 0),
(4, 3, YEAR(CURDATE()), 15, 0, 0);

-- ============================================================
-- ANNOUNCEMENTS
-- ============================================================
INSERT INTO announcements (title, content, priority, is_published, published_at, created_by)
VALUES
('Welcome to HRMS Pro!',
 'We are excited to launch our new HR Management System. Please explore all the features and reach out to HR for any queries.',
 'HIGH', TRUE, NOW(), 1),

('Company Holidays 2026',
 'The company holiday calendar for 2026 has been published. Please check the Holidays section for the complete list.',
 'MEDIUM', TRUE, NOW(), 2),

('New Leave Policy Effective Q3 2026',
 'Please be informed that the updated leave policy is now in effect from Q3 2026. Key changes include enhanced maternity leave and flexible work-from-home options.',
 'URGENT', TRUE, NOW(), 2);

-- ============================================================
-- HOLIDAYS
-- ============================================================
INSERT INTO holidays (name, holiday_date, type, description) VALUES
('New Year',                 '2026-01-01', 'PUBLIC',   'New Year Day'),
('Republic Day',             '2026-01-26', 'PUBLIC',   'Republic Day of India'),
('Holi',                     '2026-03-20', 'PUBLIC',   'Festival of Colors'),
('Good Friday',              '2026-04-03', 'PUBLIC',   'Good Friday'),
('Ambedkar Jayanti',         '2026-04-14', 'PUBLIC',   'Dr. B.R. Ambedkar Jayanti'),
('Independence Day',         '2026-08-15', 'PUBLIC',   'Independence Day of India'),
('Gandhi Jayanti',           '2026-10-02', 'PUBLIC',   'Mahatma Gandhi Jayanti'),
('Diwali',                   '2026-11-08', 'PUBLIC',   'Festival of Lights'),
('Christmas',                '2026-12-25', 'PUBLIC',   'Christmas Day');

-- ============================================================
-- SAMPLE ATTENDANCE (last 5 days for employees)
-- ============================================================
INSERT INTO attendance (employee_id, attendance_date, check_in_time, check_out_time, total_working_minutes, status)
VALUES
(2, CURDATE() - INTERVAL 4 DAY, CONCAT(CURDATE() - INTERVAL 4 DAY,' 09:05:00'), CONCAT(CURDATE() - INTERVAL 4 DAY,' 18:10:00'), 545, 'PRESENT'),
(2, CURDATE() - INTERVAL 3 DAY, CONCAT(CURDATE() - INTERVAL 3 DAY,' 09:00:00'), CONCAT(CURDATE() - INTERVAL 3 DAY,' 18:00:00'), 480, 'PRESENT'),
(2, CURDATE() - INTERVAL 2 DAY, CONCAT(CURDATE() - INTERVAL 2 DAY,' 09:30:00'), CONCAT(CURDATE() - INTERVAL 2 DAY,' 18:30:00'), 480, 'LATE'),
(3, CURDATE() - INTERVAL 4 DAY, CONCAT(CURDATE() - INTERVAL 4 DAY,' 08:55:00'), CONCAT(CURDATE() - INTERVAL 4 DAY,' 18:00:00'), 485, 'PRESENT'),
(3, CURDATE() - INTERVAL 3 DAY, CONCAT(CURDATE() - INTERVAL 3 DAY,' 09:00:00'), CONCAT(CURDATE() - INTERVAL 3 DAY,' 18:00:00'), 480, 'PRESENT'),
(4, CURDATE() - INTERVAL 4 DAY, CONCAT(CURDATE() - INTERVAL 4 DAY,' 09:10:00'), CONCAT(CURDATE() - INTERVAL 4 DAY,' 18:00:00'), 470, 'PRESENT'),
(4, CURDATE() - INTERVAL 3 DAY, CONCAT(CURDATE() - INTERVAL 3 DAY,' 09:05:00'), CONCAT(CURDATE() - INTERVAL 3 DAY,' 18:05:00'), 480, 'PRESENT');

-- ============================================================
-- NOTIFICATIONS
-- ============================================================
INSERT INTO notifications (user_id, title, message, type, is_read) VALUES
(4, 'Welcome to HRMS Pro!', 'Your account has been created. Please complete your profile.', 'EMPLOYEE_ONBOARDING', FALSE),
(2, 'New Announcement', 'A new announcement "Welcome to HRMS Pro!" has been published.', 'ANNOUNCEMENT', FALSE),
(3, 'New Announcement', 'A new announcement "Welcome to HRMS Pro!" has been published.', 'ANNOUNCEMENT', FALSE);

SET FOREIGN_KEY_CHECKS = 1;
