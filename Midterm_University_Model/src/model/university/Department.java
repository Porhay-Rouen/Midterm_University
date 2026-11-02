package model.university;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.university.FacultyDirectory;
import model.university.StudentDirectory;
import model.course.CourseOffering;
import model.course.CourseSchedule;
import model.course.Semester;
import model.util.Validation;

/**
 * A department owns its CourseCatalog, Degrees, CourseSchedules (one per
 * semester), FacultyDirectory and StudentDirectory.
 *
 * @author Porhay Rouen
 */
public class Department {

    private final String name;
    private final CourseCatalog courseCatalog;
    private final FacultyDirectory facultyDirectory;
    private final StudentDirectory studentDirectory;
    private final List<Degree> degrees = new ArrayList<>();
    private final List<CourseSchedule> schedules = new ArrayList<>();

    Department(String name) {
        this.name = Validation.requireText(name, "Department name");
        this.courseCatalog = new CourseCatalog(this);
        this.facultyDirectory = new FacultyDirectory(this);
        this.studentDirectory = new StudentDirectory(this);
    }

    public String getName() {
        return name;
    }

    public CourseCatalog getCourseCatalog() {
        return courseCatalog;
    }

    public FacultyDirectory getFacultyDirectory() {
        return facultyDirectory;
    }

    public StudentDirectory getStudentDirectory() {
        return studentDirectory;
    }

    // ---- degrees ----

    public Degree newDegree(String degreeName, int requiredCredits) {
        if (findDegree(degreeName) != null) {
            throw new IllegalArgumentException("Degree " + degreeName + " already exists.");
        }
        Degree degree = new Degree(degreeName, requiredCredits);
        degrees.add(degree);
        return degree;
    }

    public Degree findDegree(String degreeName) {
        for (Degree d : degrees) {
            if (d.getName().equalsIgnoreCase(Validation.optionalText(degreeName))) {
                return d;
            }
        }
        return null;
    }

    public List<Degree> getDegrees() {
        return Collections.unmodifiableList(degrees);
    }

    // ---- schedules ----

    /** Creates the schedule for a semester; blocks a second schedule for the same semester. */
    public CourseSchedule newCourseSchedule(Semester semester) {
        Validation.requireNonNull(semester, "Semester");
        if (findSchedule(semester) != null) {
            throw new IllegalStateException(name + " already has a schedule for " + semester + ".");
        }
        CourseSchedule schedule = new CourseSchedule(this, semester);
        schedules.add(schedule);
        return schedule;
    }

    /** Schedule for the semester, or null. */
    public CourseSchedule findSchedule(Semester semester) {
        for (CourseSchedule s : schedules) {
            if (s.getSemester().equals(semester)) {
                return s;
            }
        }
        return null;
    }

    public List<CourseSchedule> getSchedules() {
        return Collections.unmodifiableList(schedules);
    }

    /** True when any semester currently offers the course. */
    public boolean isCourseOffered(Course course) {
        for (CourseSchedule s : schedules) {
            for (CourseOffering o : s.getOfferings()) {
                if (o.getCourse() == course) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
