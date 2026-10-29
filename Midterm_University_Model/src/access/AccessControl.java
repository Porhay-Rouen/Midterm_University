package access;

import java.util.ArrayList;
import model.Person;
import model.Role;

/**
 * Authentication (login/logout) and authorization (which role may open which page).
 * Create one instance at startup, pass it to MainFrame, and call requireAuthorized(...) in every panel.
 * @author (your name)
 */
public class AccessControl {
    private ArrayList<UserAccount> accounts = new ArrayList<>();
    private UserAccount current;

    // ----- authentication -----
    /** Returns the account if the username and password are correct and the account is active, otherwise null. */
    public UserAccount login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        UserAccount a = findByUsername(username);
        if (a == null || !a.isActive() || !a.checkPassword(password)) return null;
        current = a;
        return a;
    }

    public void logout() { current = null; }
    public boolean isLoggedIn() { return current != null; }
    public UserAccount getCurrentAccount() { return current; }
    public Role getCurrentRole() { return current == null ? null : current.getRole(); }

    // ----- authorization -----
    /** True if the logged-in user has the given role type, e.g. isAuthorized(AdminRole.class). */
    public boolean isAuthorized(Class<? extends Role> roleType) {
        return current != null && roleType.isInstance(current.getRole());
    }

    /** Throws SecurityException if the logged-in user does not have the role. Call at the top of every protected action. */
    public void requireAuthorized(Class<? extends Role> roleType) {
        if (!isAuthorized(roleType)) throw new SecurityException("Access denied: you are not authorized to use this feature.");
    }

    // ----- account management (Admin use case) -----
    public UserAccount createAccount(String username, String password, Role role) {
        String u = username == null ? "" : username.trim();
        if (u.isEmpty() || u.contains(" ")) throw new IllegalArgumentException("Username is required and cannot contain spaces.");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (role == null) throw new IllegalArgumentException("Role is required.");
        if (findByUsername(u) != null) throw new IllegalArgumentException("Username already exists.");
        UserAccount a = new UserAccount(u, password, role);
        accounts.add(a);
        return a;
    }

    public void changePassword(String username, String newPassword) {
        UserAccount a = findByUsername(username);
        if (a == null) throw new IllegalArgumentException("Account not found.");
        if (newPassword == null || newPassword.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters.");
        a.setPassword(newPassword);
    }

    public void deleteAccount(String username) {
        UserAccount a = findByUsername(username);
        if (a == null) throw new IllegalArgumentException("Account not found.");
        if (a == current) throw new IllegalStateException("You cannot delete the account you are logged in with.");
        accounts.remove(a);
    }

    /** Removes all accounts of a person (call after Department.deletePerson). */
    public void removeAccountsFor(Person p) {
        for (UserAccount a : new ArrayList<>(accounts)) if (a.getRole().getPerson() == p) accounts.remove(a);
    }

    public UserAccount findByUsername(String username) {
        if (username == null) return null;
        for (UserAccount a : accounts) if (a.getUsername().equalsIgnoreCase(username.trim())) return a;
        return null;
    }

    public ArrayList<UserAccount> getAccounts() { return accounts; }
}
