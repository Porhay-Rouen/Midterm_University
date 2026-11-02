package model.util;

import java.util.regex.Pattern;

/**
 * Shared input validation helpers (null checks, text, email, phone, numbers).
 * Every model class uses these so validation lives in one place.
 *
 * @author Porhay Rouen
 */
public final class Validation {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private Validation() {
    }

    /** Throws IllegalArgumentException when the value is null. */
    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value;
    }

    /** Returns the trimmed text, or throws when it is null or blank. */
    public static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        return value.trim();
    }

    /** Returns trimmed text; null becomes an empty string. */
    public static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isValidEmail(String value) {
        return value != null && EMAIL.matcher(value.trim()).matches();
    }

    /** Returns the trimmed email, or throws when the format is wrong. */
    public static String requireEmail(String value) {
        String email = requireText(value, "Email");
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email format is invalid: " + email);
        }
        return email;
    }

    /** Phone is optional; when present it must look like a phone number. */
    public static String optionalPhone(String value) {
        String phone = optionalText(value);
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            throw new IllegalArgumentException("Phone number format is invalid: " + phone);
        }
        return phone;
    }

    public static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0.");
        }
        return value;
    }

    public static double requireNonNegative(double value, String field) {
        if (value < 0 || Double.isNaN(value)) {
            throw new IllegalArgumentException(field + " cannot be negative.");
        }
        return value;
    }
}
