package model.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.person.FacultyRole;
import model.university.Course;
import model.university.Department;
import model.util.Validation;

/**
 * All course offerings of one semester in one department (Task 4 item 3:
 * find a course by looking it up in that semester's schedule).
 *
 * @author Porhay Rouen
 */
public class CourseSchedule {

    private final Department department;
    private final Semester semester;
    private final List<CourseOffering> offerings = new ArrayList<>();

    public CourseSchedule(Department department, Semester semester) {
        this.department = department;
        this.semester = semester;
    }

    public Department getDepartment() {
        return department;
    }

    public Semester getSemester() {
        return semester;
    }

    /** Creates an offering. Faculty may be null (assign later). Blocks the same course twice. */
    public CourseOffering newOffering(Course course, FacultyRole faculty, int capacity,
            String room, String meetingTime) {
        Validation.requireNonNull(course, "Course");
        if (findByCourseNumber(course.getCourseId()) != null) {
            throw new IllegalStateException(course.getCourseId() + " is already offered in " + semester + ".");
        }
        CourseOffering offering = new CourseOffering(this, course, faculty, capacity, room, meetingTime);
        offerings.add(offering);
        return offering;
    }

    /** Removes an offering; blocked while students are enrolled. */
    public void removeOffering(CourseOffering offering) {
        Validation.requireNonNull(offering, "Offering");
        if (offering.getEnrolledCount() > 0) {
            throw new IllegalStateException("Cannot delete an offering that has enrolled students.");
        }
        offering.assignFaculty(null);
        offerings.remove(offering);
    }

    public List<CourseOffering> getOfferings() {
        return Collections.unmodifiableList(offerings);
    }

    /** Lookup by course number such as "INFO6215"; null when not offered. */
    public CourseOffering findByCourseNumber(String courseNumber) {
        if (courseNumber == null || courseNumber.trim().isEmpty()) {
            return null;
        }
        String id = Course.normalizeId(courseNumber);
        for (CourseOffering o : offerings) {
            if (o.getCourse().getCourseId().equals(id)) {
                return o;
            }
        }
        return null;
    }

    // ---- the 3 student search methods (blank keyword returns everything) ----

    public List<CourseOffering> searchByCourseId(String keyword) {
        String key = Validation.optionalText(keyword).replaceAll("\\s+", "").toLowerCase();
        List<CourseOffering> result = new ArrayList<>();
        for (CourseOffering o : offerings) {
            if (o.getCourse().getCourseId().toLowerCase().contains(key)) {
                result.add(o);
            }
        }
        return result;
    }

    public List<CourseOffering> searchByTeacher(String keyword) {
        String key = Validation.optionalText(keyword).toLowerCase();
        List<CourseOffering> result = new ArrayList<>();
        for (CourseOffering o : offerings) {
            if (o.getFacultyName().toLowerCase().contains(key)) {
                result.add(o);
            }
        }
        return result;
    }

    public List<CourseOffering> searchByCourseName(String keyword) {
        String key = Validation.optionalText(keyword).toLowerCase();
        List<CourseOffering> result = new ArrayList<>();
        for (CourseOffering o : offerings) {
            if (o.getTitle().toLowerCase().contains(key)) {
                result.add(o);
            }
        }
        return result;
    }

    public List<CourseOffering> getOfferingsFor(FacultyRole faculty) {
        List<CourseOffering> result = new ArrayList<>();
        for (CourseOffering o : offerings) {
            if (o.getFaculty() == faculty) {
                result.add(o);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return semester + " schedule";
    }
}
