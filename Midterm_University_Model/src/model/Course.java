package model;

/** A course in the catalog. Tuition is billed per credit hour. @author (your name) */
public class Course {
    public static final double TUITION_PER_CREDIT = 1500.0;
    private String courseId;
    private String name;
    private int credits;

    public Course(String courseId, String name, int credits) {
        this.courseId = Validator.requireText(courseId, "Course ID");
        this.name = Validator.requireText(name, "Course name");
        this.credits = Validator.requirePositive(credits, "Credits");
    }

    public double getTuition() { return credits * TUITION_PER_CREDIT; }

    public String getCourseId() { return courseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Validator.requireText(name, "Course name"); }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = Validator.requirePositive(credits, "Credits"); }
    @Override public String toString() { return courseId + " - " + name; }
}
