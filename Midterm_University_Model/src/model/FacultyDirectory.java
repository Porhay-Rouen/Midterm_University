package model;

import java.util.ArrayList;

/** All faculty roles of a department. @author (your name) */
public class FacultyDirectory {
    private ArrayList<FacultyRole> list = new ArrayList<>();

    public FacultyRole newFacultyRole(Person person) {
        FacultyRole r = new FacultyRole(person);
        list.add(r);
        return r;
    }

    public FacultyRole findById(String personId) {
        if (personId == null) return null;
        for (FacultyRole r : list) if (r.getPerson().getPersonId().equalsIgnoreCase(personId.trim())) return r;
        return null;
    }

    public void removeFaculty(FacultyRole r) { list.remove(r); }
    public ArrayList<FacultyRole> getFaculty() { return list; }
}
