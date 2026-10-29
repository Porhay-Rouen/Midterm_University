package main;

import access.AccessControl;
import java.time.LocalDate;
import java.util.ArrayList;
import model.*;

/** Console checks for the requirements. Run it before the UI is built. @author (your name) */
public class TestRun {
    private static int failures = 0;

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }

    private static boolean throwsMessage(Runnable r, String contains) {
        try { r.run(); return false; } catch (RuntimeException e) { return e.getMessage() != null && e.getMessage().contains(contains); }
    }

    public static void main(String[] args) {
        ConfigureJTable cfg = ConfigureJTable.configure();
        final Department dept = cfg.getDepartment();
        final AccessControl ac = cfg.getAccessControl();
        Reports rep = new Reports(dept);
        String pw = ConfigureJTable.DEFAULT_PASSWORD;

        System.out.println("== Sample data ==");
        check("30 persons", dept.getPersonDirectory().getPersons().size() == 30);
        check("16 students, 12 faculty, 1 admin, 1 registrar", dept.getStudentDirectory().getStudents().size() == 16
                && dept.getFacultyDirectory().getFaculty().size() == 12 && dept.getAdminDirectory().getAdmins().size() == 1
                && dept.getRegistrarDirectory().getRegistrars().size() == 1);
        check("3 semesters, Fall 2026 has 5 offerings", dept.getCourseSchedules().size() == 3
                && dept.getCourseSchedule(new Semester("Fall 2026", LocalDate.now())).getOfferings().size() == 5);

        System.out.println("\n== Access control ==");
        check("admin login works", ac.login("admin", pw) != null && ac.isAuthorized(AdminRole.class));
        check("admin is not authorized as student", !ac.isAuthorized(StudentRole.class));
        check("requireAuthorized blocks wrong role", throwsMessage(new Runnable() { public void run() { ac.requireAuthorized(StudentRole.class); } }, "Access denied"));
        ac.logout();
        check("logout clears the session", !ac.isLoggedIn() && !ac.isAuthorized(AdminRole.class));
        check("wrong password returns null", ac.login("admin", "wrong") == null);
        check("empty username is rejected", throwsMessage(new Runnable() { public void run() { ac.login("", ""); } }, "required"));
        check("student login", ac.login("student1", pw) != null && ac.getCurrentRole() instanceof StudentRole);
        ac.logout();

        System.out.println("\n== Admin: registration, search, delete ==");
        Person np = dept.registerPerson(RoleType.STUDENT, "New Student", "new.student@univ.edu", "617-555-9999");
        check("unique university ID generated", np.getPersonId().startsWith("U") && dept.getPersonDirectory().findById(np.getPersonId()) == np);
        check("duplicate email rejected", throwsMessage(new Runnable() { public void run() { dept.registerPerson(RoleType.FACULTY, "Other", "NEW.student@univ.edu", ""); } }, "already registered"));
        check("invalid email rejected", throwsMessage(new Runnable() { public void run() { dept.registerPerson(RoleType.FACULTY, "X", "bad-email", ""); } }, "invalid"));
        check("search by name", dept.getPersonDirectory().searchByName("porhay").size() == 1);
        check("search by ID", dept.getPersonDirectory().searchById(np.getPersonId()).size() == 1);
        check("search by department", dept.getPersonDirectory().searchByDepartment("information").size() == 31);
        Reports r2 = new Reports(dept);
        check("active users by role", r2.activeUsersByRole().get("Student") == 17 && r2.activeUsersByRole().get("Faculty") == 12);
        CourseOffering busy = dept.getAllOfferings().get(0);
        FacultyRole busyFaculty = busy.getFaculty();
        check("cannot delete faculty who still teaches", throwsMessage(fac(busyFaculty, dept), "Reassign"));

        System.out.println("\n== Student: registration rules ==");
        StudentRole s0 = dept.getStudentDirectory().findById("U100015").getPerson().getRole(StudentRole.class);
        final StudentRole s0f = s0;
        final CourseOffering third = dept.getCourseSchedule(new Semester("Fall 2026", LocalDate.now())).getOfferings().get(4);
        ArrayList<CourseOffering> fall = dept.getCourseSchedule(new Semester("Fall 2026", LocalDate.now())).getOfferings();
        StudentRole fresh = np.getRole(StudentRole.class);
        final Semester nextSem = new Semester("Spring 2027", LocalDate.now());
        CourseSchedule ns = dept.getCourseSchedule(nextSem);
        final CourseOffering n1 = ns.getOfferings().get(0);
        final CourseOffering n2 = ns.getOfferings().get(1);
        check("3 search methods", ns.searchByCourseId("6150").size() == 1 && ns.searchByTeacher("Laura").size() == 1 && ns.searchByTitle("algorithms").size() == 1);
        check("new student balance starts at 0", fresh.getTuitionAccount().getBalance() == 0);
        check("pay with 0 balance does nothing", fresh.getTuitionAccount().pay() == null && fresh.getTuitionAccount().getHistory().isEmpty());
        fresh.enroll(n1);
        check("tuition billed on enroll (4 credits x 1500)", fresh.getTuitionAccount().getBalance() == 6000.0);
        check("transcript locked while unpaid", !fresh.canViewTranscript());
        fresh.enroll(n2);
        final StudentRole freshF = fresh;
        final CourseOffering n3 = ns.newCourseOffering(dept.getCourseCatalog().findCourse("INFO 6215"), dept.getFacultyDirectory().getFaculty().get(0), 5);
        check("8 credit hour limit", throwsMessage(new Runnable() { public void run() { freshF.enroll(n3); } }, "8 credit"));
        check("pay clears balance and unlocks transcript", fresh.getTuitionAccount().pay() != null && fresh.getTuitionAccount().getBalance() == 0 && fresh.canViewTranscript());
        check("payment history recorded", fresh.getTuitionAccount().getHistory().size() == 1 && fresh.getTuitionAccount().getNetPaid() == 12000.0);
        fresh.drop(fresh.getTranscript().getCourseLoad(nextSem).getSeatAssignments().get(0));
        check("refund recorded after dropping a paid course", fresh.getTuitionAccount().getHistory().size() == 2
                && fresh.getTuitionAccount().getNetPaid() == 6000.0 && fresh.getTuitionAccount().getBalance() == 0);
        check("seat freed after drop", n1.getEnrolledCount() == 0);
        check("closed enrollment blocks registration", throwsMessage(new Runnable() { public void run() { freshF.enroll(dept.getCourseSchedule(new Semester("Spring 2026", LocalDate.now())).getOfferings().get(0)); } }, "closed"));
        check("already enrolled is blocked", throwsMessage(new Runnable() { public void run() { freshF.enroll(n2); } }, "Already"));

        System.out.println("\n== Transcript, GPA, standing ==");
        Semester t1 = new Semester("T1", LocalDate.now().plusDays(60));
        Semester t2 = new Semester("T2", LocalDate.now().plusDays(120));
        FacultyRole f = dept.getFacultyDirectory().getFaculty().get(0);
        Course x = dept.getCourseCatalog().findCourse("INFO 6215"), y = dept.getCourseCatalog().findCourse("INFO 6250");
        Course z = dept.getCourseCatalog().findCourse("INFO 6105"), w = dept.getCourseCatalog().findCourse("INFO 5105");
        StudentRole gs = dept.registerPerson(RoleType.STUDENT, "GPA Student", "gpa@univ.edu", "").getRole(StudentRole.class);
        CourseSchedule s1 = dept.newCourseSchedule(t1), s2 = dept.newCourseSchedule(t2);
        gs.enroll(s1.newCourseOffering(x, f, 5)); gs.enroll(s1.newCourseOffering(y, f, 5));
        gs.enroll(s2.newCourseOffering(z, f, 5)); gs.enroll(s2.newCourseOffering(w, f, 5));
        gs.getTranscript().getCourseLoad(t1).getSeatAssignments().get(0).setGrade("A");
        gs.getTranscript().getCourseLoad(t1).getSeatAssignments().get(1).setGrade("A");
        gs.getTranscript().getCourseLoad(t2).getSeatAssignments().get(0).setGrade("B-");
        gs.getTranscript().getCourseLoad(t2).getSeatAssignments().get(1).setGrade("C");
        check("term GPA T1 = 4.0", Math.abs(gs.getTranscript().getCourseLoad(t1).getTermGPA() - 4.0) < 0.001);
        check("term GPA T2 = 2.35", Math.abs(gs.getTranscript().getCourseLoad(t2).getTermGPA() - 2.35) < 0.001);
        check("overall GPA = 3.175", Math.abs(gs.getTranscript().getOverallGPA() - 3.175) < 0.001);
        ArrayList<TranscriptRecord> rec = gs.getTranscript().getRecords(null);
        check("T1 Good Standing, T2 Academic Warning", rec.get(0).getStanding().equals("Good Standing") && rec.get(2).getStanding().equals("Academic Warning"));
        check("semester filter returns only that term", gs.getTranscript().getRecords(t2).size() == 2);
        check("transcript row has 7 columns", rec.get(0).toRow().length == Transcript.COLUMNS.length);
        check("probation when overall GPA < 3.0", Transcript.standingFor(3.5, 2.9).equals("Academic Probation"));

        System.out.println("\n== Graduation audit ==");
        StudentRole grad = dept.registerPerson(RoleType.STUDENT, "Grad Student", "grad@univ.edu", "").getRole(StudentRole.class);
        String[] ids = {"INFO 5100", "INFO 6150", "INFO 6205", "INFO 6215", "INFO 6250", "INFO 6105", "INFO 5105", "INFO 6210"};
        for (int i = 0; i < 4; i++) {
            Semester sm = new Semester("G" + i, LocalDate.now().plusDays(200 + i));
            CourseSchedule gsch = dept.newCourseSchedule(sm);
            for (int j = 0; j < 2; j++) {
                SeatAssignment sa = grad.enroll(gsch.newCourseOffering(dept.getCourseCatalog().findCourse(ids[i * 2 + j]), f, 5));
                sa.setGrade("A");
            }
        }
        check("32 credits with core course: ready to graduate", grad.getEarnedCredits() == 32 && grad.isReadyToGraduate());
        StudentRole almost = dept.getStudentDirectory().findById("U100015").getPerson().getRole(StudentRole.class);
        check("not ready: message lists what is missing", !almost.isReadyToGraduate() && almost.getGraduationMessage().contains("more credit hours"));

        System.out.println("\n== Faculty: grading and reports ==");
        CourseOffering off = fall.get(0);
        check("enrolled count (seats with a SeatAssignment)", off.getEnrolledCount() == off.getEnrolledAssignments().size() && off.getEnrolledCount() > 0);
        SeatAssignment best = off.rankStudents().get(0);
        check("ranking: highest percentage first", best.getPercent() >= off.rankStudents().get(1).getPercent());
        SeatAssignment sa0 = off.getEnrolledAssignments().get(0);
        double p = sa0.getPercent();
        if (p >= 0) check("final letter computed from percentage", sa0.computeFinalGrade().equals(GradeScale.letterFor(p)));
        check("class GPA and distribution available", off.getClassGPA() >= 0 && off.getGradeDistribution().size() == 9);
        check("cannot lower capacity below enrollment", throwsMessage(new Runnable() { public void run() { fall_get(dept).setCapacity(1); } }, "enrollment"));
        check("cannot change enrollment after semester started", throwsMessage(new Runnable() { public void run() { fall_get(dept).setEnrollmentOpen(false); } }, "before the semester starts"));
        s1.getOfferings().get(0).setEnrollmentOpen(false);
        check("can close enrollment before semester starts", !s1.getOfferings().get(0).isEnrollmentOpen());
        check("score above maximum rejected", throwsMessage(new Runnable() { public void run() { SeatAssignment a = fall_get(dept).getEnrolledAssignments().get(0); a.getSubmission(fall_get(dept).getAssignments().get(0)).grade(500); } }, "between"));
        check("faculty tuition insight", off.getTuitionCollected() >= 0);

        System.out.println("\n== Registrar and reports ==");
        check("courses per semester", rep.coursesPerSemester().get("Fall 2026") == 5);
        check("enrollment by course rows", rep.enrollmentByCourse(null).size() >= 10);
        check("tuition revenue is positive", rep.tuitionRevenue() > 0);
        Semester fallSem = new Semester("Fall 2026", LocalDate.now());
        check("collected + unpaid reports", rep.tuitionCollected(fallSem) > 0 && rep.unpaidTuition(fallSem) > 0);
        check("revenue by department", rep.revenueByDepartment().containsKey("Information Systems"));
        check("GPA distribution rows", !rep.gpaDistribution().isEmpty() && rep.gpaDistribution().get(0).length == 5);
        check("tuition status rows for every student", rep.tuitionStatusRows().size() == dept.getStudentDirectory().getStudents().size());

        System.out.println("\n== Delete student frees seats ==");
        StudentRole del = dept.getStudentDirectory().findById("U100017").getPerson().getRole(StudentRole.class);
        int before = fall.get(0).getEnrolledCount() + fall.get(1).getEnrolledCount() + fall.get(2).getEnrolledCount() + fall.get(3).getEnrolledCount() + fall.get(4).getEnrolledCount();
        int delCount = 0;
        for (CourseLoad cl : del.getTranscript().getCourseLoads()) if (cl.getSemester().getName().equals("Fall 2026")) delCount = cl.getSeatAssignments().size();
        Person delPerson = del.getPerson();
        dept.deletePerson(delPerson);
        ac.removeAccountsFor(delPerson);
        int after = fall.get(0).getEnrolledCount() + fall.get(1).getEnrolledCount() + fall.get(2).getEnrolledCount() + fall.get(3).getEnrolledCount() + fall.get(4).getEnrolledCount();
        check("deleted student's seats are freed", before - after == delCount && delCount > 0);
        check("deleted student's account removed", ac.findByUsername("student3") == null || ac.findByUsername("student3").getRole().getPerson() != delPerson);

        System.out.println("\n" + (failures == 0 ? "ALL CHECKS PASSED" : failures + " CHECK(S) FAILED"));
    }

    private static CourseOffering fall_get(Department d) {
        return d.getCourseSchedule(new Semester("Fall 2026", LocalDate.now())).getOfferings().get(0);
    }
    private static Runnable fac(final FacultyRole f, final Department d) {
        return new Runnable() { public void run() { d.deletePerson(f.getPerson()); } };
    }
}
