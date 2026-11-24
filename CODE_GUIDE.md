# VCLASS Code Guide

This guide is for explaining the project in class. It names every Java class and function, then describes the statements inside each function in the order they run. Source line numbers can change as code is edited, so use the class and function names to find the right part in VS Code.

## A short explanation for your sir

> VCLASS is a Java Swing desktop application. Swing displays the screens. JDBC connects the Java program to MySQL. Repository classes run SQL with prepared statements. Model classes carry database rows to the screens. `CardLayout` switches dashboard sections. Java file I/O copies a student's selected assignment file into the project, and the database stores its path.

## How the program works

1. `Main.main` starts Swing and opens `LoginFrame`.
2. `LoginFrame.attemptLogin` reads the email and password, then calls `AuthService.login` in a `SwingWorker` so the screen stays responsive.
3. `AuthService.login` hashes the entered password, asks `DBConnection` for a JDBC connection, and searches the `users` table with a `PreparedStatement`.
4. When a matching user is returned, `DashboardFrame` builds the menu for that user's role.
5. A screen such as `StudentManagementPanel` reads or edits form values. Its repository class performs the SQL and returns model objects.
6. In assignment submission, `StudentAssignmentsPanel` gets a `File` from `JFileChooser`. `SubmissionFileStorage` copies it into `uploads/submissions`; `SubmissionRepository` records the relative path and submission details in MySQL.

## Project folders

- `src/main/java/com/vclass/Main.java` starts the application.
- `db/` loads the local JDBC configuration.
- `model/` contains Java objects representing rows or dropdown choices.
- `service/` contains database operations and file-storage logic.
- `ui/` contains Swing frames and panels.
- `util/` contains small helpers such as password hashing and the connection check.
- `database/schema.sql` creates the database and tables.
- `database/demo-accounts.sql` adds demo Faculty and Student accounts.
- `database.properties` contains the local MySQL URL and credentials. It is ignored by Git.
- `uploads/submissions/` stores copies of submitted files. It is ignored by Git.

## What common Java statements mean

- `package ...;` places a class in a named folder group and lets other classes import it.
- `import ...;` makes a class from Java or this project available by its short name.
- `private` means a method or field is used inside its class; `public` allows other classes to use it.
- A constructor has the same name as its class. It initializes a new object.
- `final` means the field reference is assigned once. The fields in model classes are final so a fetched record is not accidentally changed.
- `new` creates an object, such as a `JButton`, `User`, or JDBC `PreparedStatement`.
- `if` checks a condition; `else` handles the other case; `return` leaves a method and can send a value back.
- `try (...) { ... }` closes database connections, statements, result sets, or file streams automatically when the block ends. `catch` handles an error and displays a useful message.
- `PreparedStatement` runs SQL with `?` placeholders. `setString`, `setInt`, and similar calls supply values safely.
- `ResultSet` reads rows returned by a `SELECT` query.
- `SwingWorker` runs database and file work in the background. `doInBackground` does the slow work; `done` updates Swing after it finishes.
- `ActionListener` code runs when a button is clicked. `ListSelectionListener` responds when a table row is selected.
- `CardLayout` keeps dashboard pages in one area and displays the selected page.
- `JTable` displays rows; `DefaultTableModel` supplies its column names and values.
- `JFileChooser` lets the student choose a local file. The chooser accepts all file types.

# Java classes and functions

## Application and database access

### `Main.java` — `Main.main(String[] args)`

- Schedules startup using `SwingUtilities.invokeLater`, which builds Swing windows on Swing's event thread.
- Tries to use the operating system's Swing look and feel. If unavailable, it keeps Swing's default.
- Creates and displays `LoginFrame`.

### `db/DBConnection.java` — `DBConnection`

- The private constructor prevents creating `DBConnection` objects; the class exposes a static connection method instead.
- `getConnection()` checks for `database.properties` in the project root. It loads the URL, username, and password, then calls `DriverManager.getConnection` to open JDBC.
- `required(Properties, key)` checks that the URL and username exist and are not blank. It gives a clear configuration error if either is missing.

### `service/AuthService.java` — `AuthService.login(email, password)`

- Builds a query that checks email, password hash, and active status.
- Opens the database and a `PreparedStatement` with try-with-resources.
- Hashes the supplied password with `PasswordUtil.sha256` and supplies both values to the query.
- Reads the matching row and returns a `User`; returns `null` when the credentials do not match.

### `util/PasswordUtil.java` — `PasswordUtil.sha256(char[] password)`

- Converts the entered characters to UTF-8 bytes.
- Uses Java's built-in SHA-256 message digest.
- Converts each hash byte to two lowercase hexadecimal characters and returns the hash string. The constructor is private because this helper only has a static method.

### `util/DatabaseConnectionTest.java` — `DatabaseConnectionTest.main`

- Opens a connection with `DBConnection` in a try-with-resources block.
- Prints the MySQL server name/version and selected database when successful.
- Catches connection/configuration errors, prints guidance, and exits with a failure status.

## Model classes

Model classes hold values read from or sent to the database. Their constructors fill the fields; getters let the screen and repository read those values.

- `model/User.java`: `User(...)` stores the login user's ID, name, email, and role. `getId`, `getFullName`, `getEmail`, and `getRole` return those fields.
- `model/StudentRecord.java`: `StudentRecord(...)` stores a student profile plus its linked user account. `getStudentId`, `getUserId`, `getStudentNumber`, `getFullName`, `getEmail`, `getDepartment`, `getSemester`, and `isActive` return each value.
- `model/FacultyRecord.java`: `FacultyRecord(...)` stores a faculty profile and linked user. Its getters return the faculty ID, user ID, employee number, name, email, department, and active state.
- `model/CourseRecord.java`: stores course ID/code/name/description and optional assigned faculty ID/name. Its getters provide those fields.
- `model/FacultyChoice.java`: stores the ID, name, and email shown in a faculty dropdown. `toString()` controls the text displayed by `JComboBox`; ID zero represents “Unassigned.”
- `model/AssignmentCourseChoice.java`: stores a course ID/code/name for the assignment form. `toString()` creates the dropdown label.
- `model/AssignmentRecord.java`: stores assignment and course information, due time, maximum marks, and (for student views) submission status and file path. Its getters expose each field.

## Database service classes

### `service/StudentRepository.java` — student SQL

- `findAll(search)` joins `students` to `users`, optionally searches student number/name/email/department, and builds a `List<StudentRecord>` from the result rows.
- `create(...)` starts a transaction, inserts a `STUDENT` row into `users`, reads its generated ID, inserts the linked profile into `students`, then commits. If either insert fails, it rolls back both.
- `update(...)` edits the linked account and profile in one transaction. It changes the password only when a new password was supplied.
- `delete(studentId)` deletes the matching user account. The database foreign key cascades that deletion to the student profile and the student's related records.
- `blankToNull(value)` trims optional text and stores an empty field as SQL `NULL`.

### `service/FacultyRepository.java` — faculty SQL

- `findAll(search)` joins `faculty` and `users`, searches employee number/name/email/department, and returns `FacultyRecord` objects.
- `create(...)` inserts the faculty login, gets its generated user ID, inserts the faculty profile, and commits both changes together.
- `update(...)` updates the login/profile together and changes the password only when a new password was entered.
- `delete(facultyId)` deletes the linked user. The foreign key removes the faculty profile; courses assigned to that faculty become unassigned.
- `blankToNull(value)` converts blank optional text to SQL `NULL`.

### `service/CourseRepository.java` — course SQL

- `findAll(search)` joins courses to faculty and users, then returns `CourseRecord` rows for the table.
- `findActiveFaculty()` returns active faculty as `FacultyChoice` objects and includes the “Unassigned” option.
- `create(...)` inserts a course and its optional faculty ID.
- `update(...)` changes the course details and assigned faculty.
- `delete(courseId)` deletes the course. MySQL's cascade rules also remove related enrollment, assignment, attendance, and submission rows.
- `setFaculty(...)` saves no faculty as SQL `NULL`; otherwise it saves the selected faculty ID.
- `blankToNull(value)` handles optional descriptions.

### `service/AssignmentRepository.java` — assignment SQL

- `findCoursesForStaff(role, userId)` returns every course for Admin or only courses assigned to the signed-in Faculty member.
- `findForStaff(role, userId, search)` lists assignments for Admin or the signed-in Faculty member and applies the search text.
- `findForStudent(userId, search)` lists assignment rows with that student's submission status and stored file path.
- `create(...)` inserts a course assignment, description, deadline, and mark limit.
- `update(...)` edits those same assignment fields.
- `delete(assignmentId)` gathers submission file paths, deletes the assignment and its database children, commits, then deletes the saved files from disk.
- `blankToNull(value)` stores a blank optional description as `NULL`.

### `service/SubmissionFileStorage.java` — Java file handling

- `store(File source)` checks that a real, non-empty file was chosen and that it is at most 25 MB.
- It creates `uploads/submissions` if needed, makes a unique safe filename, and copies bytes with `FileInputStream` and `FileOutputStream`.
- It returns a relative path such as `uploads/submissions/<unique-name>` for the database.
- `resolve(relativePath)` turns a stored relative path into a `File` on this computer.
- `delete(relativePath)` removes a saved file when a submission is replaced or its assignment is deleted.

### `service/SubmissionRepository.java` — `SubmissionRepository.submit(...)`

- Copies the selected file with `SubmissionFileStorage.store`.
- Starts a database transaction and finds the student's profile ID using the login's user ID.
- Reads the assignment deadline and the previous submission path.
- Sets the status to `LATE` if the deadline has passed, otherwise `SUBMITTED`.
- Inserts the submission or updates the existing submission for this student/assignment pair. A resubmission clears prior marks and feedback.
- Commits the database change. If the save fails, it deletes the new copied file; after a successful replacement, it deletes the old copy.

## Swing screens

### `ui/LoginFrame.java` — login window

- `LoginFrame()` sets the window title, size, close behavior, centers it, and builds the form.
- `buildContent()` lays out the title, email/password fields, and sign-in button.
- `label(...)` creates a consistently styled text label.
- `styleField(...)` applies the neutral background, text, and border to a field.
- `attemptLogin()` validates that fields are not blank, starts a `SwingWorker`, calls `AuthService.login`, clears the password array, and then either shows an error or opens `DashboardFrame`.
- In its `SwingWorker`, `doInBackground()` performs the database login away from the UI thread. `done()` re-enables the button and updates the window after the result arrives.

### `ui/DashboardFrame.java` — role dashboard

- `DashboardFrame(user)` creates the main window and adds the sidebar and main content.
- `buildSidebar(user)` creates the VCLASS label, role-specific navigation buttons, signed-in user's name, and sign-out action.
- `buildMainArea(user)` creates the page title and a `CardLayout` holder. It adds the overview and builds the matching screen for each supported menu item.
- `buildOverview(user)` creates the welcome message and summary cards.
- `statCard(...)` creates one summary card.
- `buildPlaceholder(module)` creates a simple page for a module that is not implemented yet.
- `menuFor(role)` returns different navigation labels for Admin, Faculty, and Student.
- `showPage(name)` tells `CardLayout` which page to show and marks its navigation button as selected.
- `label(...)` creates a text label using the dashboard's font and color.

### `ui/StudentManagementPanel.java` — Admin student screen

- The constructor adds the toolbar, table, status label, selection listener, and initial database load.
- `buildToolbar()` creates search, refresh, add, edit, and delete controls.
- `buildTable()` creates the student table and hides the internal ID column.
- `button(...)` and `styleField(...)` apply consistent control styling.
- `loadStudents()` calls `StudentRepository.findAll` in a `SwingWorker` and fills the table model.
- `openForm(existing)` displays the add/edit form. A null record means add; a record means edit. It validates values, then calls the repository in the background.
- `validate(...)` checks required name/email/number, email format, and password rules.
- `openSelectedForEdit()` finds the selected row's student record and opens the form.
- `deleteSelected()` asks for confirmation, then deletes the selected record.
- `updateSelectionButtons()` enables edit/delete only when a row is selected.
- `setBusy(...)` temporarily disables refresh and the table during database work.
- `safe(...)` turns a null string into an empty string for a form.
- `showFailure(...)` displays database errors and translates duplicate-key errors into a clearer message.

### `ui/FacultyManagementPanel.java` — Admin faculty screen

- The constructor builds the panel, connects selection/search listeners, and loads faculty.
- `buildToolbar()` creates the faculty search and account buttons.
- `buildTable()` styles the faculty list and hides its internal ID.
- `button(...)` and `styleField(...)` style controls.
- `loadFaculty()` runs the search and updates the table.
- `openForm(existing)` creates or edits the faculty login/profile form and saves through `FacultyRepository`.
- `validate(...)` checks required fields, email format, and password length.
- `openSelectedForEdit()` loads the selected faculty record and opens its form.
- `deleteSelected()` confirms and removes a faculty account.
- `updateSelectionButtons()` enables row actions only when a row is selected.
- `setBusy(...)` disables controls during work; `safe(...)` handles null form text; `showFailure(...)` displays errors.

### `ui/CourseManagementPanel.java` — Admin course screen

- The constructor builds the toolbar/table and loads courses.
- `buildToolbar()` creates search, refresh, add, edit, and delete controls.
- `buildTable()` displays course code/name, assigned faculty, and description.
- `button(...)` and `styleField(...)` standardize the controls.
- `loadCourses()` reads courses and refreshes the table.
- `openForm(existing)` loads active faculty choices for the form.
- `showCourseForm(existing, faculty)` asks for course details, validates code/name, then creates or updates through `CourseRepository`.
- `openSelectedForEdit()` loads the selected course and opens the edit form.
- `deleteSelected()` confirms the deletion, including its related records, then calls the repository.
- `updateSelectionButtons()`, `setBusy(...)`, and `safe(...)` handle selection, loading state, and optional form text.
- `showFailure(...)` presents SQL errors in a readable dialog.

### `ui/AssignmentManagementPanel.java` — Admin/Faculty assignment screen

- The constructor stores the signed-in user, creates the controls, and loads assignments for that role.
- `buildToolbar()` creates search, refresh, add, edit, and delete controls.
- `buildTable()` styles the assignment list and hides the internal ID.
- `button(...)` and `styleField(...)` style controls.
- `load()` asks the repository for assignments that the current role may manage.
- `openForm(existing)` loads courses allowed for the current Admin/Faculty account.
- `showForm(existing, courses)` collects course, title, description, due date/time, and max marks, validates them, and saves the assignment.
- `openSelectedForEdit()` retrieves the selected assignment and opens it in the form.
- `deleteSelected()` confirms the assignment and submission-file deletion, then calls the repository.
- `updateSelectionButtons()`, `busy(...)`, and `safe(...)` manage the selected row and form text.
- `fail(...)` displays an error dialog.

### `ui/StudentAssignmentsPanel.java` — Student submission screen

- The constructor builds search/file/submission controls and the assignment table, then loads assignments for the student account.
- `buildToolbar()` creates search, choose-file, comment, submit/replace, and open-file controls.
- `buildTable()` displays course/assignment/deadline/status and hides internal IDs and stored paths.
- `button(...)` and `styleField(...)` style buttons and text fields.
- `load()` lists assignments and the student's current submission status/path.
- `chooseFile()` opens `JFileChooser` with the all-file-types option and stores the selected object in a `File` field.
- `submitFile()` checks assignment selection and file size, asks for confirmation, then calls `SubmissionRepository.submit` in a `SwingWorker`.
- `openSubmittedFile()` opens the selected student's stored file with the computer's default application.
- `fileName(path)` shows a readable filename without the generated unique prefix.
- `busy(...)` disables controls during work; `fail(...)` displays upload/database errors.

## SQL schema

`database/schema.sql` creates `vclass_swing_db`, then creates the tables in parent-before-child order:

1. `users` stores login name, email, password hash, role, and active status.
2. `students` and `faculty` store role-specific profile details linked to `users`.
3. `courses` optionally links to a faculty member.
4. `enrollments` links students to courses.
5. `assignments` belongs to a course.
6. `attendance` links a student and course on a date.
7. `submissions` links a student to an assignment and stores `file_path` plus grading details.

Foreign keys enforce these relationships. `ON DELETE CASCADE` removes child rows when a parent is deleted; `ON DELETE SET NULL` keeps a course if its faculty account is deleted.

## Important explanation about submitted files

The actual PDF/Word/etc. file is copied onto the computer in `uploads/submissions`. MySQL stores only its relative filename/path in `submissions.file_path`, not the file bytes. The file chooser has no extension filter, so it accepts any type. The app currently limits each file to 25 MB.
