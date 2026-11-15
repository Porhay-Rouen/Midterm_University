# Implementing Access-Controlled Use Cases in a Digital University System (Midterm)

**INFO 5100 · Group 1 · Northeastern University**

## Team Information

| Member | Role | NUID |
|---|---|---|
| Porhay Rouen | Administrator | 003997909 |
| Muykhim Ing | Faculty | 003997968 |
| Brendan Gilbert | Student | 003118676 |
| RaeNeice Dunbar | Registrar | 003914260 |

---

## 1. Project Overview

### Purpose

The Digital University System is a Java Swing desktop application that models a university: colleges, departments, courses, semesters, course offerings, and the people who use them. This project connects that model to an Access Control Layer. Every user has to log in, and once logged in they can only reach the screens and actions that their role permits.

### Objectives

- Combine the reference Digital University model with an authentication and role-based authorization layer.
- Implement login and logout, and make sure each role (Admin, Faculty, Student, Registrar) can only reach its own features.
- Implement the four role use cases from the assignment spec, with input validation and null checks throughout.
- Pre-populate the system with realistic sample data so every feature can be demoed straight away.
- Use a consistent UI: one home panel per role, with CardLayout navigation and JTable views.

### Key Features Implemented

- **Secure login.** Users enter a username, password, and role. Passwords are stored as salted SHA-256 hashes and never as plain text.
- **Role-based authorization.** `MainFrame` builds a different home screen for each role. Every role panel also calls `session.requireRole(...)`, so it cannot be opened with the wrong role.
- **Admin.** Account management, person registration with automatic ID generation and duplicate checks, student/faculty records with 3 search methods, registrar records, an analytics dashboard, and profile editing.
- **Student.** Course registration with 3 search methods and an 8-credit limit, assignment submission, a graduation audit, a transcript filtered by semester showing GPA and academic standing, tuition payment with payment history and refunds, and profile editing.
- **Registrar.** Course offering management, admin-side enroll and drop, financial reconciliation, institutional reports, and profile and office-hours editing.
- **Faculty.** Course management (details, syllabus upload, open/close enrollment), enrolled-student lists with transcript summaries, assignment grading with automatic letter grades, student ranking and class GPA, course performance reports with a semester filter and CSV export, tuition insight, and profile editing.
- **Sample data.** 31 persons: 18 students, 11 faculty, 1 admin, and 1 registrar. Also 2 departments, 3 degrees, 3 semesters, and 11 course offerings with faculty assigned. Fall 2026 is a finished semester with every assignment scored and final grades. Spring 2027 is the current semester, with 2 of 3 assignments scored and grades marked IP until the semester ends. Every student has seat assignments.

---

## 2. Installation & Setup Instructions

### Prerequisites

| Requirement | Version / Notes |
|---|---|
| Java JDK | 17 or newer (the project is compiled with `javac.source`/`target` = 17) |
| IDE | Apache NetBeans (recommended). The UI is built with the NetBeans GUI Builder (`.form` files). |
| Build tool | Apache Ant, which comes bundled with NetBeans (`build.xml`) |
| Libraries | None. The project uses only the standard JDK (Swing, `java.time`, `java.security`). |
| Git | Needed to clone the repository |

### Clone the Repository

```bash
git clone https://github.com/Porhay-Rouen/Midterm_University.git
cd Midterm_University
```

### Open and Run in NetBeans

1. Open NetBeans and choose **File → Open Project…**.
2. Select the `Midterm_University_Model` folder inside the cloned repo. It shows a NetBeans project icon.
3. Right-click the project and choose **Properties → Libraries**. Make sure the Java Platform is JDK 17 or newer.
4. Check that the main class is `app.Main` under **Properties → Run → Main Class**.
5. Right-click the project and choose **Clean and Build**.
6. Press **Run Project** (F6). The Login window opens.

### Run from the Command Line (optional)

```bash
cd Midterm_University_Model
ant clean jar
java -jar dist/Midterm_University_Model.jar
```

### Demo Accounts

Every sample account uses the password `123456`.

| Role (select in drop-down) | Username(s) |
|---|---|
| Admin | `admin` |
| Registrar | `registrar` |
| Faculty | `faculty1` … `faculty11` |
| Student | `student1` … `student18` |

- Every student has paid for Fall 2026. For Spring 2027, student1–student8 have paid, so their transcripts are visible. student9–student18 still owe a balance, so their transcripts are locked until they pay. student3 has dropped a paid course, so their payment history shows a refund.
- faculty1–faculty5 teach the current Spring 2027 courses. faculty6–faculty10 taught Fall 2026, so they have finished grades to look at.

---

## 3. Authentication & Access Control

### Architecture

| Class | Responsibility |
|---|---|
| `access.AccessControl` | Stores credentials (username → salted hash), and handles login, logout, createAccount, resetPassword, and deleteAccount. |
| `access.Session` | Represents the logged-in user: the Person, the Role chosen at login, and whether the session is active. Provides `can(type)` and `requireRole(type)`. |
| `app.AppContext` | Holds the single shared instance of University, AccessControl, and UniversityReports, and passes it to every screen. |
| `ui.login.LoginFrame` | The login screen with username, password, and role drop-down. |
| `ui.main.MainFrame` | Builds the home screen for the session's role type only. |

### Login Process

1. The user enters a username, a password, and picks a role (Admin, Faculty, Student, or Registrar).
2. `AccessControl.login(username, password, role)` then checks the following, in order:
   - The username and password are not empty, and a role is selected. If not, it throws `IllegalArgumentException`.
   - The username exists and the password hash matches the stored salted SHA-256 hash.
   - The person is still registered and the account is active.
   - The person actually holds the role that was selected.
3. If any check fails, the user sees an error dialog. For an unknown user and a wrong password the message is the same, "Invalid username or password.", so attackers cannot use it to find out which usernames exist.
4. If every check passes, a Session is created, the password field is cleared, and MainFrame opens with the home panel for that role.

Usernames are case-insensitive (`Admin` and `admin` are the same account). Usernames need at least 3 characters (letters, digits, `.`, `_`, `-`) and passwords need at least 6.

### Logout Process

1. The user clicks **Log Out** in the header of their home panel.
2. `AccessControl.logout(session)` marks the session inactive, so `session.can(...)` now returns false for every role.
3. The window is disposed and the Login screen opens again.

### Authorization Rules

Authorization is enforced at two levels:

1. **Navigation level.** `MainFrame.buildMenu()` switches on `session.getRoleType()` and loads only that role's home panel. No other role's pages are ever created.
2. **Panel level (defense in depth).** The constructor of every role page calls `session.requireRole(Role.Type.X)`. A closed session or a role mismatch throws `SecurityException("Access denied: …")`, so a page cannot be opened even if someone tries to reach it directly.

| Feature Area | Admin | Faculty | Student | Registrar |
|---|:---:|:---:|:---:|:---:|
| Create / reset / delete login accounts | ✅ | ❌ | ❌ | ❌ |
| Register persons (student / faculty / registrar) | ✅ | ❌ | ❌ | ❌ |
| View / edit / delete student & faculty records | ✅ | ❌ | ❌ | ❌ |
| Manage registrar records | ✅ | ❌ | ❌ | ❌ |
| University analytics dashboard | ✅ | ❌ | ❌ | ❌ |
| Manage own assigned courses / grading / course reports | ❌ | ✅ | ❌ | ❌ |
| Search, enroll in, and drop own courses | ❌ | ❌ | ✅ | ❌ |
| View own transcript / pay own tuition | ❌ | ❌ | ✅ | ❌ |
| Create / edit / delete course offerings | ❌ | ❌ | ❌ | ✅ |
| Enroll / drop any student (admin-side) | ❌ | ❌ | ❌ | ✅ |
| Tuition reconciliation & institutional reports | ❌ | ❌ | ❌ | ✅ |
| Edit own profile | ✅ | ✅ | ✅ | ✅ |

Other rules:

- Deactivated accounts cannot log in, even with the correct password.
- A user may only log in as a role they actually hold. For example, student1 choosing "Admin" is refused.
- The data model contains no passwords. Credentials exist only inside AccessControl.

---

## 4. Features Implemented

### Administrator — Porhay Rouen

Package `ui.admin`. Home panel: `AdminMainPanel` (red header).

| Page | Functionality |
|---|---|
| Dashboard (`DashboardPanel`) | Four JTable reports: total active users by role, courses offered per semester, enrolled students per course (with faculty and capacity), and the tuition revenue summary of paid tuition. |
| Accounts (`AccountsPanel`) | Table listing every person with their ID, roles, username, and status. Supports create account, reset password, and delete account (removes the login but keeps the person record). |
| Register (`RegisterPanel`) | Registers a new Student, Faculty, or Registrar, with the department and degree picked as needed. The university ID is generated automatically (S001, F001, R001…). Duplicate emails are rejected. |
| Records (`RecordsPanel`) | View, edit, and delete student and faculty details (name, email, phone, department, active status). Supports 3 search methods: by name, by ID, and by department. Can assign or reassign faculty to a course offering. |
| Registrars (`RegistrarsPanel`) | View, update, and delete registrar information, including office hours. |
| Profile (`ProfilePanel`) | View and edit the admin's own profile details. |

### Faculty — Muykhim Ing

Package `ui.faculty`. Home panel: `FacultyMainPanel` (purple header).

| Page | Functionality |
|---|---|
| My Courses (`MyCoursesPanel`) | A semester drop-down lists the faculty member's assigned courses. They can view and update the title, description, room, meeting time, and capacity, upload or change the syllabus with a file chooser, and open or close enrollment. Enrollment can only be changed before the semester starts. |
| Students (`StudentsPanel`) | Pick a semester and course to see the enrolled students and each one's assignment progress. View Transcript Summary shows the selected student's overall GPA and academic standing. |
| Grading (`GradingPanel`) | Add assignments (title and max score, validated). Record a score for each student and assignment. The table shows each student's rank, every assignment score, their total %, and their final grade. Compute final grades for one student or the whole class, with the letter grade calculated automatically from the percentage. A ranking table lists students ranked by total % and shows the class GPA. |
| Reports (`ReportsPanel`) | A semester filter (All Semesters, or one semester). Shows course performance (enrollment count, average grade), the grade distribution (students per letter grade), and tuition collected from the faculty member's enrolled students. Export CSV saves the report to a file. |
| Profile (`ProfilePanel`) | Shows the university ID, username, and department, which are read-only. The first name, last name, email, and phone can be edited, with Save and Reset buttons. |

### Student — Brendan Gilbert

Package `ui.student`. Home panel: `StudentHomePanel` (blue header).

| Page | Functionality |
|---|---|
| Register (`RegisterPanel`) | View all offerings for the semester. 3 search methods (by Course ID, by Teacher, by Course Name), with results in the same JTable. Enroll and drop courses. A live credit counter enforces the 8-credit-per-semester limit. |
| Transcript (`TranscriptPanel`) | Semester drop-down that filters the JTable. Columns: Term, Academic Standing, Course ID, Course Name, Grade, Term GPA, and Overall GPA. Academic standing is calculated automatically (Good Standing / Academic Warning / Academic Probation). The transcript stays locked until tuition is paid. |
| Tuition (`TuitionPanel`) | Shows the current balance, which is billed per enrolled course at $1,000 per credit. The Pay Tuition button shows a "no balance to pay" dialog when the balance is ≤ 0 and does not add that attempt to the history. The payment history table includes automatic refunds when a student drops a course they have already paid for. |
| Coursework (`CourseworkPanel`) | Pick an enrolled course to see its assignments, what has been submitted, and the scores. Submit an assignment with a file chooser, and see the grade so far for the course. |
| Grad. Audit (`GradAuditPanel`) | Shows the degree and its required credits (MSIS = 32), credits completed, credits in progress, and whether the INFO 5100 core course is done. A progress meter shows completion, and the page states automatically whether the student is ready to graduate. |
| Profile (`StudentProfilePanel`) | View and edit personal details (name, email, phone). The university ID, department, and degree are shown read-only. Save and Reset buttons. |

### Registrar — RaeNeice Dunbar

Package `ui.registrar`. Home panel: `RegistrarHomePanel` (green header).

| Page | Functionality |
|---|---|
| Offerings (`RegOfferingsPanel`) | Create, update, and delete semester course offerings. Choose the semester, course, and faculty, and set capacity, room, and meeting time. Includes a find/search box and fills the form from the selected row. |
| Enrollment (`RegEnrollmentPanel`) | Admin-side enroll a student into an offering and drop a student. The same rules as the student page apply (8-credit limit, capacity, tuition refund on drop). |
| Finance (`RegFinancePanel`) | Monitors tuition payment status for every enrolled student, with a filter for All, Paid, or Unpaid. Per-semester financial report: tuition collected, the unpaid tuition summary, and the revenue breakdown by department. |
| Reports (`RegReportsPanel`) | Report drop-down shown in a JTable: Enrollment by department / course and GPA distribution by program (3.5–4.0, 3.0–3.49, 2.0–2.99, below 2.0, no GPA yet). |
| Profile (`RegProfilePanel`) | View and edit own contact information and office hours, with an Edit / Save toggle. |
| Shared | `RegPageTemplate`, a reusable page layout for registrar screens. |

### Shared Infrastructure — Porhay Rouen (with team contributions)

- `model.*` holds the complete domain model: University, College, Department, Course, Degree, Semester, CourseSchedule, CourseOffering, Seat/SeatAssignment, Person/Roles, Transcript, CourseLoad, Grade, Payment, UniversityReports, and Validation.
- `access.*` contains AccessControl and Session.
- `app.*` contains Main, AppContext, and SampleData.
- `ui.login.LoginFrame` and `ui.main.MainFrame`.
- `UI_GUIDE.md`, the shared home-screen layout and styling guide by Brendan Gilbert, based on Rae's `RegistrarHomePanel` pattern.

---

## 5. Usage Instructions

### General Flow

1. Run the project. The Login window opens.
2. Enter a username and the password `123456`, then pick the matching Role and click **Login**.
3. Your role's home screen opens. The header shows the page title and "Signed in as …", and the left nav buttons switch pages. The active page's button turns blue.
4. Any action that breaks a rule, such as missing input or going over the credit limit, shows a dialog that explains why.
5. Click **Log Out** to return to the Login screen.

### Example Scenarios

#### Scenario A – Admin registers a new student and gives them a login

1. Log in as `admin` with the role Admin.
2. Open **Register**, choose type Student, enter the first name, last name, and email, pick a department and degree, then submit. The new ID (for example S019) appears in the confirmation.
3. Try registering again with the same email. The duplicate is rejected.
4. Open **Accounts**, select the new person, enter a username and password, and click **Create**.
5. Open **Dashboard**. The active Student count has gone up by one.

#### Scenario B – Admin searches and edits records

1. In **Records**, choose Search by Department, type `Information Systems`, and click **Search**.
2. Select a faculty member, change their phone number, and click **Update**.
3. Select an offering and assign a different faculty member to it.

#### Scenario C – Student registers for courses and hits the credit limit

1. Log in as `student1` with the role Student. Sample students already have 8 credits.
2. Open **Register**, and in the Semester drop-down, pick Spring 2027. Search by Teacher with the keyword `Shah`, select INFO6150, and click **Enroll**. The 8-credit limit blocks it with a message.
3. Click the **Show All** button and **Drop** one course. The credit counter falls to 4. Now enroll in a different course.

#### Scenario D – Student pays tuition to unlock the transcript

1. Log in as `student10` (unpaid for Spring). Open **Transcript**. It is locked and shows the outstanding balance.
2. Open **Tuition**, click **Pay Tuition**, and confirm with **Yes**. The payment shows up in the history.
3. Click **Pay Tuition** again. A "no balance to pay" dialog appears and nothing is added to the history.
4. Go back to **Transcript**. It is now visible. Fall 2026 shows final grades with term and overall GPA and standing, and Spring 2027 shows IP for the current semester. Filter by semester with the drop-down.
5. Open **Grad. Audit** to see credits completed (Fall), credits in progress (Spring), and whether the student is ready to graduate. Open **Coursework**, pick a course, select an assignment, and submit a file.
6. Log in as `admin`. On **Dashboard**, the tuition revenue has gone up.

#### Scenario E – Registrar creates an offering and enrolls a student

1. Log in as `registrar` with the role Registrar.
2. **Offerings:** choose Spring 2027, a course, and a faculty member, set capacity to 25, room to R201, and time to "Sat 9-12", then click **Create**.
3. **Enrollment:** select a student who has fewer than 8 credits, select the new offering, and click **Enroll**.
4. **Finance:** set the filter to Unpaid to see who still owes tuition, then check the per-department revenue.
5. **Reports:** choose GPA distribution by program.

#### Scenario F – Faculty grades a course and exports a report

1. Log in as `faculty1` with the role Faculty.
2. **My Courses:** choose Spring 2027, select INFO5100, change the capacity to 35, and click **Save Changes**. Click **Upload Syllabus…** and pick a file. Click **Close Enrollment**, then reopen it.
3. **Students:** select the course, then a student, and click **View Transcript Summary**.
4. **Grading:** select the course, add an assignment (for example "Final Project", max 100), select a student, click **Record Score**, then click **Compute All Final Grades**. Check the rank order and class GPA.
5. **Reports:** set the filter to Fall 2026 to see the average grade and grade distribution. (Log in as `faculty6` for a finished course.) Click **Export CSV**.
6. **Profile:** change the phone number and click **Save Profile**.

---

## 6. Testing Guide

### How to Test

- **Manual UI testing.** Run `app.Main` and work through the test cases below for each role. Watch the NetBeans Output window: it should show no stack traces or exceptions.
- **Model smoke test.** `src/demo/ModelConsoleTest.java` runs about 66 console checks on the model, covering enrollment rules, GPA, standing, tuition, and refunds. Right-click the file and choose **Run File**. It runs separately from the main application.
- Each team member tested their own use case on their branch before opening a pull request to master.

### Authentication Test Cases

| # | Steps | Expected Result |
|---|---|---|
| A1 | `admin` / `123456` / Admin | Admin home opens |
| A2 | `admin` / `wrongpw` / Admin | "Invalid username or password." |
| A3 | `nobody` / `123456` / Admin | Same message as A2, so the username's existence isn't revealed |
| A4 | Blank username, or blank password | "Username cannot be empty." / "Password cannot be empty." |
| A5 | `ADMIN` / `123456` / Admin | Login succeeds (usernames are case-insensitive) |
| A6 | Admin deactivates student3 in Records → log in as `student3` | "This account is deactivated…" |
| A7 | Log in, click Log Out | Returns to Login. The session is inactive. |

### Authorization Test Cases

| # | Steps | Expected Result |
|---|---|---|
| Z1 | `student1` / `123456` / Admin | "This account does not have the ADMIN role." |
| Z2 | `faculty1` / `123456` / Registrar | Refused with the same kind of message |
| Z3 | `registrar` / `123456` / Student | Refused |
| Z4 | Log in as each role | Only that role's nav buttons and pages appear |
| Z5 | (Code check) Build a role panel with a session of the wrong role | `SecurityException: Access denied: this page is for the X role.` |

### Feature Test Cases (samples)

| # | Role | Steps | Expected Result |
|---|---|---|---|
| F1 | Admin | Register a person with an email that already exists | Rejected as a duplicate |
| F2 | Admin | Register with an empty first name or an invalid email | Validation error dialog |
| F3 | Admin | Records → search by ID S001 | One matching row |
| F4 | Student | Enroll in a third 4-credit course | Blocked: over 8 credits |
| F5 | Student | Pay when balance = 0 | "You have no balance to pay." Nothing is added to history. |
| F6 | Student | Drop a paid course | A refund appears in the payment history |
| F7 | Student | Unpaid student opens Transcript | Locked message showing the balance |
| F8 | Registrar | Create an offering with no course or faculty selected, or a non-numeric capacity | Validation error dialog |
| F9 | Registrar | Enroll a student who is already in that offering | Error dialog |
| F10 | Registrar | Finance → filter Paid | Shows only students with a zero balance |
| F11 | Faculty | Add an assignment with an empty title, or a max score of 0 or text | Validation error dialog |
| F12 | Faculty | Record a score with no student selected, or a non-numeric score | Validation error dialog |
| F13 | Faculty | Close enrollment on a course whose semester has already started (Fall 2026) | Refused: only allowed before the semester starts |
| F14 | Faculty | Reports → Export CSV with nothing to report | "There is nothing to export for this semester." |
| F15 | Student | Coursework → Submit with no assignment selected | "Select an assignment in the table first." |
| F16 | Student | Grad. Audit for a student with a Fall course | Shows credits completed, and Ready to graduate = No (fewer than 32 credits) |

### Verifying Authentication & Authorization Work

1. Run A1–A7. Each one should succeed or fail exactly as listed, with a dialog and never a console exception.
2. Run Z1–Z4 for every combination of user and role. A user is admitted only with a role they hold.
3. After logging out, confirm that you return to the Login screen and that none of the previous user's screens are still open.
4. Code review: every role page constructor calls `session.requireRole(...)`, and `MainFrame.buildMenu()` creates only one role's panel.

---

## 7. Challenges & Solutions

| Challenge | Solution |
|---|---|
| Four people editing MainFrame at the same time caused merge conflicts. | Each member edits only their own case in `buildMenu()`, and nobody runs Source → Format on shared files. Git can then merge the changes cleanly. |
| Inconsistent look between role screens. | Brendan wrote `UI_GUIDE.md`, based on Rae's `RegistrarHomePanel` pattern. It defines one standard structure (header / left nav / CardLayout content), the colors for each role, and a fixed 1100×700 window size. |
| Nav buttons stretched to the full window height in a BorderLayout West region. | Wrapped the GridLayout nav panel in a `pnlNavHolder` (BorderLayout North), so buttons keep their normal height. |
| Button colors were invisible on macOS because the Aqua look-and-feel ignores background colors. | Set the nav buttons to `opaque = true` and `borderPainted = false`. This gives flat, colored buttons on every OS. |
| Storing passwords safely while keeping the model free of security code. | AccessControl keeps salted SHA-256 hashes, compares them with `MessageDigest.isEqual`, and uses one generic error message. The model never sees a password. |
| Tuition balance going out of sync after enrolls, drops, and payments. | The balance is derived (tuition for enrolled courses minus what has been paid) and never stored. Dropping a paid course creates a refund record automatically. |
| Enroll/drop rules duplicated between the Student and Registrar pages. | Both pages call the same model methods (`student.enroll` / `drop`), so there is one source of truth for the credit limit, capacity, and refunds. |
| Unhandled exceptions crashing the app. | Model methods throw `IllegalArgumentException` or `IllegalStateException` with clear messages. Every UI action catches them and shows `e.getMessage()` in a `JOptionPane`. |
| Git mistakes on a personal branch (a commit made by accident). | Used `git revert` instead of rewriting history, kept a backup-registrar branch, and re-applied the work in clean commits. |

---

## 8. Future Enhancements

- **Stored files.** Store submitted files and syllabi inside the app, instead of saving only the file path, so faculty can open what a student submitted.
- **More export formats.** Export reports to PDF or Excel as well as CSV, and include charts of the grade distribution.
- **Persistent storage.** Save data to a database (for example SQLite or MySQL via JDBC) or to files, so changes survive a restart.
- **Stronger passwords.** Stronger password hashing with PBKDF2, bcrypt, or Argon2, plus a password-strength meter and "change my own password".
- **Login protection.** Account lockout after repeated failed logins, and a session timeout for idle sessions.
- **Audit log.** A log of admin and registrar actions (who changed what, and when).
- **Report export** for the Admin dashboard and Registrar finance reports.
- **Automated tests.** JUnit tests for AccessControl, enrollment rules, and the GPA and standing calculations, run in CI with GitHub Actions.
- **Waitlists** for full course offerings, and email notifications for enrollment changes.
- **Better usability.** Sortable and filterable tables, keyboard shortcuts, and accessibility improvements.
- **Email separation.** Separate personal and university email addresses.
- **Phone validation improvements.** A stricter shared phone-number format.
- **Course visibility.** Students can see courses from every department.

---

## 9. Contribution Breakdown

### GitHub Metrics

Taken from the commit history of `Porhay-Rouen/Midterm_University`. Work ran from 10/28/2026 to 11/12/2026.

| Member | Branch | Commits | PRs Opened | PRs Merged | Lines + / − |
|---|---|---:|---:|---:|---|
| Porhay Rouen | `porhay` | 13 | 6 | 14 | +13,472 / −2,551 |
| Muykhim Ing | `kim` | 10 | 2 | 1 | +4,201 / −933 |
| Brendan Gilbert | `brendan` | 19 | 8 | 0 | +3,377 / −449 |
| RaeNeice Dunbar | `Registrar` | 12 | 3 | 4 | +4,781 / −1,842 |
| **Team total** | | **54** | **19** | **19** | |

- The master branch has 82 commits in total, counting merge commits.
- **Commits** counts non-merge commits across all branches. **PRs Opened** counts pull requests from that member's branch that were merged into master. **PRs Merged** counts pull requests that member merged.
- Pull request numbers: Porhay #1, #2, #3, #5, #16, #18 · Muykhim #6, #19 · Brendan #7–#11, #13, #14, #17 · RaeNeice #4, #12, #15.
- Every pull request came from a personal branch and was merged into master.

### Porhay Rouen — Administrator + Core Architecture

- **Coding:** Designed and implemented the full model package (domain classes, validation, reports), the access package (AccessControl, Session), app (Main, AppContext, SampleData, including the finished Fall 2026 semester with grades), LoginFrame, and the Admin use case (Dashboard, Accounts, Register, Records, Registrars, Profile).
- **Documentation:** `MODEL_GUIDE.md` (model cheat sheet for every use case). Javadoc and comments on model and access classes.
- **Testing:** ModelConsoleTest model smoke test. Manual testing of login and admin pages.
- **Other:** Created the GitHub repository and set up the branching and PR workflow. Opened 6 PRs and merged 14 teammates' PRs into master.

### Muykhim Ing — Faculty

- **Coding:** Faculty use case: FacultyMainPanel (home and navigation), MyCoursesPanel (course details, syllabus upload, open/close enrollment), StudentsPanel (enrolled students, transcript summary), GradingPanel (assignments, scores, automatic final grades, ranking, class GPA), ReportsPanel (performance, grade distribution, tuition, semester filter, CSV export), and ProfilePanel. Wired the Faculty case in MainFrame.
- **Documentation:** University Model UML diagram. Javadoc comments on the faculty pages.
- **Testing:** Tested grading input validation, grade computation, report filters, and CSV export.
- **Other:** Opened 2 PRs (#6, #19) and merged PR #16.

### Brendan Gilbert — Student

- **Coding:** Student use case: StudentHomePanel, RegisterPanel (3 searches, enroll/drop, credit limit), CourseworkPanel (assignment submission, grade so far), TranscriptPanel (semester filter, GPA, standing, tuition lock), GradAuditPanel (credits, core course, ready to graduate), TuitionPanel (Pay Tuition, history, refunds), and StudentProfilePanel. Wired the Student case in MainFrame.
- **Documentation:** `UI_GUIDE.md`, the shared standard for role home screens, including the macOS button fix.
- **Testing:** Tested the student registration, coursework, transcript, graduation audit, tuition, and profile flows, plus cross-platform UI checks on macOS.
- **Other:** Opened 8 PRs (#7–#11, #13, #14, #17). Set the project to Java 17 and cleaned up `.gitignore` (stopped tracking .DS_Store and NetBeans private settings). Added code reviews on PRs #12, #15, #16, #18, and #19.

### RaeNeice Dunbar — Registrar

- **Coding:** Registrar use case: RegistrarHomePanel (the home screen and navigation pattern the other roles adopted), RegOfferingsPanel (create/update/delete offerings, faculty, capacity, room/time), RegEnrollmentPanel (admin-side enroll/drop), RegFinancePanel (payment status, collected/unpaid, department revenue), RegReportsPanel (enrollment by department/course, GPA distribution by program), RegProfilePanel (contact info and office hours), and the RegPageTemplate layout. Wired the Registrar case in MainFrame.
- **Documentation:** Drafted the README.
- **Testing:** Tested every registrar page: input validation on offerings, enrollment rule enforcement, finance filters, and report output.
- **Other:** Opened 3 PRs (#4, #12, #15) and merged 4 teammates' PRs (#5, #8, #9, #14). Merged master into Registrar to keep the branch in step with the team.
