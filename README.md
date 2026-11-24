# VCLASS — Virtual Classroom Management System Using Java Swing

A Java Swing-based Virtual Classroom Management System developed using **Java 17, JDBC, Maven, and MySQL**. VClass provides a desktop application for managing students, faculty, courses, assignments, and submissions.

## 1. Project Overview

VClass is designed to simplify classroom management through a centralized desktop application. It includes user authentication, role-based dashboards, academic record management, and assignment submission functionality.

## 2. Technologies Used

- **Java 17** – Core programming language
- **Java Swing** – Desktop graphical user interface
- **JDBC** – Database connectivity
- **MySQL** – Database management
- **Apache Maven** – Dependency and build management
- **VS Code** – Development environment

## 3. Features

- User login and authentication
- Role-based dashboards
- Student management
- Faculty management
- Course management
- Assignment creation and management
- Student assignment submissions
- File uploads with a 25 MB size limit
- MySQL database integration
- Secure database configuration using local properties

## 4. Installation and Setup

### Prerequisites

- Java JDK 17
- Apache Maven
- MySQL Server
- Visual Studio Code (recommended)

### Step 1: Clone the Repository

```bash
git clone https://github.com/rc-exe12/VClass_.git
cd VClass_
```

### Step 2: Create the MySQL Database

Start your MySQL server and execute:

```sql
source database/schema.sql;
```

Alternatively, open `database/schema.sql` in MySQL Workbench and execute it.

The script creates the `vclass_swing_db` database and the required tables.

Demo admin credentials:

- Email: `admin@vclass.com`
- Password: `admin123`

Change the demo password before using real accounts.

For temporary role testing, execute `database/demo-accounts.sql` to create demo Faculty and Student accounts.

### Step 3: Configure JDBC

Copy `database.properties.example` to `database.properties`.

Update your local MySQL credentials:

```properties
db.username=your_username
db.password=your_password
```

The sample connection expects MySQL at `localhost:3306`.

**Important:** Never upload your actual `database.properties` file or database credentials to GitHub.

### Step 4: Build the Project

```bash
mvn clean package
```

### Step 5: Run the Application

Open `src/main/java/com/vclass/Main.java` in VS Code and run the `main` method.

Alternatively, use the configured Java run configuration.

## 5. Project Structure

```text
VClass/
├── database/
│   ├── schema.sql
│   └── demo-accounts.sql
├── src/
│   └── main/
│       └── java/
│           └── com/vclass/
│               ├── Main.java
│               ├── db/
│               ├── model/
│               ├── service/
│               ├── ui/
│               └── util/
├── database.properties.example
├── pom.xml
├── README.md
└── CODE_GUIDE.md
```

## 6. Assignment File Submission

Faculty and Admins can create assignments through the **Assignment Management** section.

Students can:
1. Open the Assignments section.
2. Select an assignment.
3. Choose a file using the file chooser.
4. Submit the assignment.

The application stores submitted files in `uploads/submissions/` and saves their relative paths in the MySQL database.

Files are limited to 25 MB. The `uploads/` directory is excluded from Git because it contains user-submitted files.

## 7. Source Code Documentation

For a detailed function-by-function explanation of the source code, refer to [CODE_GUIDE.md](CODE_GUIDE.md).

## 8. Project Team

**Developed by – Team VClass**

| S. No. | Name | Roll Number |
|---|---|---|
| 1 | Ramcharan Sai | 25R11A05FF |
| 2 | Manikanta Ram | 25R11A05FD |
| 3 | S. Navaneeth | 25R11A05EX |

## 9. Academic Details

- **Project Title:** Virtual Classroom Management System
- **Project Name:** VCLASS
- **College:** Geetanjali College of Engineering and Technology
- **Academic Year:** 2025–2026

## 10. Future Enhancements

- Improved user interface and experience
- Additional academic management features
- Enhanced assignment tracking
- Better reporting and analytics
- Cloud-based deployment

---

**VCLASS — Making Virtual Classroom Management Simpler.**
