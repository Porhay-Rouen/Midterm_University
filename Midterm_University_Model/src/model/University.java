package model;

import java.util.ArrayList;

/** The university: top of the model. @author (your name) */
public class University {
    private String name;
    private ArrayList<College> colleges = new ArrayList<>();

    public University(String name) { this.name = Validator.requireText(name, "University name"); }

    public College newCollege(String collegeName) {
        College c = new College(collegeName);
        colleges.add(c);
        return c;
    }

    public String getName() { return name; }
    public ArrayList<College> getColleges() { return colleges; }
    @Override public String toString() { return name; }
}
