-- Run this once in MySQL Workbench to create sample Faculty and Student logins.
USE vclass_swing_db;

INSERT INTO users (full_name, email, password_hash, role, is_active)
VALUES
  ('VCLASS Demo Faculty', 'faculty@vclass.com', SHA2('teacher123', 256), 'FACULTY', TRUE),
  ('VCLASS Demo Student', 'student@vclass.com', SHA2('student123', 256), 'STUDENT', TRUE)
ON DUPLICATE KEY UPDATE
  full_name = VALUES(full_name),
  password_hash = VALUES(password_hash),
  role = VALUES(role),
  is_active = TRUE;

INSERT INTO faculty (user_id, employee_number, department)
SELECT user_id, 'FAC-001', 'Computer Science'
FROM users WHERE email = 'faculty@vclass.com'
ON DUPLICATE KEY UPDATE department = 'Computer Science';

INSERT INTO students (user_id, student_number, department, semester)
SELECT user_id, 'STU-001', 'Computer Science', 1
FROM users WHERE email = 'student@vclass.com'
ON DUPLICATE KEY UPDATE department = 'Computer Science', semester = 1;
