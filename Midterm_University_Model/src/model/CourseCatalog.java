package model;

import java.util.ArrayList;

/** All courses a department offers. @author (your name) */
public class CourseCatalog {
    private ArrayList<Course> courses = new ArrayList<>();

    public Course newCourse(String courseId, String name, int credits) {
        Course c = new Course(courseId, name, credits);
        if (findCourse(c.getCourseId()) != null) throw new IllegalArgumentException("Course ID already exists.");
        courses.add(c);
        return c;
    }

    public Course findCourse(String courseId) {
        if (courseId == null) return null;
        for (Course c : courses) if (c.getCourseId().equalsIgnoreCase(courseId.trim())) return c;
        return null;
    }

    public void removeCourse(Course c) { courses.remove(c); }

    public ArrayList<Course> search(String keyword) {
        ArrayList<Course> result = new ArrayList<>();
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        for (Course c : courses) {
            if (c.getCourseId().toLowerCase().contains(k) || c.getName().toLowerCase().contains(k)) result.add(c);
        }
        return result;
    }

    public ArrayList<Course> getCourses() { return courses; }
}
