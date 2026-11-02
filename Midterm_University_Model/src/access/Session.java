package access;

import model.person.Person;
import model.person.Role;

/**
 * The logged-in user: who they are and which role they picked at login.
 * MainFrame receives one Session and uses it to decide which menu buttons to show.
 * Panels call requireRole(...) before doing role-only work so blocked pages stay blocked.
 *
 * @author Porhay Rouen
 */
public class Session {

    private final Person person;
    private final Role role;
    private boolean active = true;

    Session(Person person, Role role) {
        this.person = person;
        this.role = role;
    }

    public Person getPerson() {
        return person;
    }

    /** The role object chosen at login, e.g. cast to AdminRole when the type is ADMIN. */
    public Role getRole() {
        return role;
    }

    public Role.Type getRoleType() {
        return role.getType();
    }

    public boolean isActive() {
        return active;
    }

    /** True while the session is open and the user logged in with this role. */
    public boolean can(Role.Type type) {
        return active && role.getType() == type;
    }

    /** Throws SecurityException when the session is closed or the role does not match. */
    public void requireRole(Role.Type type) {
        if (!can(type)) {
            throw new SecurityException("Access denied: this page is for the " + type + " role.");
        }
    }

    /** Ends the session. MainFrame calls this on Logout and then shows the Login screen again. */
    public void logout() {
        active = false;
    }
}
