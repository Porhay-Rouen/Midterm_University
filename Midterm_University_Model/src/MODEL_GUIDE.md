# Model Guide (package `model`)

Author: Porhay Rouen. Drop `src/model/` into the NetBeans project. Compiles on Java 17+.
`src/demo/ModelConsoleTest.java` is a console smoke test (66 checks). Do NOT push it to main (no debug prints rule).

## How to build objects (factory methods, no `new` for most classes)

```java
University uni = new University("Northeastern University");
College college = uni.newCollege("College of Engineering");
Department is = college.newDepartment("Information Systems");
Semester fall = uni.newSemester("Fall 2026", LocalDate.of(2026,9,1), LocalDate.of(2026,12,15));
Course c = is.getCourseCatalog().newCourse("INFO5100", "App Eng & Dev", 4, "desc");
Degree msis = is.newDegree("MSIS", 32);  msis.addCourse(c, true);   // true = core
CourseSchedule sched = is.newCourseSchedule(fall);
CourseOffering o = sched.newOffering(c, facultyRole, 30, "R101", "Mon 6-9 PM");
StudentRole s = uni.registerStudent("Porhay","Rouen","p@univ.edu","", is, msis);
FacultyRole f = uni.registerFaculty("John","Smith","j@univ.edu","", is);
AdminRole a = uni.registerAdmin(...);   RegistrarRole r = uni.registerRegistrar(...);
```
IDs are auto-generated (S001, F001, A001, R001). Duplicate email throws IllegalArgumentException.

## Cheat sheet by use case

| Role | Page | Use |
|---|---|---|
| Admin | Dashboard | `UniversityReports`: `activeUsersByRole()`, `coursesOfferedPerSemester()`, `enrolledPerCourse()`, `tuitionRevenueTotal()` |
| Admin | Register | `uni.registerStudent/Faculty/Registrar(...)` |
| Admin | Accounts / Records | `uni.getPersons(Role.Type)`, `uni.deletePerson(p)`, `uni.changeEmail(p, email)`, `p.setFirstName/setLastName/setPhone/setActive`, `uni.searchByName/searchById/searchByDepartment`, `offering.assignFaculty(f)` |
| Faculty | My Courses | `faculty.getOfferings(semester)`, `o.setTitle/setDescription/setMeetingTime/setCapacity/setSyllabusPath/setEnrollmentOpen`, `o.canChangeEnrollment()` |
| Faculty | Students | `o.getSeatAssignments()`, `sa.getSubmissionProgress()`, `student.getTranscript()` |
| Faculty | Grading | `o.addAssignment(title,max)`, `sa.recordScore(a,score)`, `sa.computeFinalGrade()`, `sa.setGrade(Grade)`, `o.getRankedSeatAssignments()`, `o.getClassGPA()` |
| Faculty | Reports | `o.getAveragePercentage()`, `o.getGradeDistribution()`, `o.getEnrolledCount()`, `faculty.getTotalTuitionCollected()` |
| Student | Register | `schedule.searchByCourseId/searchByTeacher/searchByCourseName(text)`, `student.enroll(o)`, `student.drop(o)`, `CourseLoad.MAX_CREDITS` |
| Student | Coursework | `sa.submit(assignment, path)`, `sa.getScore(a)`, `sa.getPercentage()` |
| Student | Graduation | `student.getCreditsCompleted()`, `isCoreCompleted()`, `isReadyToGraduate()`, `getGraduationProgressPercent()` |
| Student | Transcript | `student.canViewTranscript()`, `transcript.getSemesters()`, `findCourseLoad(sem)`, `load.getSemesterGPA()`, `transcript.getOverallGPA()`, `transcript.getStanding(sem)` |
| Student | Pay Tuition | `student.getBalance()`, `student.pay()` (null = nothing to pay, show dialog), `student.getPayments()` |
| Registrar | Offerings | `dept.newCourseSchedule(sem)`, `schedule.newOffering(...)`, `schedule.removeOffering(o)` |
| Registrar | Enrollment | `student.enroll(o)` / `student.drop(o)` (same rules as the student page) |
| Registrar | Finance | `reports.paymentStatusOfAllStudents()`, `tuitionCollected(sem)`, `tuitionUnpaid(sem)`, `revenueByDepartment(sem)` |
| Registrar | Reports | `reports.enrollmentByDepartmentAndCourse()`, `reports.gpaDistributionByProgram()` |

Any rule violation throws `IllegalArgumentException` (bad input) or `IllegalStateException` (rule broken, e.g. over 8 credits). Catch them in the UI and show `e.getMessage()` in a `JOptionPane`.

## Design decisions
- Balance = tuition of enrolled courses not yet paid (derived, never stored), so it can't drift.
- Dropping a paid course adds a Refund line to payment history; dropping an unpaid course just removes the charge.
- Passing grade = final and not F. IP (in progress) never counts toward GPA or credits.
- Tuition is `Course.TUITION_PER_CREDIT` = $1000 per credit (change in one place).
- Login/passwords are NOT in the model: use the provided Access Control package.
