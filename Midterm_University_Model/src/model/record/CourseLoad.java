package model.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.course.CourseOffering;
import model.course.SeatAssignment;
import model.course.Semester;
import model.person.StudentRole;
import model.university.Course;

/**
 * All of one student's SeatAssignments for one semester (Task 4 item 1:
 * the semester GPA is calculated here). Max 8 credit hours per semester.
 *
 * @author Porhay Rouen
 */
public class CourseLoad {

    public static final int MAX_CREDITS = 8;

    private final StudentRole student;
    private final Semester semester;
    private final List<SeatAssignment> seatAssignments = new ArrayList<>();

    CourseLoad(StudentRole student, Semester semester) {
        this.student = student;
        this.semester = semester;
    }

    public StudentRole getStudent() {
        return student;
    }

    public Semester getSemester() {
        return semester;
    }

    public List<SeatAssignment> getSeatAssignments() {
        return Collections.unmodifiableList(seatAssignments);
    }

    public void add(SeatAssignment sa) {
        seatAssignments.add(sa);
    }

    public void remove(SeatAssignment sa) {
        seatAssignments.remove(sa);
    }

    public SeatAssignment findByOffering(CourseOffering offering) {
        for (SeatAssignment sa : seatAssignments) {
            if (sa.getCourseOffering() == offering) {
                return sa;
            }
        }
        return null;
    }

    /** Credits of every course in this semester (graded or not). */
    public int getTotalCredits() {
        int total = 0;
        for (SeatAssignment sa : seatAssignments) {
            total += sa.getCredits();
        }
        return total;
    }

    /** Credits of courses that already have a final grade. */
    public int getGradedCredits() {
        int total = 0;
        for (SeatAssignment sa : seatAssignments) {
            if (sa.getGrade().isFinal()) {
                total += sa.getCredits();
            }
        }
        return total;
    }

    /** Sum of (grade points x credit hours) over final grades. */
    public double getQualityPoints() {
        double total = 0;
        for (SeatAssignment sa : seatAssignments) {
            if (sa.getGrade().isFinal()) {
                total += sa.getGrade().getPoints() * sa.getCredits();
            }
        }
        return total;
    }

    public boolean hasGradedCourses() {
        return getGradedCredits() > 0;
    }

    /** Term GPA = quality points / graded credit hours (0 when nothing is graded). */
    public double getSemesterGPA() {
        int credits = getGradedCredits();
        return credits == 0 ? 0 : getQualityPoints() / credits;
    }

    /** True when adding this course would stay within the 8-credit limit. */
    public boolean canAdd(Course course) {
        return getTotalCredits() + course.getCredits() <= MAX_CREDITS;
    }
}
