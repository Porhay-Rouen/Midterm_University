package model;

import java.util.ArrayList;

/** All student roles of a department. @author (your name) */
public class StudentDirectory {
    private ArrayList<StudentRole> list = new ArrayList<>();

    public StudentRole newStudentRole(Person person) {
        StudentRole r = new StudentRole(person);
        list.add(r);
        return r;
    }

    public StudentRole findById(String personId) {
        if (personId == null) return null;
        for (StudentRole r : list) if (r.getPerson().getPersonId().equalsIgnoreCase(personId.trim())) return r;
        return null;
    }

    public void removeStudent(StudentRole r) { list.remove(r); }
    public ArrayList<StudentRole> getStudents() { return list; }
}
