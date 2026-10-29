package access;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import model.Role;

/** A login account tied to one role. Passwords are stored as salted SHA-256 hashes, never as text. @author (your name) */
public class UserAccount {
    private String username;
    private String salt;
    private String passwordHash;
    private Role role;
    private boolean active = true;

    UserAccount(String username, String password, Role role) {
        this.username = username;
        this.role = role;
        setPassword(password);
    }

    void setPassword(String password) {
        byte[] raw = new byte[8];
        new SecureRandom().nextBytes(raw);
        this.salt = toHex(raw);
        this.passwordHash = hash(salt, password);
    }

    boolean checkPassword(String password) { return passwordHash.equals(hash(salt, password)); }

    private static String hash(String salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest((salt + password).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
