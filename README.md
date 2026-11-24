# VCLASS — Virtual Classroom Management System Using Java Swing

This starter contains a Swing login, MySQL JDBC configuration, and a separate connection check. The application uses Java 17, Swing, JDBC, and MySQL. Maven only downloads the MySQL JDBC driver and compiles the project; there is no Spring Boot or web framework.

## 1. Open in VS Code

Install the **Extension Pack for Java** and **Maven for Java**, then use **File → Open Folder** to open this project. Accept the Java project import prompt. Java 17 and Maven are available in this environment.

## 2. Create the MySQL database

Start your MySQL server. Open `database/schema.sql` in MySQL Workbench and execute it, or run this from PowerShell:

```powershell
mysql -u root -p -e "source database/schema.sql"
```

The script creates `vclass_swing_db`, the `users`, `students`, `faculty`, `courses`, `enrollments`, `assignments`, `attendance`, and `submissions` tables, and demo admin credentials: **admin@vclass.com** / **admin123**. Change the demo password before using real accounts.

## 3. Configure JDBC

Copy `database.properties.example` to `database.properties` in the project root. Set `db.username` and `db.password` for your local MySQL account. The local config is ignored by Git. The sample URL expects MySQL at `localhost:3306`.

## 4. Check the database connection

Open `src/main/java/com/vclass/util/DatabaseConnectionTest.java` and click **Run** above `main`, or select **Test MySQL Connection** in Run and Debug. Or run:

```powershell
mvn compile
mvn exec:java -Dexec.mainClass="com.vclass.util.DatabaseConnectionTest"
```

The check prints the MySQL server version and selected database. If Maven's `exec:java` goal is not available, use the VS Code Java run configuration.

## 5. Launch login

Open `src/main/java/com/vclass/Main.java` and click **Run** above `main`, or choose **VCLASS Login** in Run and Debug. Sign in with the demo credentials. The login uses JDBC `PreparedStatement` and checks the SHA-256 hash stored in MySQL. On success it shows the user's name and role; the next step is the role dashboard using `CardLayout`.

## Source layout

- `src/main/java/com/vclass/Main.java` — application entry point
- `src/main/java/com/vclass/db/DBConnection.java` — reads JDBC settings
- `src/main/java/com/vclass/model/User.java` — signed-in user model
- `src/main/java/com/vclass/service/AuthService.java` — prepared login query
- `src/main/java/com/vclass/ui/LoginFrame.java` — grayscale Swing login screen
- `src/main/java/com/vclass/ui/DashboardFrame.java` — role-based dashboard with CardLayout
- `src/main/java/com/vclass/ui/StudentManagementPanel.java` — Admin student list and account actions
- `src/main/java/com/vclass/ui/FacultyManagementPanel.java` — Admin faculty list and account actions
- `src/main/java/com/vclass/model/FacultyRecord.java` — faculty row data
- `src/main/java/com/vclass/service/FacultyRepository.java` — JDBC faculty queries and transactions
- `src/main/java/com/vclass/service/StudentRepository.java` — JDBC student queries and transactions
- `src/main/java/com/vclass/util/DatabaseConnectionTest.java` — connection check
- `src/main/java/com/vclass/util/PasswordUtil.java` — SHA-256 helper
- `database/schema.sql` — database and demo account setup



For temporary role testing, run database/demo-accounts.sql once in Workbench. It creates one demo Faculty and Student login; these sample accounts are for classroom testing only.





- `src/main/java/com/vclass/ui/CourseManagementPanel.java` — Admin course list and account actions
- `src/main/java/com/vclass/model/CourseRecord.java` — course row data
- `src/main/java/com/vclass/service/CourseRepository.java` — JDBC course queries and transactions
- `src/main/java/com/vclass/ui/AssignmentManagementPanel.java` — staff assignment creation and management
- `src/main/java/com/vclass/ui/StudentAssignmentsPanel.java` — student assignment list and file submission
- `src/main/java/com/vclass/service/AssignmentRepository.java` — JDBC assignment queries
- `src/main/java/com/vclass/service/SubmissionRepository.java` — saves assignment submissions
- `src/main/java/com/vclass/service/SubmissionFileStorage.java` — copies selected files into uploads


## Assignment file submission

Faculty and Admins can create assignments from their dashboard's **Assignment Management** section. Students can open **Assignments**, choose an assignment, and use **Choose file...** to attach a document. The chooser accepts all file types; files must be 25 MB or smaller.

The app copies each submitted file into `uploads/submissions/` using Java's `File` and stream classes, then stores the relative path in the MySQL `submissions.file_path` column. Re-submitting replaces the previous stored file. The `uploads/` folder is ignored by Git because it contains user-submitted files.



For a function-by-function explanation of the source, read [CODE_GUIDE.md](CODE_GUIDE.md).

