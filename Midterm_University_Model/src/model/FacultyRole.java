package model;

/** Faculty member. @author (your name) */
public class FacultyRole extends Role {
    public FacultyRole(Person person) { super(person); }
    @Override public String getRoleName() { return "Faculty"; }
}
