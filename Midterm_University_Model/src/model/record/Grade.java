package model.record;

import model.util.Validation;

/**
 * Letter grades and their grade points. This enum is the single source of
 * truth for the grade map (A = 4.0 ... F = 0.0). IP means "in progress".
 *
 * @author Porhay Rouen
 */
public enum Grade {
    A("A", 4.0),
    A_MINUS("A-", 3.7),
    B_PLUS("B+", 3.3),
    B("B", 3.0),
    B_MINUS("B-", 2.7),
    C_PLUS("C+", 2.3),
    C("C", 2.0),
    C_MINUS("C-", 1.7),
    F("F", 0.0),
    IP("IP", 0.0);

    private final String label;
    private final double points;

    Grade(String label, double points) {
        this.label = label;
        this.points = points;
    }

    public String getLabel() {
        return label;
    }

    public double getPoints() {
        return points;
    }

    /** True once the course is finished (everything except IP). Only final grades count toward GPA. */
    public boolean isFinal() {
        return this != IP;
    }

    /** True when the grade earns credit toward graduation (final and not F). */
    public boolean isPassing() {
        return isFinal() && this != F;
    }

    /** Converts a percentage (0-100) to a letter grade. */
    public static Grade fromPercentage(double percent) {
        if (percent >= 93) return A;
        if (percent >= 90) return A_MINUS;
        if (percent >= 87) return B_PLUS;
        if (percent >= 83) return B;
        if (percent >= 80) return B_MINUS;
        if (percent >= 77) return C_PLUS;
        if (percent >= 73) return C;
        if (percent >= 70) return C_MINUS;
        return F;
    }

    /** Parses "A-", "b+", "IP" ... Throws IllegalArgumentException for unknown text. */
    public static Grade fromLabel(String text) {
        String clean = Validation.requireText(text, "Grade");
        for (Grade g : values()) {
            if (g.label.equalsIgnoreCase(clean)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown grade: " + text);
    }

    /** Shows the label ("A-") so a JComboBox displays it nicely. */
    @Override
    public String toString() {
        return label;
    }
}
