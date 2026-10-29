package model;

/** A piece of graded work defined by faculty for one course offering. @author (your name) */
public class Assignment {
    private String title;
    private double maxPoints;

    public Assignment(String title, double maxPoints) {
        this.title = Validator.requireText(title, "Assignment title");
        if (maxPoints <= 0) throw new IllegalArgumentException("Maximum points must be greater than 0.");
        this.maxPoints = maxPoints;
    }

    public String getTitle() { return title; }
    public double getMaxPoints() { return maxPoints; }
    @Override public String toString() { return title; }
}
