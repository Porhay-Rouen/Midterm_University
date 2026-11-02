package model.university;

import model.university.Department;
import model.util.Validation;

/**
 * A course in a department's catalog (for example INFO5100, 4 credits).
 * Create courses with CourseCatalog.newCourse(...).
 *
 * @author Porhay Rouen
 */
public class Course {

    /** Tuition billed per credit hour. */
    public static final double TUITION_PER_CREDIT = 1000.0;

    private final Department department;
    private final String courseId;
    private String name;
    private int credits;
    private String description;

    Course(Department department, String courseId, String name, int credits, String description) {
        this.department = Validation.requireNonNull(department, "Department");
        this.courseId = normalizeId(courseId);
        this.name = Validation.requireText(name, "Course name");
        this.credits = Validation.requirePositive(credits, "Credits");
        this.description = Validation.optionalText(description);
    }

    /** "info 5100" and "INFO5100" both become "INFO5100". */
    public static String normalizeId(String id) {
        return Validation.requireText(id, "Course ID").replaceAll("\\s+", "").toUpperCase();
    }

    public Department getDepartment() {
        return department;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public int getCredits() {
        return credits;
    }

    public String getDescription() {
        return description;
    }

    public double getTuition() {
        return credits * TUITION_PER_CREDIT;
    }

    public void setName(String name) {
        this.name = Validation.requireText(name, "Course name");
    }

    public void setCredits(int credits) {
        this.credits = Validation.requirePositive(credits, "Credits");
    }

    public void setDescription(String description) {
        this.description = Validation.optionalText(description);
    }

    @Override
    public String toString() {
        return courseId + " - " + name;
    }
}
