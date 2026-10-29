package model;

/** Shared input checks. Every setter and constructor in the model uses these. @author (your name) */
public final class Validator {
    private Validator() { }

    public static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " is required.");
        return value.trim();
    }

    public static String requireEmail(String value) {
        String email = requireText(value, "Email");
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) throw new IllegalArgumentException("Email format is invalid.");
        return email.toLowerCase();
    }

    public static int requirePositive(int value, String field) {
        if (value <= 0) throw new IllegalArgumentException(field + " must be greater than 0.");
        return value;
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required.");
        return value;
    }
}
