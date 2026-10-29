package model;

import java.util.ArrayList;

/** One student's place in one course offering: grade, coursework and tuition status. @author (your name) */
public class SeatAssignment {
    private Seat seat;
    private CourseOffering offering;
    private StudentRole student;
    private String grade = GradeScale.IN_PROGRESS;
    private ArrayList<Submission> submissions = new ArrayList<>();
    private double billedAmount;
    private boolean paid;

    public SeatAssignment(Seat seat, CourseOffering offering, StudentRole student) {
        this.seat = seat;
        this.offering = offering;
        this.student = student;
    }

    /** Returns this student's submission for the assignment, creating an empty one if needed. */
    public Submission getSubmission(Assignment a) {
        for (Submission s : submissions) if (s.getAssignment() == a) return s;
        Submission s = new Submission(a);
        submissions.add(s);
        return s;
    }

    /** One submission per assignment of the offering (progress view). */
    public ArrayList<Submission> getSubmissions() {
        for (Assignment a : offering.getAssignments()) getSubmission(a);
        return submissions;
    }

    /** Percentage over graded assignments only, or -1 if nothing is graded yet. */
    public double getPercent() {
        double earned = 0, possible = 0;
        for (Submission s : submissions) {
            if (s.isGraded()) {
                earned += s.getScore();
                possible += s.getAssignment().getMaxPoints();
            }
        }
        return possible == 0 ? -1 : earned * 100.0 / possible;
    }

    /** Sets the final letter grade from the graded assignments. */
    public String computeFinalGrade() {
        double p = getPercent();
        if (p < 0) throw new IllegalStateException("No graded assignments yet.");
        grade = GradeScale.letterFor(p);
        return grade;
    }

    public void setGrade(String grade) {
        if (!GradeScale.IN_PROGRESS.equals(grade) && !GradeScale.isLetter(grade)) {
            throw new IllegalArgumentException("Invalid grade: " + grade);
        }
        this.grade = grade;
    }

    public boolean isGraded() { return GradeScale.isLetter(grade); }
    public double getGradePoints() { return GradeScale.points(grade); }
    public int getCredits() { return offering.getCourse().getCredits(); }

    public Seat getSeat() { return seat; }
    public CourseOffering getOffering() { return offering; }
    public StudentRole getStudent() { return student; }
    public String getGrade() { return grade; }
    public double getBilledAmount() { return billedAmount; }
    void setBilledAmount(double amount) { this.billedAmount = amount; }
    public boolean isPaid() { return paid; }
    void setPaid(boolean paid) { this.paid = paid; }
}
