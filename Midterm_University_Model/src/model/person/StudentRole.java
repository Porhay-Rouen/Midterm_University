package model.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.course.CourseOffering;
import model.course.SeatAssignment;
import model.course.Semester;
import model.record.CourseLoad;
import model.record.Payment;
import model.record.Transcript;
import model.university.Course;
import model.university.Degree;
import model.university.Department;
import model.util.Validation;

/**
 * A student: department, degree program, transcript, tuition balance and
 * payment history. Enroll/drop, tuition and the graduation audit live here.
 * Create with StudentDirectory.newStudentRole(...) or University.registerStudent(...).
 *
 * @author Porhay Rouen
 */
public class StudentRole extends Role {

    private final Department department;
    private Degree degree;
    private final Transcript transcript;
    private final List<Payment> payments = new ArrayList<>();

    public StudentRole(Person person, Department department, Degree degree) {
        super(person);
        this.department = Validation.requireNonNull(department, "Department");
        this.degree = degree;
        this.transcript = new Transcript(this);
    }

    @Override
    public Type getType() {
        return Type.STUDENT;
    }

    @Override
    public Department getDepartment() {
        return department;
    }

    public Degree getDegree() {
        return degree;
    }

    public void setDegree(Degree degree) {
        this.degree = degree;
    }

    public Transcript getTranscript() {
        return transcript;
    }

    // ---- registration ----

    /**
     * Enrolls in an offering. Throws IllegalStateException when: already enrolled,
     * enrollment closed, no open seats, or the 8-credit semester limit would be exceeded.
     * Tuition is billed automatically.
     */
    public SeatAssignment enroll(CourseOffering offering) {
        Validation.requireNonNull(offering, "Course offering");
        Semester semester = offering.getSemester();
        CourseLoad load = transcript.findCourseLoad(semester);
        if (load != null && load.findByOffering(offering) != null) {
            throw new IllegalStateException("Already enrolled in " + offering.getCourse().getCourseId() + ".");
        }
        int current = load == null ? 0 : load.getTotalCredits();
        if (current + offering.getCourse().getCredits() > CourseLoad.MAX_CREDITS) {
            throw new IllegalStateException("Cannot exceed " + CourseLoad.MAX_CREDITS
                    + " credit hours in " + semester + " (currently " + current + ").");
        }
        SeatAssignment sa = offering.assignSeat(this);
        transcript.getOrCreateCourseLoad(semester).add(sa);
        return sa;
    }

    /**
     * Drops a course that has no final grade yet. If the tuition was already paid,
     * a refund is added to the payment history; otherwise the charge just disappears.
     */
    public void drop(CourseOffering offering) {
        Validation.requireNonNull(offering, "Course offering");
        CourseLoad load = transcript.findCourseLoad(offering.getSemester());
        SeatAssignment sa = load == null ? null : load.findByOffering(offering);
        if (sa == null) {
            throw new IllegalStateException("Not enrolled in " + offering.getCourse().getCourseId() + ".");
        }
        if (sa.getGrade().isFinal()) {
            throw new IllegalStateException("Cannot drop a course that already has a final grade.");
        }
        load.remove(sa);
        offering.releaseSeat(sa);
        if (sa.isPaid()) {
            payments.add(Payment.refund(sa.getTuition(),
                    "Refund for dropped course " + offering.getCourse().getCourseId()));
        }
    }

    /** Every SeatAssignment across all semesters. */
    public List<SeatAssignment> getAllSeatAssignments() {
        List<SeatAssignment> all = new ArrayList<>();
        for (CourseLoad load : transcript.getCourseLoads()) {
            all.addAll(load.getSeatAssignments());
        }
        return all;
    }

    /** Frees every seat held by this student (used when the account is deleted). */
    public void releaseAllSeats() {
        for (SeatAssignment sa : getAllSeatAssignments()) {
            sa.getCourseOffering().releaseSeat(sa);
        }
    }

    // ---- tuition ----

    /** Amount owed = tuition of all enrolled courses that are not paid yet. Starts at 0. */
    public double getBalance() {
        double total = 0;
        for (SeatAssignment sa : getAllSeatAssignments()) {
            if (!sa.isPaid()) {
                total += sa.getTuition();
            }
        }
        return total;
    }

    /**
     * Pays the whole balance. Returns the Payment, or null when the balance is 0
     * (nothing is recorded in that case, so the UI should show a "no balance to pay" dialog).
     */
    public Payment pay() {
        double balance = getBalance();
        if (balance <= 0) {
            return null;
        }
        for (SeatAssignment sa : getAllSeatAssignments()) {
            if (!sa.isPaid()) {
                sa.markPaid();
            }
        }
        Payment payment = Payment.payment(balance, "Tuition payment");
        payments.add(payment);
        return payment;
    }

    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    /** Net money paid = payments minus refunds. */
    public double getNetTuitionPaid() {
        double total = 0;
        for (Payment p : payments) {
            total += p.isRefund() ? -p.getAmount() : p.getAmount();
        }
        return total;
    }

    /** The transcript stays locked until the balance is paid off. */
    public boolean canViewTranscript() {
        return getBalance() <= 0;
    }

    // ---- graduation audit ----

    /** Credits earned with a passing final grade. */
    public int getCreditsCompleted() {
        int total = 0;
        for (SeatAssignment sa : getAllSeatAssignments()) {
            if (sa.getGrade().isPassing()) {
                total += sa.getCredits();
            }
        }
        return total;
    }

    /** Credits of courses still in progress. */
    public int getCreditsInProgress() {
        int total = 0;
        for (SeatAssignment sa : getAllSeatAssignments()) {
            if (!sa.getGrade().isFinal()) {
                total += sa.getCredits();
            }
        }
        return total;
    }

    /** True when every core course of the degree has a passing grade. */
    public boolean isCoreCompleted() {
        if (degree == null) {
            return false;
        }
        for (Course core : degree.getCoreCourses()) {
            boolean passed = false;
            for (SeatAssignment sa : getAllSeatAssignments()) {
                if (sa.getCourse() == core && sa.getGrade().isPassing()) {
                    passed = true;
                    break;
                }
            }
            if (!passed) {
                return false;
            }
        }
        return true;
    }

    /** Ready to graduate: required credits earned and all core courses passed. */
    public boolean isReadyToGraduate() {
        return degree != null && getCreditsCompleted() >= degree.getRequiredCredits() && isCoreCompleted();
    }

    /** 0-100 for a JProgressBar. */
    public int getGraduationProgressPercent() {
        if (degree == null) {
            return 0;
        }
        return (int) Math.min(100, getCreditsCompleted() * 100.0 / degree.getRequiredCredits());
    }
}
