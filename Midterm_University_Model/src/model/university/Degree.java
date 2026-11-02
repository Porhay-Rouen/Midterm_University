package model.university;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.util.Validation;

/**
 * A degree program (for example MSIS: 32 credits, INFO5100 is core, everything
 * else is elective).
 *
 * @author Porhay Rouen
 */
public class Degree {

    private final String name;
    private final int requiredCredits;
    private final List<Course> courses = new ArrayList<>();
    private final List<Course> coreCourses = new ArrayList<>();

    Degree(String name, int requiredCredits) {
        this.name = Validation.requireText(name, "Degree name");
        this.requiredCredits = Validation.requirePositive(requiredCredits, "Required credits");
    }

    public String getName() {
        return name;
    }

    public int getRequiredCredits() {
        return requiredCredits;
    }

    /** Adds a course to the program; pass core = true for required core courses. */
    public void addCourse(Course course, boolean core) {
        Validation.requireNonNull(course, "Course");
        if (!courses.contains(course)) {
            courses.add(course);
        }
        if (core && !coreCourses.contains(course)) {
            coreCourses.add(course);
        }
    }

    public boolean isCore(Course course) {
        return coreCourses.contains(course);
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public List<Course> getCoreCourses() {
        return Collections.unmodifiableList(coreCourses);
    }

    @Override
    public String toString() {
        return name;
    }
}
