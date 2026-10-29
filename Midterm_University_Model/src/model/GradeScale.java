package model;

/** Single source of truth for letter grades, grade points and percent-to-letter conversion. @author (your name) */
public final class GradeScale {
    public static final String IN_PROGRESS = "IP";
    public static final String[] LETTERS = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "F"};
    private static final double[] POINTS = {4.0, 3.7, 3.3, 3.0, 2.7, 2.3, 2.0, 1.7, 0.0};
    private static final double[] MIN_PERCENT = {93, 90, 87, 83, 80, 77, 73, 70, 0};

    private GradeScale() { }

    public static boolean isLetter(String grade) { return indexOf(grade) >= 0; }

    /** Grade points for a letter, or -1 if the grade is not a letter (for example IP). */
    public static double points(String grade) {
        int i = indexOf(grade);
        return i < 0 ? -1 : POINTS[i];
    }

    /** Converts a total percentage (0-100) to a final letter grade. */
    public static String letterFor(double percent) {
        for (int i = 0; i < LETTERS.length; i++) if (percent >= MIN_PERCENT[i]) return LETTERS[i];
        return "F";
    }

    public static double round(double value) { return Math.round(value * 100.0) / 100.0; }

    private static int indexOf(String grade) {
        if (grade == null) return -1;
        for (int i = 0; i < LETTERS.length; i++) if (LETTERS[i].equals(grade)) return i;
        return -1;
    }
}
