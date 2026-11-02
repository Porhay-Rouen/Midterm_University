package model.university;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import  model.university.Department;
import model.util.Validation;

/**
 * Every course a department offers (Task 4 item 2: list the program's courses
 * by asking the department's catalog).
 *
 * @author Porhay Rouen
 */
public class CourseCatalog {

    private final Department department;
    private final List<Course> courses = new ArrayList<>();

    CourseCatalog(Department department) {
        this.department = department;
    }

    public Department getDepartment() {
        return department;
    }

    /** Adds a course; blocks duplicate course IDs. */
    public Course newCourse(String courseId, String name, int credits, String description) {
        if (findCourse(courseId) != null) {
            throw new IllegalArgumentException("Course " + courseId + " already exists in the catalog.");
        }
        Course course = new Course(department, courseId, name, credits, description);
        courses.add(course);
        return course;
    }

    /** Finds by course ID ("INFO 5100" or "info5100"); null when missing. */
    public Course findCourse(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            return null;
        }
        String id = Course.normalizeId(courseId);
        for (Course c : courses) {
            if (c.getCourseId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public List<Course> searchByName(String keyword) {
        List<Course> result = new ArrayList<>();
        String key = Validation.optionalText(keyword).toLowerCase();
        for (Course c : courses) {
            if (c.getName().toLowerCase().contains(key)) {
                result.add(c);
            }
        }
        return result;
    }

    /** Removes a course; blocked while any semester still offers it. */
    public void removeCourse(Course course) {
        Validation.requireNonNull(course, "Course");
        if (department.isCourseOffered(course)) {
            throw new IllegalStateException("Cannot delete " + course.getCourseId() + ": it has course offerings.");
        }
        courses.remove(course);
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }
}
