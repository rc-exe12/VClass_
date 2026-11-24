CREATE DATABASE IF NOT EXISTS vclass_swing_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE vclass_swing_db;

CREATE TABLE IF NOT EXISTS users (
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash CHAR(64) NOT NULL,
  role ENUM('ADMIN','FACULTY','STUDENT') NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
  student_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL UNIQUE,
  student_number VARCHAR(30) NOT NULL UNIQUE,
  department VARCHAR(100),
  semester TINYINT UNSIGNED,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS faculty (
  faculty_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL UNIQUE,
  employee_number VARCHAR(30) NOT NULL UNIQUE,
  department VARCHAR(100),
  FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS courses (
  course_id INT PRIMARY KEY AUTO_INCREMENT,
  course_code VARCHAR(20) NOT NULL UNIQUE,
  course_name VARCHAR(120) NOT NULL,
  description TEXT,
  faculty_id INT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)
    ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
  enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  course_id INT NOT NULL,
  enrolled_on DATE NOT NULL DEFAULT (CURRENT_DATE),
  status ENUM('ACTIVE','COMPLETED','DROPPED') NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_enrollment_student_course (student_id, course_id),
  FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (course_id) REFERENCES courses(course_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS assignments (
  assignment_id INT PRIMARY KEY AUTO_INCREMENT,
  course_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  due_at DATETIME NOT NULL,
  max_marks DECIMAL(7,2) NOT NULL DEFAULT 100.00,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (course_id) REFERENCES courses(course_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
  attendance_id INT PRIMARY KEY AUTO_INCREMENT,
  student_id INT NOT NULL,
  course_id INT NOT NULL,
  attendance_date DATE NOT NULL,
  status ENUM('PRESENT','ABSENT','LATE') NOT NULL,
  remarks VARCHAR(255),
  UNIQUE KEY uq_attendance_student_course_date (student_id, course_id, attendance_date),
  FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (course_id) REFERENCES courses(course_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS submissions (
  submission_id INT PRIMARY KEY AUTO_INCREMENT,
  assignment_id INT NOT NULL,
  student_id INT NOT NULL,
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  file_path VARCHAR(500),
  comments TEXT,
  marks_awarded DECIMAL(7,2),
  feedback TEXT,
  status ENUM('SUBMITTED','GRADED','LATE') NOT NULL DEFAULT 'SUBMITTED',
  UNIQUE KEY uq_submission_assignment_student (assignment_id, student_id),
  FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (student_id) REFERENCES students(student_id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

-- Demo account for the first login. Change this password after setup.
INSERT INTO users (full_name, email, password_hash, role)
VALUES ('VCLASS Administrator', 'admin@vclass.com', SHA2('admin123', 256), 'ADMIN')
ON DUPLICATE KEY UPDATE full_name = 'VCLASS Administrator';
