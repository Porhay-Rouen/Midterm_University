package app;

import access.AccessControl;
import java.time.LocalDate;
import model.course.Assignment;
import model.course.CourseOffering;
import model.course.CourseSchedule;
import model.course.SeatAssignment;
import model.course.Semester;
import model.person.FacultyRole;
import model.person.StudentRole;
import model.university.College;
import model.university.Course;
import model.university.Degree;
import model.university.Department;
import model.university.University;

/**
 * Sample data for the demo (the "ConfigureJTable" data): 2 departments, 18 students,
 * 11 faculty, 1 admin, 1 registrar and 3 semesters.
 *   Fall 2026   finished: 3 assignments per offering, every score entered, final grades computed, tuition paid.
 *   Spring 2027 current: 3 assignments per offering, the first 2 scored, grades still in progress (IP),
 *               first 8 students paid, one refund (a paid student dropped a course).
 *   Summer 2027 upcoming: one Computer Science offering.
 *
 * Demo logins (all use the password in DEMO_PASSWORD):
 *   admin, registrar, student1..student18, faculty1..faculty11
 *
 * @author Porhay Rouen
 */
public final class SampleData {

    /** Shared demo password; documented in the README. */
    public static final String DEMO_PASSWORD = "123456";

    private static final String[][] STUDENTS = {
        {"Porhay", "Rouen"}, {"Sokha", "Chan"}, {"Maya", "Patel"}, {"Liam", "Nguyen"}, {"Emma", "Garcia"},
        {"Noah", "Kim"}, {"Olivia", "Brown"}, {"Ethan", "Davis"}, {"Ava", "Wilson"}, {"Lucas", "Martin"},
        {"Mia", "Lopez"}, {"Jack", "Taylor"}, {"Zoe", "Clark"}, {"Ryan", "Adams"}, {"Lily", "Young"},
        {"Omar", "Khalil"}, {"Sara", "Lindgren"}, {"Dev", "Patel"}
    };

    private static final String[][] FACULTY = {
        {"John", "Smith"}, {"Mei", "Lee"}, {"Priya", "Shah"}, {"David", "Wu"}, {"Sara", "Cohen"},
        {"Omar", "Haddad"}, {"Julia", "Rossi"}, {"Kenji", "Sato"}, {"Anna", "Novak"}, {"Carlos", "Diaz"}
    };

    private static final String[][] COURSES = {
        {"INFO5100", "Application Engineering and Development"},
        {"INFO6205", "Program Structure and Algorithms"},
        {"INFO6150", "Web Design and User Experience"},
        {"INFO6215", "Software Architecture"},
        {"INFO6250", "Data Management and Database Design"}
    };

    private static final String[] TIMES = {"Mon 6-9 PM", "Tue 6-9 PM", "Wed 6-9 PM", "Thu 6-9 PM", "Fri 6-9 PM"};

    private SampleData() {
    }

    /** Every offering gets the same 3 assignments (100 points each). */
    private static void addStandardAssignments(CourseOffering offering) {
        offering.addAssignment("Homework 1", 100);
        offering.addAssignment("Midterm", 100);
        offering.addAssignment("Final Project", 100);
    }

    /**
     * Records scores for the first 'count' assignments of every enrolled student.
     * Each student has a fixed "ability" (70 to 98) so GPAs differ from student to student,
     * and every score is a fixed number (no randomness), so the demo is always the same.
     */
    private static void scoreStudents(CourseOffering offering, int offeringIndex, int count,
            java.util.Map<StudentRole, Integer> studentIndex) {
        for (SeatAssignment sa : offering.getSeatAssignments()) {
            int ability = 70 + (studentIndex.get(sa.getStudent()) * 7) % 29;
            int k = 0;
            for (Assignment a : offering.getAssignments()) {
                if (k >= count) {
                    break;
                }
                double score = ability + (offeringIndex * 3 + k * 5) % 6;
                sa.recordScore(a, Math.min(score, a.getMaxScore()));
                k++;
            }
        }
    }

    /** Builds the university, the logins and returns them in one AppContext. */
    public static AppContext load() {
        University uni = new University("Northeastern University");
        College college = uni.newCollege("College of Engineering");
        Department is = college.newDepartment("Information Systems");
        // Fall 2026 is finished (used for grades and GPA); Spring 2027 has not started yet,
        // so faculty can still open/close enrollment.
        Semester fall = uni.newSemester("Fall 2026", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 15));
        Semester semester = uni.newSemester("Spring 2027", LocalDate.of(2027, 1, 11), LocalDate.of(2027, 4, 30));

        Degree msis = is.newDegree("MSIS", 32);
        Course[] courses = new Course[COURSES.length];
        for (int i = 0; i < COURSES.length; i++) {
            courses[i] = is.getCourseCatalog().newCourse(COURSES[i][0], COURSES[i][1], 4, "");
            msis.addCourse(courses[i], i == 0); // INFO5100 is the core course
        }

        // Login accounts are created through AccessControl, so it must exist before any account is made.
        AccessControl access = new AccessControl(uni);
        char[] pw = DEMO_PASSWORD.toCharArray();

        access.createAccount("admin", pw, uni.registerAdmin("Ada", "Admin", "ada.admin@univ.edu", "").getPerson());
        access.createAccount("registrar", pw,
                uni.registerRegistrar("Rita", "Registrar", "rita.registrar@univ.edu", "").getPerson());

        FacultyRole[] faculty = new FacultyRole[FACULTY.length];
        for (int i = 0; i < FACULTY.length; i++) {
            faculty[i] = uni.registerFaculty(FACULTY[i][0], FACULTY[i][1],
                    FACULTY[i][0].toLowerCase() + "." + FACULTY[i][1].toLowerCase() + "@univ.edu", "", is);
            access.createAccount("faculty" + (i + 1), pw, faculty[i].getPerson());
        }

        StudentRole[] students = new StudentRole[STUDENTS.length];
        for (int i = 0; i < STUDENTS.length; i++) {
            students[i] = uni.registerStudent(STUDENTS[i][0], STUDENTS[i][1],
                    STUDENTS[i][0].toLowerCase() + "." + STUDENTS[i][1].toLowerCase() + "@univ.edu", "", is, msis);
            access.createAccount("student" + (i + 1), pw, students[i].getPerson());
        }

        java.util.Map<StudentRole, Integer> studentIndex = new java.util.HashMap<>();
        for (int i = 0; i < students.length; i++) {
            studentIndex.put(students[i], i);
        }

        // ---- Fall 2026: finished semester. Scores for every assignment, final grades, tuition paid ----
        CourseSchedule fallSchedule = is.newCourseSchedule(fall);
        CourseOffering[] fallOfferings = new CourseOffering[courses.length];
        for (int i = 0; i < courses.length; i++) {
            fallOfferings[i] = fallSchedule.newOffering(courses[i], faculty[(i + 5) % faculty.length], 30,
                    "R20" + (i + 1), TIMES[i]);
            addStandardAssignments(fallOfferings[i]);
        }
        for (int i = 0; i < students.length; i++) {
            students[i].enroll(fallOfferings[i % fallOfferings.length]);
            students[i].enroll(fallOfferings[(i + 1) % fallOfferings.length]);
            students[i].pay();   // everybody paid Fall: one payment record each
        }
        for (int i = 0; i < fallOfferings.length; i++) {
            scoreStudents(fallOfferings[i], i, 3, studentIndex);         // all 3 assignments scored
            for (SeatAssignment sa : fallOfferings[i].getSeatAssignments()) {
                sa.computeFinalGrade();                                  // final letter grade from the percentage
            }
        }

        // ---- Spring 2027: current semester. 2 of 3 assignments scored, grades still IP ----
        CourseSchedule schedule = is.newCourseSchedule(semester);
        CourseOffering[] offerings = new CourseOffering[courses.length];
        for (int i = 0; i < courses.length; i++) {
            offerings[i] = schedule.newOffering(courses[i], faculty[i], 30, "R10" + (i + 1), TIMES[i]);
            addStandardAssignments(offerings[i]);
        }

        // Every student takes 2 courses (8 credits = the limit); the first 8 students have paid Spring.
        for (int i = 0; i < students.length; i++) {
            students[i].enroll(offerings[i % offerings.length]);
            students[i].enroll(offerings[(i + 1) % offerings.length]);
            if (i < 8) {
                students[i].pay();
            }
        }
        for (int i = 0; i < offerings.length; i++) {
            scoreStudents(offerings[i], i, 2, studentIndex);
        }
        // A paid student drops one course: the model refunds it, so Payment History shows a refund.
        students[2].drop(offerings[3]);

        // ---- Extra data: second department, second degree, third semester ----
        Department cs = college.newDepartment("Computer Science");
        Degree mscs = cs.newDegree("MSCS", 32);
        Course algo = cs.getCourseCatalog().newCourse("CS5800", "Algorithms", 4, "");
        Course data = cs.getCourseCatalog().newCourse("CS5200", "Database Management", 4, "");
        mscs.addCourse(algo, true);    // true = core course
        mscs.addCourse(data, false);
        FacultyRole csTeacher = uni.registerFaculty("Maya", "Singh", "maya.singh@univ.edu", "", cs);
        access.createAccount("faculty11", pw, csTeacher.getPerson());

        Degree msdata = is.newDegree("MS Data Analytics", 32);
        msdata.addCourse(courses[1], true);

        Semester summer = uni.newSemester("Summer 2027", LocalDate.of(2027, 5, 10), LocalDate.of(2027, 8, 15));
        CourseSchedule csSchedule = cs.newCourseSchedule(summer);
        csSchedule.newOffering(algo, csTeacher, 30, "R301", "Mon 6-9 PM");

        return new AppContext(uni, access);
    }
}