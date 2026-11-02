package model.course;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import model.person.StudentRole;
import model.record.Grade;
import model.university.Course;
import model.util.Validation;

/**
 * One student in one seat of one course offering (Task 4 item 4: the letter
 * grade is stored and set here). Also holds assignment scores, submissions
 * and the tuition billed for this enrollment.
 *
 * @author Porhay Rouen
 */
public class SeatAssignment {

    private final Seat seat;
    private final StudentRole student;
    private Grade grade = Grade.IP;
    private final double tuition;
    private boolean paid = false;
    private final Map<Assignment, Double> scores = new LinkedHashMap<>();
    private final Map<Assignment, String> submissions = new LinkedHashMap<>();

    SeatAssignment(Seat seat, StudentRole student) {
        this.seat = seat;
        this.student = student;
        this.tuition = seat.getCourseOffering().getCourse().getTuition();
    }

    public Seat getSeat() {
        return seat;
    }

    public StudentRole getStudent() {
        return student;
    }

    public CourseOffering getCourseOffering() {
        return seat.getCourseOffering();
    }

    public Course getCourse() {
        return getCourseOffering().getCourse();
    }

    public Semester getSemester() {
        return getCourseOffering().getSemester();
    }

    public int getCredits() {
        return getCourse().getCredits();
    }

    public Grade getGrade() {
        return grade;
    }

    /** Sets the letter grade by hand (the Grading page "Save Grade" button). */
    public void setGrade(Grade grade) {
        this.grade = Validation.requireNonNull(grade, "Grade");
    }

    public double getTuition() {
        return tuition;
    }

    public boolean isPaid() {
        return paid;
    }

    public void markPaid() {
        this.paid = true;
    }

    // ---- assignments: submissions and scores ----

    /** Student submits a file path for an assignment. */
    public void submit(Assignment assignment, String filePath) {
        Validation.requireNonNull(assignment, "Assignment");
        submissions.put(assignment, Validation.requireText(filePath, "Submission file"));
    }

    public String getSubmission(Assignment assignment) {
        return submissions.get(assignment);
    }

    /** Faculty records a score between 0 and the assignment's maximum. */
    public void recordScore(Assignment assignment, double score) {
        Validation.requireNonNull(assignment, "Assignment");
        if (score < 0 || score > assignment.getMaxScore()) {
            throw new IllegalArgumentException(
                    "Score must be between 0 and " + assignment.getMaxScore() + ".");
        }
        scores.put(assignment, score);
    }

    /** Score for an assignment, or null when not graded yet. */
    public Double getScore(Assignment assignment) {
        return scores.get(assignment);
    }

    public Map<Assignment, Double> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    public boolean hasScores() {
        return !scores.isEmpty();
    }

    /** Total grade percentage over the assignments graded so far (0 when none). */
    public double getPercentage() {
        double earned = 0;
        double max = 0;
        for (Map.Entry<Assignment, Double> e : scores.entrySet()) {
            earned += e.getValue();
            max += e.getKey().getMaxScore();
        }
        return max == 0 ? 0 : earned / max * 100.0;
    }

    /** Computes the final letter grade from the percentage, stores it and returns it. */
    public Grade computeFinalGrade() {
        if (!hasScores()) {
            throw new IllegalStateException("No assignments have been graded yet.");
        }
        grade = Grade.fromPercentage(getPercentage());
        return grade;
    }

    /** Percent of the offering's assignments this student has submitted (progress report). */
    public double getSubmissionProgress() {
        int total = getCourseOffering().getAssignments().size();
        return total == 0 ? 0 : submissions.size() * 100.0 / total;
    }

    void forgetAssignment(Assignment assignment) {
        scores.remove(assignment);
        submissions.remove(assignment);
    }
}
