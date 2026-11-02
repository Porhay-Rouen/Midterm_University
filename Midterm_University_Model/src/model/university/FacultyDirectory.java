package model.university;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.person.FacultyRole;
import model.person.Person;
import model.util.Validation;

/**
 * Faculty profiles of one department.
 *
 * @author Porhay Rouen
 */
public class FacultyDirectory {

    private final Department department;
    private final List<FacultyRole> facultyList = new ArrayList<>();

    FacultyDirectory(Department department) {
        this.department = department;
    }

    /** Gives an existing Person a FacultyRole in this department. */
    public FacultyRole newFacultyRole(Person person) {
        FacultyRole role = new FacultyRole(person, department);
        facultyList.add(role);
        return role;
    }

    public List<FacultyRole> getFacultyList() {
        return Collections.unmodifiableList(facultyList);
    }

    public FacultyRole findById(String universityId) {
        for (FacultyRole f : facultyList) {
            if (f.getPerson().getUniversityId().equalsIgnoreCase(Validation.optionalText(universityId))) {
                return f;
            }
        }
        return null;
    }

    public List<FacultyRole> searchByName(String keyword) {
        String key = Validation.optionalText(keyword).toLowerCase();
        List<FacultyRole> result = new ArrayList<>();
        for (FacultyRole f : facultyList) {
            if (f.getPerson().getFullName().toLowerCase().contains(key)) {
                result.add(f);
            }
        }
        return result;
    }

    /** Removes a faculty member; blocked while they still teach offerings (reassign first). */
    public void removeFaculty(FacultyRole faculty) {
        Validation.requireNonNull(faculty, "Faculty");
        if (!faculty.getOfferings().isEmpty()) {
            throw new IllegalStateException(faculty.getPerson().getFullName()
                    + " still teaches courses. Reassign those offerings first.");
        }
        faculty.getPerson().removeRole(faculty);
        facultyList.remove(faculty);
    }
}
