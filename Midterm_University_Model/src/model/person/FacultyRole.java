package model.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.course.CourseOffering;
import model.course.Semester;
import model.university.Department;
import model.util.Validation;

/**
 * A faculty member. Knows the department and the offerings assigned to them.
 *
 * @author Porhay Rouen
 */
public class FacultyRole extends Role {

    private final Department department;
    private final List<CourseOffering> offerings = new ArrayList<>();

    public FacultyRole(Person person, Department department) {
        super(person);
        this.department = Validation.requireNonNull(department, "Department");
    }

    @Override
    public Type getType() {
        return Type.FACULTY;
    }

    @Override
    public Department getDepartment() {
        return department;
    }

    /** All offerings assigned to this faculty member (all semesters). */
    public List<CourseOffering> getOfferings() {
        return Collections.unmodifiableList(offerings);
    }

    /** Offerings in one semester (the "My Courses" page). */
    public List<CourseOffering> getOfferings(Semester semester) {
        List<CourseOffering> result = new ArrayList<>();
        for (CourseOffering o : offerings) {
            if (o.getSemester().equals(semester)) {
                result.add(o);
            }
        }
        return result;
    }

    /** Total tuition collected from paid students in all of this faculty member's offerings. */
    public double getTotalTuitionCollected() {
        double total = 0;
        for (CourseOffering o : offerings) {
            total += o.getTuitionCollected();
        }
        return total;
    }

    public void attachOffering(CourseOffering offering) {
        if (!offerings.contains(offering)) {
            offerings.add(offering);
        }
    }

    public void detachOffering(CourseOffering offering) {
        offerings.remove(offering);
    }
}
