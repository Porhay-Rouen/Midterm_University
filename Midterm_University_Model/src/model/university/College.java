package model.university;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.university.Department;
import model.util.Validation;

/**
 * A college groups departments (for example the College of Engineering).
 *
 * @author Porhay Rouen
 */
public class College {

    private final String name;
    private final List<Department> departments = new ArrayList<>();

    College(String name) {
        this.name = Validation.requireText(name, "College name");
    }

    public String getName() {
        return name;
    }

    public Department newDepartment(String departmentName) {
        for (Department d : departments) {
            if (d.getName().equalsIgnoreCase(Validation.optionalText(departmentName))) {
                throw new IllegalArgumentException("Department " + departmentName + " already exists.");
            }
        }
        Department department = new Department(departmentName);
        departments.add(department);
        return department;
    }

    public List<Department> getDepartments() {
        return Collections.unmodifiableList(departments);
    }

    @Override
    public String toString() {
        return name;
    }
}
