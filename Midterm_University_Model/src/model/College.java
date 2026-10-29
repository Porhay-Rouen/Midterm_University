package model;

import java.util.ArrayList;

/** A college of the university. @author (your name) */
public class College {
    private String name;
    private ArrayList<Department> departments = new ArrayList<>();

    public College(String name) { this.name = Validator.requireText(name, "College name"); }

    public Department newDepartment(String departmentName) {
        Department d = new Department(departmentName);
        departments.add(d);
        return d;
    }

    public String getName() { return name; }
    public ArrayList<Department> getDepartments() { return departments; }
    @Override public String toString() { return name; }
}
