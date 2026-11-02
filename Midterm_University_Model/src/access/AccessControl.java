package access;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import model.person.Person;
import model.person.Role;
import model.university.University;
import model.util.Validation;

/**
 * Authentication for the Digital University System: usernames, salted password
 * hashes, login with a chosen role, and account create / reset / delete for the
 * Admin "Accounts" page. The model itself holds no passwords.
 *
 * NOTE: this is a stand-in with the same job as the course's provided Access
 * Control package. If the team switches to that package, only this class
 * (and Session) needs to change; the UI keeps calling login() and logout().
 *
 * Rule violations throw IllegalArgumentException (bad input), IllegalStateException
 * (account state) and SecurityException (login refused). The UI shows getMessage().
 *
 * @author Porhay Rouen
 */
public class AccessControl {

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 6;

    private static final String LOGIN_FAILED = "Invalid username or password.";

    /** Stored for one account: never the plain password. */
    private static final class Credential {
        private final String universityId;
        private String salt;
        private String hash;

        Credential(String universityId, String salt, String hash) {
            this.universityId = universityId;
            this.salt = salt;
            this.hash = hash;
        }
    }

    private final University university;
    private final SecureRandom random = new SecureRandom();
    /** key = username in lower case, so "Admin" and "admin" are the same account. */
    private final Map<String, Credential> credentials = new HashMap<>();

    public AccessControl(University university) {
        this.university = Validation.requireNonNull(university, "University");
    }

    // ---- login / logout ----

    /**
     * Checks the username and password, then checks the person is active and really
     * holds the role picked in the Role combo box. Returns the Session for MainFrame.
     *
     * @throws IllegalArgumentException when username or password is empty or no role is chosen
     * @throws SecurityException        when the credentials, account status or role do not match
     */
    public Session login(String username, char[] password, Role.Type roleType) {
        String name = Validation.optionalText(username);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        Validation.requireNonNull(roleType, "Role");

        Credential credential = credentials.get(name.toLowerCase());
        // Same message for "no such user" and "wrong password" so usernames are not revealed.
        if (credential == null || !matches(credential, password)) {
            throw new SecurityException(LOGIN_FAILED);
        }
        Person person = university.findPersonById(credential.universityId);
        if (person == null) {
            throw new SecurityException(LOGIN_FAILED);
        }
        if (!person.isActive()) {
            throw new SecurityException("This account is deactivated. Please contact an administrator.");
        }
        for (Role role : person.getRoles()) {
            if (role.getType() == roleType) {
                return new Session(person, role);
            }
        }
        throw new SecurityException("This account does not have the " + roleType + " role.");
    }

    /** Ends the session; the caller then shows the Login screen. */
    public void logout(Session session) {
        Validation.requireNonNull(session, "Session").logout();
    }

    // ---- account management (Admin: Accounts / Register pages) ----

    /** Creates the login for a registered person. One account per person, usernames are unique. */
    public void createAccount(String username, char[] password, Person person) {
        String name = requireUsername(username);
        requirePassword(password);
        Validation.requireNonNull(person, "Person");
        if (university.findPersonById(person.getUniversityId()) != person) {
            throw new IllegalArgumentException("Person is not registered in the university.");
        }
        if (credentials.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("Username '" + name + "' is already taken.");
        }
        if (usernameOf(person) != null) {
            throw new IllegalStateException(person.getFullName() + " already has an account.");
        }
        String salt = newSalt();
        credentials.put(name.toLowerCase(), new Credential(person.getUniversityId(), salt, hash(salt, password)));
    }

    /** Admin sets a new password for an existing username. */
    public void resetPassword(String username, char[] newPassword) {
        requirePassword(newPassword);
        Credential credential = credentials.get(Validation.optionalText(username).toLowerCase());
        if (credential == null) {
            throw new IllegalArgumentException("No account with username '" + Validation.optionalText(username) + "'.");
        }
        credential.salt = newSalt();
        credential.hash = hash(credential.salt, newPassword);
    }

    /** Removes the login only; the Person record stays (use University.deletePerson for that). */
    public void deleteAccount(String username) {
        if (credentials.remove(Validation.optionalText(username).toLowerCase()) == null) {
            throw new IllegalArgumentException("No account with username '" + Validation.optionalText(username) + "'.");
        }
    }

    /** The username of this person, or null when they have no account yet. */
    public String usernameOf(Person person) {
        Validation.requireNonNull(person, "Person");
        for (Map.Entry<String, Credential> e : credentials.entrySet()) {
            if (e.getValue().universityId.equals(person.getUniversityId())) {
                return e.getKey();
            }
        }
        return null;
    }

    public boolean isUsernameTaken(String username) {
        return credentials.containsKey(Validation.optionalText(username).toLowerCase());
    }

    // ---- helpers ----

    private String requireUsername(String username) {
        String name = Validation.requireText(username, "Username");
        if (name.length() < MIN_USERNAME_LENGTH || !name.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("Username must be at least " + MIN_USERNAME_LENGTH
                    + " characters: letters, digits, '.', '_' or '-'.");
        }
        return name;
    }

    private void requirePassword(char[] password) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    private String newSalt() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String hash(String salt, char[] password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            digest.update(new String(password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    private static boolean matches(Credential credential, char[] password) {
        return MessageDigest.isEqual(
                credential.hash.getBytes(StandardCharsets.UTF_8),
                hash(credential.salt, password).getBytes(StandardCharsets.UTF_8));
    }
}
