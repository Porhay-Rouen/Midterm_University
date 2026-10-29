package main;

import access.AccessControl;
import java.time.LocalDate;
import java.util.ArrayList;
import model.*;

/**
 * Builds the sample data: 1 university, 1 college, 1 department, 30 persons
 * (1 admin, 1 registrar, 12 faculty, 16 students), 3 semesters and 10 course offerings
 * with faculty assigned and students holding seat assignments.
 * Sample login: usernames admin, registrar, faculty1-12, student1-16; password is DEFAULT_PASSWORD.
 * @author (your name)
 */
public class ConfigureJTable {
    public static final String DEFAULT_PASSWORD = "Passw0rd!";
    private static final String[] FACULTY = {"Alan Smith", "Grace Lee", "Maria Garcia", "Chen Wei", "Priya Patel", "John Brown",
        "Sara Kim", "David Nguyen", "Laura Martin", "Omar Hassan", "Elena Rossi", "Tom Baker"};
    private static final String[] STUDENTS = {"Porhay Rouen", "Sokha Chan", "Dara Pich", "Vanna Sok", "Lina Ho", "Rith Mao",
        "Sophea Kem", "Bopha Ly", "Ravy Heng", "Kosal Tep", "Maly Chea", "Sambath Noun", "Nary Oum", "Pheak Seng", "Chantha Vong", "Visal Khun"};
    private static final String[] GRADES = {"A", "A-", "B+", "B", "A", "B-", "A-", "C+"};

    private University university;
    private Department department;
    private AccessControl accessControl = new AccessControl();

    public static ConfigureJTable configure() {
        ConfigureJTable c = new ConfigureJTable();
        c.build();
        return c;
    }

    private void build() {
        university = new University("Sample University");
        department = university.newCollege("College of Engineering").newDepartment("Information Systems");

        // people and accounts
        Person admin = department.registerPerson(RoleType.ADMIN, "Admin User", "admin@univ.edu", "617-555-0001");
        accessControl.createAccount("admin", DEFAULT_PASSWORD, admin.getRole(AdminRole.class));
        Person reg = department.registerPerson(RoleType.REGISTRAR, "Rita Registrar", "registrar@univ.edu", "617-555-0002");
        reg.getRole(RegistrarRole.class).setOfficeHours("Mon-Fri 9:00-12:00");
        accessControl.createAccount("registrar", DEFAULT_PASSWORD, reg.getRole(RegistrarRole.class));
        ArrayList<FacultyRole> fac = new ArrayList<>();
        for (int i = 0; i < FACULTY.length; i++) {
            Person p = department.registerPerson(RoleType.FACULTY, "Dr. " + FACULTY[i], email(FACULTY[i]), "617-555-" + (1000 + i));
            fac.add(p.getRole(FacultyRole.class));
            accessControl.createAccount("faculty" + (i + 1), DEFAULT_PASSWORD, p.getRole(FacultyRole.class));
        }
        ArrayList<StudentRole> stu = new ArrayList<>();
        for (int i = 0; i < STUDENTS.length; i++) {
            Person p = department.registerPerson(RoleType.STUDENT, STUDENTS[i], email(STUDENTS[i]), "617-555-" + (2000 + i));
            stu.add(p.getRole(StudentRole.class));
            accessControl.createAccount("student" + (i + 1), DEFAULT_PASSWORD, p.getRole(StudentRole.class));
        }

        // catalog and degree
        CourseCatalog cat = department.getCourseCatalog();
        Course c5100 = cat.newCourse("INFO 5100", "Application Engineering and Development", 4);
        Course c6150 = cat.newCourse("INFO 6150", "Web Design and User Experience", 4);
        Course c6205 = cat.newCourse("INFO 6205", "Program Structures and Algorithms", 4);
        Course c6215 = cat.newCourse("INFO 6215", "Software Architecture", 4);
        Course c6250 = cat.newCourse("INFO 6250", "Cloud Computing", 4);
        Course c6105 = cat.newCourse("INFO 6105", "Data Science Engineering Methods", 4);
        Course c5105 = cat.newCourse("INFO 5105", "Database Management Systems", 4);
        Course c6210 = cat.newCourse("INFO 6210", "Data Management and Database Design", 4);
        Degree msis = department.newDegree("Master of Information Systems");
        for (Course c : cat.getCourses()) msis.addCourse(c);

        // Spring 2026: finished, graded, paid
        Semester spring = new Semester("Spring 2026", LocalDate.of(2026, 1, 12));
        CourseSchedule sp = department.newCourseSchedule(spring);
        CourseOffering[] spOff = {sp.newCourseOffering(c5100, fac.get(0), 30), sp.newCourseOffering(c6205, fac.get(1), 30),
            sp.newCourseOffering(c6150, fac.get(2), 30)};
        for (int i = 0; i < 12; i++) {
            SeatAssignment a = stu.get(i).enroll(spOff[0]);
            SeatAssignment b = stu.get(i).enroll(spOff[1 + i % 2]);
            a.setGrade(GRADES[i % GRADES.length]);
            b.setGrade(GRADES[(i * 3 + 1) % GRADES.length]);
            stu.get(i).getTuitionAccount().pay();
        }
        for (CourseOffering o : spOff) o.closeEnrollment();

        // Fall 2026: in progress, coursework partly graded, half of the students paid
        Semester fall = new Semester("Fall 2026", LocalDate.of(2026, 9, 8));
        CourseSchedule fa = department.newCourseSchedule(fall);
        Course[] fallCourses = {c6215, c6250, c6105, c5105, c6210};
        CourseOffering[] fo = new CourseOffering[5];
        Assignment[] a1 = new Assignment[5];
        Assignment[] quiz = new Assignment[5];
        for (int k = 0; k < 5; k++) {
            fo[k] = fa.newCourseOffering(fallCourses[k], fac.get(3 + k), 25);
            fo[k].setSchedule("Tue/Thu 6:00-7:30 PM");
            fo[k].setRoom("Room " + (100 + k));
            a1[k] = fo[k].newAssignment("Assignment 1", 100);
            quiz[k] = fo[k].newAssignment("Quiz 1", 50);
        }
        for (int i = 0; i < 16; i++) {
            int[] picks = {i % 5, (i + 2) % 5};
            for (int k : picks) {
                SeatAssignment sa = stu.get(i).enroll(fo[k]);
                sa.getSubmission(a1[k]).submit("assignment1_" + i + ".pdf");
                if (i % 3 != 0) sa.getSubmission(a1[k]).grade(60 + (i * 7 + k * 5) % 40);
                if (i % 2 == 0) {
                    sa.getSubmission(quiz[k]).submit("quiz1_" + i + ".pdf");
                    sa.getSubmission(quiz[k]).grade(30 + (i * 3) % 20);
                }
            }
            if (i % 2 == 0) stu.get(i).getTuitionAccount().pay();
        }

        // Spring 2027: upcoming, open for registration, nobody enrolled yet
        Semester next = new Semester("Spring 2027", LocalDate.of(2027, 1, 19));
        CourseSchedule nx = department.newCourseSchedule(next);
        nx.newCourseOffering(c6150, fac.get(8), 25);
        nx.newCourseOffering(c6205, fac.get(9), 25);
    }

    private String email(String fullName) {
        return fullName.toLowerCase().replace(".", "").replace(" ", ".") + "@univ.edu";
    }

    public University getUniversity() { return university; }
    public Department getDepartment() { return department; }
    public AccessControl getAccessControl() { return accessControl; }
}
