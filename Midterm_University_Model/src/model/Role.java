package model;

/** Base class of StudentRole, FacultyRole, AdminRole and RegistrarRole. @author (your name) */
public abstract class Role {
    protected Person person;

    public Role(Person person) {
        this.person = Validator.requireNonNull(person, "Person");
        person.addRole(this);
    }

    public Person getPerson() { return person; }
    public abstract String getRoleName();
    @Override public String toString() { return person.getName() + " (" + getRoleName() + ")"; }
}
