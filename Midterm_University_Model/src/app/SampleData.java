package app;

import access.AccessControl;
import java.time.LocalDate;
import model.course.CourseOffering;
import model.course.CourseSchedule;
import model.course.Semester;
import model.person.FacultyRole;
import model.person.StudentRole;
import model.university.College;
import model.university.Course;
import model.university.Degree;
import model.university.Department;
import model.university.University;

/**
 * Sample data for the demo (the "ConfigureJTable" data): 1 department, 10 students,
 * 10 faculty, 1 admin, 1 registrar, 1 semester and 5 offerings with seat assignments.
 *
 * Demo logins (all use the password in DEMO_PASSWORD):
 *   admin, registrar, student1..student10, faculty1..faculty10
 *
 * @author Porhay Rouen
 */
public final class SampleData {

    /** Shared demo password; documented in the README. */
    public static final String DEMO_PASSWORD = "123456";

    private static final String[][] STUDENTS = {
        {"Porhay", "Rouen"}, {"Sokha", "Chan"}, {"Maya", "Patel"}, {"Liam", "Nguyen"}, {"Emma", "Garcia"},
        {"Noah", "Kim"}, {"Olivia", "Brown"}, {"Ethan", "Davis"}, {"Ava", "Wilson"}, {"Lucas", "Martin"}
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

    private SampleData() {
    }

    /** Builds the university, the logins and returns them in one AppContext. */
    public static AppContext load() {
        University uni = new University("Northeastern University");
        College college = uni.newCollege("College of Engineering");
        Department is = college.newDepartment("Information Systems");
        // Spring 2027 has not started yet, so faculty can still open/close enrollment.
        Semester semester = uni.newSemester("Spring 2027", LocalDate.of(2027, 1, 11), LocalDate.of(2027, 4, 30));

        Degree msis = is.newDegree("MSIS", 32);
        Course[] courses = new Course[COURSES.length];
        for (int i = 0; i < COURSES.length; i++) {
            courses[i] = is.getCourseCatalog().newCourse(COURSES[i][0], COURSES[i][1], 4, "");
            msis.addCourse(courses[i], i == 0); // INFO5100 is the core course
        }

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

        CourseSchedule schedule = is.newCourseSchedule(semester);
        String[] times = {"Mon 6-9 PM", "Tue 6-9 PM", "Wed 6-9 PM", "Thu 6-9 PM", "Fri 6-9 PM"};
        CourseOffering[] offerings = new CourseOffering[courses.length];
        for (int i = 0; i < courses.length; i++) {
            offerings[i] = schedule.newOffering(courses[i], faculty[i], 30, "R10" + (i + 1), times[i]);
        }

        // Every student takes 2 courses (8 credits = the limit); the first 5 students have paid.
        for (int i = 0; i < students.length; i++) {
            students[i].enroll(offerings[i % offerings.length]);
            students[i].enroll(offerings[(i + 1) % offerings.length]);
            if (i < 5) {
                students[i].pay();
            }
        }
        return new AppContext(uni, access);
    }
}
