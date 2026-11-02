package model.person;

import model.university.Department;
import model.util.Validation;

/**
 * Base class for every role a Person can hold. The role chosen at login
 * decides which menu buttons the UI shows.
 *
 * @author Porhay Rouen
 */
public abstract class Role {

    /** The four roles, with the prefix used to auto-generate university IDs. */
    public enum Type {
        STUDENT("S", "Student"),
        FACULTY("F", "Faculty"),
        ADMIN("A", "Admin"),
        REGISTRAR("R", "Registrar");

        private final String idPrefix;
        private final String label;

        Type(String idPrefix, String label) {
            this.idPrefix = idPrefix;
            this.label = label;
        }

        public String getIdPrefix() {
            return idPrefix;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final Person person;

    protected Role(Person person) {
        this.person = Validation.requireNonNull(person, "Person");
        person.addRole(this);
    }

    public Person getPerson() {
        return person;
    }

    public abstract Type getType();

    public String getRoleName() {
        return getType().toString();
    }

    /** Students and faculty belong to a department; admin and registrar are university-level (null). */
    public Department getDepartment() {
        return null;
    }
}
