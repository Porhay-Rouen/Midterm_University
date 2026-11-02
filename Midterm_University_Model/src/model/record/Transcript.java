package model.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.course.Semester;
import model.person.StudentRole;

/**
 * A student's academic history. It only collects CourseLoads; the GPA math
 * for one term lives in CourseLoad. The overall GPA is computed across all terms.
 *
 * @author Porhay Rouen
 */
public class Transcript {

    private final StudentRole student;
    private final List<CourseLoad> courseLoads = new ArrayList<>();

    public Transcript(StudentRole student) {
        this.student = student;
    }

    public StudentRole getStudent() {
        return student;
    }

    /** Course loads sorted oldest to newest. */
    public List<CourseLoad> getCourseLoads() {
        List<CourseLoad> sorted = new ArrayList<>(courseLoads);
        Collections.sort(sorted, (a, b) -> a.getSemester().compareTo(b.getSemester()));
        return sorted;
    }

    /** Semesters that have a non-empty course load (fills the semester dropdown). */
    public List<Semester> getSemesters() {
        List<Semester> result = new ArrayList<>();
        for (CourseLoad load : getCourseLoads()) {
            if (!load.getSeatAssignments().isEmpty()) {
                result.add(load.getSemester());
            }
        }
        return result;
    }

    /** Course load for a semester, or null. */
    public CourseLoad findCourseLoad(Semester semester) {
        for (CourseLoad load : courseLoads) {
            if (load.getSemester().equals(semester)) {
                return load;
            }
        }
        return null;
    }

    public CourseLoad getOrCreateCourseLoad(Semester semester) {
        CourseLoad load = findCourseLoad(semester);
        if (load == null) {
            load = new CourseLoad(student, semester);
            courseLoads.add(load);
        }
        return load;
    }

    /** Overall GPA = all quality points / all graded credit hours (0 when nothing is graded). */
    public double getOverallGPA() {
        double points = 0;
        int credits = 0;
        for (CourseLoad load : courseLoads) {
            points += load.getQualityPoints();
            credits += load.getGradedCredits();
        }
        return credits == 0 ? 0 : points / credits;
    }

    public boolean hasGradedCourses() {
        for (CourseLoad load : courseLoads) {
            if (load.hasGradedCourses()) {
                return true;
            }
        }
        return false;
    }

    /** Standing for one term, using that term's GPA and the overall GPA. */
    public AcademicStanding getStanding(Semester semester) {
        CourseLoad load = findCourseLoad(semester);
        if (load == null || !load.hasGradedCourses()) {
            return AcademicStanding.IN_PROGRESS;
        }
        return AcademicStanding.determine(load.getSemesterGPA(), getOverallGPA());
    }

    /** Current standing based on overall GPA only (used by admin records). */
    public AcademicStanding getCurrentStanding() {
        if (!hasGradedCourses()) {
            return AcademicStanding.IN_PROGRESS;
        }
        double overall = getOverallGPA();
        return AcademicStanding.determine(overall, overall);
    }
}
