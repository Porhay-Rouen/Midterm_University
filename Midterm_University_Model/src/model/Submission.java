package model;

import java.time.LocalDateTime;

/** One student's work for one assignment. @author (your name) */
public class Submission {
    private Assignment assignment;
    private String content = "";
    private boolean submitted;
    private LocalDateTime submittedOn;
    private Double score;

    public Submission(Assignment assignment) { this.assignment = Validator.requireNonNull(assignment, "Assignment"); }

    /** Student submits (or re-submits, until it is graded). */
    public void submit(String content) {
        if (isGraded()) throw new IllegalStateException("Graded work cannot be re-submitted.");
        this.content = Validator.requireText(content, "Submission");
        this.submitted = true;
        this.submittedOn = LocalDateTime.now();
    }

    /** Faculty grades a submitted assignment. */
    public void grade(double score) {
        if (!submitted) throw new IllegalStateException("The student has not submitted this assignment.");
        if (score < 0 || score > assignment.getMaxPoints()) {
            throw new IllegalArgumentException("Score must be between 0 and " + assignment.getMaxPoints() + ".");
        }
        this.score = score;
    }

    public boolean isGraded() { return score != null; }
    public Assignment getAssignment() { return assignment; }
    public String getContent() { return content; }
    public boolean isSubmitted() { return submitted; }
    public LocalDateTime getSubmittedOn() { return submittedOn; }
    public Double getScore() { return score; }
}
