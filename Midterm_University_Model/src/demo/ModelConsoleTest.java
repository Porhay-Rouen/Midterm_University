package demo;

import java.time.LocalDate;
import model.course.Assignment;
import model.course.CourseOffering;
import model.course.CourseSchedule;
import model.course.SeatAssignment;
import model.course.Semester;
import model.person.AdminRole;
import model.person.FacultyRole;
import model.person.StudentRole;
import model.record.AcademicStanding;
import model.record.CourseLoad;
import model.record.Grade;
import model.record.Payment;
import model.report.UniversityReports;
import model.university.College;
import model.university.Course;
import model.university.Degree;
import model.university.Department;
import model.university.University;

/**
 * Console smoke test for the model. Prints PASS/FAIL per check.
 * Delete this file (or keep it off main) before final submission: the
 * assignment forbids debug prints in the submitted code.
 *
 * @author Porhay Rouen
 */
public class ModelConsoleTest {

    private static int failures = 0;

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) {
            failures++;
        }
    }

    private static boolean throwsError(Runnable r) {
        try {
            r.run();
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    public static void main(String[] args) {
        University uni = new University("Northeastern University");
        College college = uni.newCollege("College of Engineering");
        Department is = college.newDepartment("Information Systems");
        Semester spring = uni.newSemester("Spring 2026", LocalDate.of(2026, 1, 12), LocalDate.of(2026, 5, 1));
        Semester fall = uni.newSemester("Fall 2026", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15));

        Course c5100 = is.getCourseCatalog().newCourse("INFO5100", "App Eng & Dev", 4, "Core course");
        Course c6205 = is.getCourseCatalog().newCourse("INFO6205", "Prog Structures", 4, "");
        Course c6215 = is.getCourseCatalog().newCourse("INFO 6215", "Software Arch", 4, "");
        Degree msis = is.newDegree("MSIS", 32);
        msis.addCourse(c5100, true);
        msis.addCourse(c6205, false);
        msis.addCourse(c6215, false);

        AdminRole admin = uni.registerAdmin("Ada", "Admin", "ada@univ.edu", "");
        FacultyRole smith = uni.registerFaculty("John", "Smith", "j.smith@univ.edu", "617-555-0101", is);
        FacultyRole lee = uni.registerFaculty("Mei", "Lee", "m.lee@univ.edu", "", is);
        StudentRole porhay = uni.registerStudent("Porhay", "Rouen", "p.rouen@univ.edu", "", is, msis);
        StudentRole sokha = uni.registerStudent("Sokha", "Chan", "s.chan@univ.edu", "", is, msis);

        System.out.println("--- registration ---");
        check("auto ID student S001", porhay.getPerson().getUniversityId().equals("S001"));
        check("auto ID second student S002", sokha.getPerson().getUniversityId().equals("S002"));
        check("auto ID faculty F001", smith.getPerson().getUniversityId().equals("F001"));
        check("duplicate email blocked",
                throwsError(() -> uni.registerStudent("X", "Y", "P.ROUEN@univ.edu", "", is, msis)));
        check("bad email blocked", throwsError(() -> uni.registerAdmin("X", "Y", "not-an-email", "")));
        check("blank name blocked", throwsError(() -> uni.registerAdmin(" ", "Y", "z@univ.edu", "")));
        check("failed registration leaves no orphan", uni.findPersonByEmail("z@univ.edu") == null);
        check("search by name", uni.searchByName("rou").size() == 1);
        check("search by id", uni.searchById("F0").size() == 2);
        check("search by department", uni.searchByDepartment("information").size() == 4);

        System.out.println("--- catalog / schedule ---");
        check("catalog finds 'info 6215'", is.getCourseCatalog().findCourse("info 6215") == c6215);
        check("duplicate course blocked", throwsError(() -> is.getCourseCatalog().newCourse("INFO5100", "Dup", 4, "")));
        CourseSchedule springSched = is.newCourseSchedule(spring);
        CourseOffering o5100 = springSched.newOffering(c5100, smith, 30, "R101", "Mon 6-9 PM");
        CourseOffering o6205 = springSched.newOffering(c6205, lee, 25, "R102", "Tue 6-9 PM");
        CourseOffering o6215 = springSched.newOffering(c6215, smith, 2, "R103", "Wed 6-9 PM");
        check("find offering by course number", springSched.findByCourseNumber("INFO6215") == o6215);
        check("3 search methods", springSched.searchByCourseId("62").size() == 2
                && springSched.searchByTeacher("smith").size() == 2
                && springSched.searchByCourseName("arch").size() == 1);
        check("duplicate offering blocked", throwsError(() -> springSched.newOffering(c5100, smith, 5, "", "")));

        System.out.println("--- enrollment and credit limit ---");
        porhay.enroll(o5100);
        porhay.enroll(o6205);
        check("enrolled count (Task 4 item 5)", o5100.getEnrolledCount() == 1);
        check("8-credit limit enforced", throwsError(() -> porhay.enroll(o6215)));
        check("double enroll blocked", throwsError(() -> porhay.enroll(o5100)));
        check("seat released after failed enroll", o6215.getEnrolledCount() == 0);

        System.out.println("--- tuition ---");
        check("balance billed 8000", porhay.getBalance() == 8000.0);
        check("transcript locked while unpaid", !porhay.canViewTranscript());
        Payment p = porhay.pay();
        check("payment recorded", p != null && p.getAmount() == 8000.0 && porhay.getPayments().size() == 1);
        check("balance zero after pay", porhay.getBalance() == 0 && porhay.canViewTranscript());
        check("second pay returns null and adds no history", porhay.pay() == null && porhay.getPayments().size() == 1);

        sokha.enroll(o6215);
        sokha.pay();
        sokha.drop(o6215);
        check("drop after pay creates refund",
                sokha.getPayments().size() == 2 && sokha.getPayments().get(1).isRefund());
        check("net paid is zero after refund", sokha.getNetTuitionPaid() == 0);
        check("seat freed after drop", o6215.getEnrolledCount() == 0);
        sokha.enroll(o6205);
        sokha.drop(o6205);
        check("drop unpaid course removes the charge", sokha.getBalance() == 0 && sokha.getPayments().size() == 2);

        System.out.println("--- grading ---");
        Assignment hw1 = o5100.addAssignment("HW1", 50);
        Assignment hw2 = o5100.addAssignment("HW2", 50);
        SeatAssignment sa5100 = porhay.getTranscript().findCourseLoad(spring).findByOffering(o5100);
        sa5100.submit(hw1, "hw1.pdf");
        sa5100.recordScore(hw1, 48);
        sa5100.recordScore(hw2, 46);
        check("percentage 94", Math.abs(sa5100.getPercentage() - 94.0) < 1e-9);
        check("auto final letter grade A", sa5100.computeFinalGrade() == Grade.A);
        check("score above max blocked", throwsError(() -> sa5100.recordScore(hw1, 51)));
        SeatAssignment sa6205 = porhay.getTranscript().findCourseLoad(spring).findByOffering(o6205);
        check("not graded yet is IP", sa6205.getGrade() == Grade.IP);
        sa6205.setGrade(Grade.fromLabel("A-"));

        System.out.println("--- GPA / transcript ---");
        CourseLoad load = porhay.getTranscript().findCourseLoad(spring);
        check("semester GPA 3.85", Math.abs(load.getSemesterGPA() - 3.85) < 1e-9);
        check("overall GPA 3.85", Math.abs(porhay.getTranscript().getOverallGPA() - 3.85) < 1e-9);
        check("standing Good Standing", porhay.getTranscript().getStanding(spring) == AcademicStanding.GOOD_STANDING);
        check("drop graded course blocked", throwsError(() -> porhay.drop(o5100)));
        check("class GPA for INFO5100", Math.abs(o5100.getClassGPA() - 4.0) < 1e-9);
        check("grade distribution A=1", o5100.getGradeDistribution().get(Grade.A) == 1);
        check("ranked list has 1 student", o5100.getRankedSeatAssignments().size() == 1);
        check("faculty tuition collected 4000", smith.getTotalTuitionCollected() == 4000.0);

        System.out.println("--- graduation audit ---");
        check("not ready: 8 of 32 credits", !porhay.isReadyToGraduate() && porhay.getCreditsCompleted() == 8);
        check("core INFO5100 completed", porhay.isCoreCompleted());
        check("progress 25%", porhay.getGraduationProgressPercent() == 25);

        System.out.println("--- standing rules ---");
        check("warning rule", AcademicStanding.determine(2.5, 3.2) == AcademicStanding.ACADEMIC_WARNING);
        check("probation rule", AcademicStanding.determine(3.5, 2.9) == AcademicStanding.ACADEMIC_PROBATION);
        check("good rule", AcademicStanding.determine(3.0, 3.0) == AcademicStanding.GOOD_STANDING);

        System.out.println("--- capacity and faculty reassignment ---");
        check("capacity below enrolled blocked", throwsError(() -> o5100.setCapacity(0)));
        o5100.setCapacity(40);
        check("capacity grows", o5100.getCapacity() == 40 && o5100.getOpenSeats() == 39);
        o5100.setCapacity(10);
        check("capacity shrinks", o5100.getCapacity() == 10 && o5100.getEnrolledCount() == 1);
        o5100.setEnrollmentOpen(false);
        check("closed enrollment blocks enroll", throwsError(() -> sokha.enroll(o5100)));
        o5100.assignFaculty(lee);
        check("reassign faculty", lee.getOfferings().contains(o5100) && !smith.getOfferings().contains(o5100));
        check("delete faculty with courses blocked", throwsError(() -> uni.deletePerson(lee.getPerson())));

        System.out.println("--- reports ---");
        UniversityReports reports = new UniversityReports(uni);
        check("active users: students 2", reports.activeUsersByRole().get("Student") == 2);
        check("active users: faculty 2", reports.activeUsersByRole().get("Faculty") == 2);
        check("courses per semester Spring = 3", reports.coursesOfferedPerSemester().get("Spring 2026") == 3);
        check("revenue total 8000", reports.tuitionRevenueTotal() == 8000.0);
        check("collected for Spring 8000", reports.tuitionCollected(spring) == 8000.0);
        check("unpaid for Spring 0", reports.tuitionUnpaid(spring) == 0.0);
        check("enrolled per course rows", reports.enrolledPerCourse().size() == 3);
        check("GPA distribution row for MSIS", reports.gpaDistributionByProgram().size() == 1);

        System.out.println("--- delete accounts ---");
        uni.deletePerson(sokha.getPerson());
        check("student deleted", uni.findPersonById("S002") == null && is.getStudentDirectory().getStudents().size() == 1);
        uni.deletePerson(porhay.getPerson());
        check("deleting student frees seats", o5100.getEnrolledCount() == 0);

        System.out.println(failures == 0 ? "\nALL CHECKS PASSED" : "\n" + failures + " CHECK(S) FAILED");
    }
}
