package model;

import java.util.ArrayList;

/** All course offerings of one semester for a department. @author (your name) */
public class CourseSchedule {
    private Semester semester;
    private ArrayList<CourseOffering> offerings = new ArrayList<>();

    public CourseSchedule(Semester semester) { this.semester = Validator.requireNonNull(semester, "Semester"); }

    public CourseOffering newCourseOffering(Course course, FacultyRole faculty, int capacity) {
        CourseOffering o = new CourseOffering(semester, course, faculty, capacity);
        offerings.add(o);
        return o;
    }

    public CourseOffering findOffering(String courseId) {
        if (courseId == null) return null;
        for (CourseOffering o : offerings) if (o.getCourse().getCourseId().equalsIgnoreCase(courseId.trim())) return o;
        return null;
    }

    public void removeOffering(CourseOffering o) {
        if (o.getEnrolledCount() > 0) throw new IllegalStateException("Cannot delete an offering that has enrolled students.");
        offerings.remove(o);
    }

    // ----- the three student search methods (an empty keyword returns everything) -----
    public ArrayList<CourseOffering> searchByCourseId(String keyword) {
        String k = norm(keyword);
        ArrayList<CourseOffering> r = new ArrayList<>();
        for (CourseOffering o : offerings) if (o.getCourse().getCourseId().toLowerCase().contains(k)) r.add(o);
        return r;
    }

    public ArrayList<CourseOffering> searchByTeacher(String keyword) {
        String k = norm(keyword);
        ArrayList<CourseOffering> r = new ArrayList<>();
        for (CourseOffering o : offerings) if (o.getFaculty().getPerson().getName().toLowerCase().contains(k)) r.add(o);
        return r;
    }

    public ArrayList<CourseOffering> searchByTitle(String keyword) {
        String k = norm(keyword);
        ArrayList<CourseOffering> r = new ArrayList<>();
        for (CourseOffering o : offerings) if (o.getTitle().toLowerCase().contains(k)) r.add(o);
        return r;
    }

    public ArrayList<CourseOffering> getOfferingsByFaculty(FacultyRole f) {
        ArrayList<CourseOffering> r = new ArrayList<>();
        for (CourseOffering o : offerings) if (o.getFaculty() == f) r.add(o);
        return r;
    }

    private String norm(String s) { return s == null ? "" : s.trim().toLowerCase(); }

    public Semester getSemester() { return semester; }
    public ArrayList<CourseOffering> getOfferings() { return offerings; }
}
