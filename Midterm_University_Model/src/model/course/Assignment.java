package model.course;

import model.util.Validation;

/**
 * A gradable assignment inside a course offering (homework, exam ...).
 * Create with CourseOffering.addAssignment(title, maxScore).
 *
 * @author Porhay Rouen
 */
public class Assignment {

    private final String title;
    private final double maxScore;

    Assignment(String title, double maxScore) {
        this.title = Validation.requireText(title, "Assignment title");
        if (maxScore <= 0) {
            throw new IllegalArgumentException("Maximum score must be greater than 0.");
        }
        this.maxScore = maxScore;
    }

    public String getTitle() {
        return title;
    }

    public double getMaxScore() {
        return maxScore;
    }

    @Override
    public String toString() {
        return title + " (" + maxScore + ")";
    }
}
