package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** A student's tuition balance (starts at 0) and payment history. @author (your name) */
public class TuitionAccount {
    private StudentRole owner;
    private double balance;
    private ArrayList<PaymentRecord> history = new ArrayList<>();

    public TuitionAccount(StudentRole owner) { this.owner = owner; }

    /** Called when the student enrolls: the course tuition is billed. */
    void bill(SeatAssignment sa) {
        double amount = sa.getOffering().getCourse().getTuition();
        sa.setBilledAmount(amount);
        sa.setPaid(false);
        balance += amount;
    }

    /** Called when the student drops: removes the bill, or records a refund if it was already paid. */
    void cancelBill(SeatAssignment sa) {
        if (sa.isPaid()) {
            history.add(new PaymentRecord(PaymentRecord.REFUND, sa.getBilledAmount(), sa.getOffering().getSemester().getName(),
                    owner.getPerson().getDepartment(), "Refund for dropping " + sa.getOffering().getCourse().getCourseId()));
        } else {
            balance -= sa.getBilledAmount();
        }
    }

    /**
     * Pays the whole balance. Returns the payment records created (one per semester),
     * or null if the balance is 0 or negative (nothing happens and nothing is recorded).
     */
    public ArrayList<PaymentRecord> pay() {
        if (balance <= 0) return null;
        LinkedHashMap<String, Double> bySemester = new LinkedHashMap<>();
        for (CourseLoad cl : owner.getTranscript().getCourseLoads()) {
            for (SeatAssignment a : cl.getSeatAssignments()) {
                if (!a.isPaid()) {
                    Double old = bySemester.get(cl.getSemester().getName());
                    bySemester.put(cl.getSemester().getName(), (old == null ? 0 : old) + a.getBilledAmount());
                    a.setPaid(true);
                }
            }
        }
        ArrayList<PaymentRecord> made = new ArrayList<>();
        for (Map.Entry<String, Double> e : bySemester.entrySet()) {
            PaymentRecord r = new PaymentRecord(PaymentRecord.PAYMENT, e.getValue(), e.getKey(), owner.getPerson().getDepartment(), "Tuition payment");
            history.add(r);
            made.add(r);
        }
        balance = 0;
        return made;
    }

    /** Payments minus refunds. */
    public double getNetPaid() {
        double sum = 0;
        for (PaymentRecord r : history) sum += r.getSignedAmount();
        return sum;
    }

    public double getBalance() { return balance; }
    public ArrayList<PaymentRecord> getHistory() { return history; }
}
