package model;

import java.util.ArrayList;

/** A student's courses for one semester. Enforces the 8 credit hour limit. @author (your name) */
public class CourseLoad {
    public static final int MAX_CREDITS = 8;
    private Semester semester;
    private ArrayList<SeatAssignment> assignments = new ArrayList<>();

    public CourseLoad(Semester semester) { this.semester = Validator.requireNonNull(semester, "Semester"); }

    /** Adds an offering to this load. Throws IllegalStateException with the reason if it is not allowed. */
    SeatAssignment add(StudentRole student, CourseOffering offering) {
        if (!offering.isEnrollmentOpen()) {
            throw new IllegalStateException("Enrollment is closed for " + offering.getCourse().getCourseId() + ".");
        }
        for (SeatAssignment a : assignments) {
            if (a.getOffering().getCourse().getCourseId().equalsIgnoreCase(offering.getCourse().getCourseId())) {
                throw new IllegalStateException("Already enrolled in " + offering.getCourse().getCourseId() + " this semester.");
            }
        }
        if (getTotalCredits() + offering.getCourse().getCredits() > MAX_CREDITS) {
            throw new IllegalStateException("Credit limit exceeded: a student may take at most " + MAX_CREDITS + " credit hours per semester.");
        }
        Seat seat = offering.findOpenSeat();
        if (seat == null) throw new IllegalStateException("Course is full.");
        SeatAssignment sa = new SeatAssignment(seat, offering, student);
        seat.setAssignment(sa);
        assignments.add(sa);
        return sa;
    }

    void remove(SeatAssignment sa) {
        sa.getSeat().setAssignment(null);
        assignments.remove(sa);
    }

    public int getTotalCredits() {
        int total = 0;
        for (SeatAssignment a : assignments) total += a.getCredits();
        return total;
    }

    public int getGradedCredits() {
        int total = 0;
        for (SeatAssignment a : assignments) if (a.isGraded()) total += a.getCredits();
        return total;
    }

    public double getQualityPoints() {
        double q = 0;
        for (SeatAssignment a : assignments) if (a.isGraded()) q += a.getGradePoints() * a.getCredits();
        return q;
    }

    /** Term GPA = quality points / credit hours (graded courses only). 0 if nothing is graded. */
    public double getTermGPA() {
        int c = getGradedCredits();
        return c == 0 ? 0 : getQualityPoints() / c;
    }

    public Semester getSemester() { return semester; }
    public ArrayList<SeatAssignment> getSeatAssignments() { return assignments; }
}
