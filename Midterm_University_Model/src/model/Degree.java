package model;

import java.util.ArrayList;

/** A degree program and the courses it requires. @author (your name) */
public class Degree {
    private String name;
    private ArrayList<Course> courses = new ArrayList<>();

    public Degree(String name) { this.name = Validator.requireText(name, "Degree name"); }

    public void addCourse(Course c) { if (c != null && !courses.contains(c)) courses.add(c); }
    public void removeCourse(Course c) { courses.remove(c); }

    public String getName() { return name; }
    public ArrayList<Course> getCourses() { return courses; }
    @Override public String toString() { return name; }
}
