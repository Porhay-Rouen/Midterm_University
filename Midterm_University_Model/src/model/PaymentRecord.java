package model;

import java.time.LocalDateTime;

/** One line of a student's payment history: a payment or a refund. @author (your name) */
public class PaymentRecord {
    public static final String PAYMENT = "Payment";
    public static final String REFUND = "Refund";
    private String type;
    private double amount;
    private String semester;
    private String department;
    private String description;
    private LocalDateTime date = LocalDateTime.now();

    public PaymentRecord(String type, double amount, String semester, String department, String description) {
        this.type = type; this.amount = amount; this.semester = semester; this.department = department; this.description = description;
    }

    /** Positive for a payment, negative for a refund. */
    public double getSignedAmount() { return PAYMENT.equals(type) ? amount : -amount; }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getSemester() { return semester; }
    public String getDepartment() { return department; }
    public String getDescription() { return description; }
    public LocalDateTime getDate() { return date; }
}
