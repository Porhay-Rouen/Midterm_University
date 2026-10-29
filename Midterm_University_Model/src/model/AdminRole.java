package model;

/** University administrator. @author (your name) */
public class AdminRole extends Role {
    public AdminRole(Person person) { super(person); }
    @Override public String getRoleName() { return "Admin"; }
}
