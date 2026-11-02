package model.record;

/**
 * Academic standing derived from term GPA and overall GPA.
 * Good Standing: term >= 3.0 and overall >= 3.0.
 * Academic Warning: term < 3.0 while overall >= 3.0.
 * Academic Probation: overall < 3.0 (regardless of term GPA).
 * In Progress: the term has no final grades yet.
 *
 * @author Porhay Rouen
 */
public enum AcademicStanding {
    GOOD_STANDING("Good Standing"),
    ACADEMIC_WARNING("Academic Warning"),
    ACADEMIC_PROBATION("Academic Probation"),
    IN_PROGRESS("In Progress");

    private final String label;

    AcademicStanding(String label) {
        this.label = label;
    }

    public static AcademicStanding determine(double termGpa, double overallGpa) {
        if (overallGpa < 3.0) {
            return ACADEMIC_PROBATION;
        }
        if (termGpa < 3.0) {
            return ACADEMIC_WARNING;
        }
        return GOOD_STANDING;
    }

    @Override
    public String toString() {
        return label;
    }
}
