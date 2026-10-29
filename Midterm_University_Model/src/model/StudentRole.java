package model;

/** Student: owns a transcript and a tuition account. Enrolling and dropping go through this class. @author (your name) */
public class StudentRole extends Role {
    public static final int REQUIRED_CREDITS = 32;
    public static final String CORE_COURSE_ID = "INFO 5100";

    private Transcript transcript = new Transcript();
    private TuitionAccount tuitionAccount = new TuitionAccount(this);
    private String program = "MSIS";

    public StudentRole(Person person) { super(person); }

    /** Enrolls in an offering and bills the tuition. Throws IllegalStateException with a readable message if not allowed. */
    public SeatAssignment enroll(CourseOffering offering) {
        Validator.requireNonNull(offering, "Course offering");
        CourseLoad load = transcript.newCourseLoad(offering.getSemester());
        SeatAssignment sa = load.add(this, offering);
        tuitionAccount.bill(sa);
        return sa;
    }

    /** Drops a course. The bill is cancelled, or a refund is recorded if it was already paid. */
    public void drop(SeatAssignment sa) {
        CourseLoad load = transcript.findLoadOf(sa);
        if (load == null) throw new IllegalStateException("This course is not in the student's course load.");
        tuitionAccount.cancelBill(sa);
        load.remove(sa);
    }

    /** A student cannot see the transcript until all tuition is paid. */
    public boolean canViewTranscript() { return tuitionAccount.getBalance() <= 0; }

    public int getEarnedCredits() { return transcript.getEarnedCredits(); }
    public boolean hasCompletedCore() { return transcript.hasPassed(CORE_COURSE_ID); }
    public int getCreditsRemaining() { return Math.max(0, REQUIRED_CREDITS - getEarnedCredits()); }
    public boolean isReadyToGraduate() { return getCreditsRemaining() == 0 && hasCompletedCore(); }

    /** Text for the Graduation Audit page. */
    public String getGraduationMessage() {
        if (isReadyToGraduate()) return "Ready to graduate: " + getEarnedCredits() + " of " + REQUIRED_CREDITS + " credit hours earned and " + CORE_COURSE_ID + " completed.";
        StringBuilder sb = new StringBuilder("Not ready to graduate: ");
        if (getCreditsRemaining() > 0) sb.append(getCreditsRemaining()).append(" more credit hours needed. ");
        if (!hasCompletedCore()) sb.append("Core course ").append(CORE_COURSE_ID).append(" not completed.");
        return sb.toString().trim();
    }

    public Transcript getTranscript() { return transcript; }
    public TuitionAccount getTuitionAccount() { return tuitionAccount; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = Validator.requireText(program, "Program"); }
    @Override public String getRoleName() { return "Student"; }
}
