package model.university;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.person.Person;
import model.person.StudentRole;
import model.util.Validation;

/**
 * Student profiles of one department.
 *
 * @author Porhay Rouen
 */
public class StudentDirectory {

    private final Department department;
    private final List<StudentRole> students = new ArrayList<>();

    StudentDirectory(Department department) {
        this.department = department;
    }

    /** Gives an existing Person a StudentRole in this department. */
    public StudentRole newStudentRole(Person person, Degree degree) {
        StudentRole role = new StudentRole(person, department, degree);
        students.add(role);
        return role;
    }

    public List<StudentRole> getStudents() {
        return Collections.unmodifiableList(students);
    }

    public StudentRole findById(String universityId) {
        for (StudentRole s : students) {
            if (s.getPerson().getUniversityId().equalsIgnoreCase(Validation.optionalText(universityId))) {
                return s;
            }
        }
        return null;
    }

    public List<StudentRole> searchByName(String keyword) {
        String key = Validation.optionalText(keyword).toLowerCase();
        List<StudentRole> result = new ArrayList<>();
        for (StudentRole s : students) {
            if (s.getPerson().getFullName().toLowerCase().contains(key)) {
                result.add(s);
            }
        }
        return result;
    }

    /** Removes the student, frees their seats and detaches the role. */
    public void removeStudent(StudentRole student) {
        Validation.requireNonNull(student, "Student");
        student.releaseAllSeats();
        student.getPerson().removeRole(student);
        students.remove(student);
    }
}
