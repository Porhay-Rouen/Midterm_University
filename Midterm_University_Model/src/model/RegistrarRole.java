package model;

/** Registrar. Has office hours in addition to the person's contact info. @author (your name) */
public class RegistrarRole extends Role {
    private String officeHours = "";

    public RegistrarRole(Person person) { super(person); }

    public String getOfficeHours() { return officeHours; }
    public void setOfficeHours(String officeHours) { this.officeHours = officeHours == null ? "" : officeHours.trim(); }
    @Override public String getRoleName() { return "Registrar"; }
}
