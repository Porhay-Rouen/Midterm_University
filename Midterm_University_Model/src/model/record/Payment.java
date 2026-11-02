package model.record;

import java.time.LocalDateTime;

/**
 * One line of a student's payment history: either a tuition payment or a refund.
 *
 * @author Porhay Rouen
 */
public class Payment {

    private final double amount;
    private final boolean refund;
    private final String description;
    private final LocalDateTime timestamp = LocalDateTime.now();

    private Payment(double amount, boolean refund, String description) {
        this.amount = amount;
        this.refund = refund;
        this.description = description;
    }

    public static Payment payment(double amount, String description) {
        return new Payment(amount, false, description);
    }

    public static Payment refund(double amount, String description) {
        return new Payment(amount, true, description);
    }

    public double getAmount() {
        return amount;
    }

    public boolean isRefund() {
        return refund;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /** "Payment" or "Refund", handy for a JTable column. */
    public String getTypeLabel() {
        return refund ? "Refund" : "Payment";
    }
}
