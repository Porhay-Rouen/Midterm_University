package model.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.util.Validation;

/**
 * A person in the university. A person owns one or more Roles
 * (StudentRole, FacultyRole, AdminRole, RegistrarRole).
 * Create people through University.registerStudent/registerFaculty/... so
 * the ID is generated and duplicates are blocked.
 *
 * @author Porhay Rouen
 */
public class Person {

    private final String universityId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean active = true;
    private final List<Role> roles = new ArrayList<>();

    public Person(String universityId, String firstName, String lastName, String email, String phone) {
        this.universityId = Validation.requireText(universityId, "University ID");
        this.firstName = Validation.requireText(firstName, "First name");
        this.lastName = Validation.requireText(lastName, "Last name");
        this.email = Validation.requireEmail(email);
        this.phone = Validation.optionalPhone(phone);
    }

    public String getUniversityId() {
        return universityId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setFirstName(String firstName) {
        this.firstName = Validation.requireText(firstName, "First name");
    }

    public void setLastName(String lastName) {
        this.lastName = Validation.requireText(lastName, "Last name");
    }

    public void setPhone(String phone) {
        this.phone = Validation.optionalPhone(phone);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /** Package-private on purpose: use University.changeEmail so duplicate emails are blocked. */
    public void setEmail(String email) {
        this.email = Validation.requireEmail(email);
    }

    /** Called by the Role constructor; a person can hold each role type only once. */
    public void addRole(Role role) {
        Validation.requireNonNull(role, "Role");
        if (hasRole(role.getType())) {
            throw new IllegalStateException(getFullName() + " already has the role " + role.getType());
        }
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public List<Role> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    public boolean hasRole(Role.Type type) {
        for (Role r : roles) {
            if (r.getType() == type) {
                return true;
            }
        }
        return false;
    }

    /** Returns the role of the given class, or null. Example: person.getRole(StudentRole.class). */
    public <T extends Role> T getRole(Class<T> roleClass) {
        for (Role r : roles) {
            if (roleClass.isInstance(r)) {
                return roleClass.cast(r);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + universityId + ")";
    }
}
