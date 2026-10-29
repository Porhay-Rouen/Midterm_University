package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Summary numbers for the Admin dashboard, Faculty reports and Registrar reports.
 * Methods that return rows (Object[]) can be added straight into a DefaultTableModel.
 * @author (your name)
 */
public class Reports {
    public static final String[] GPA_BUCKETS = {"3.5 - 4.0", "3.0 - 3.49", "2.5 - 2.99", "Below 2.5"};
    private final Department dept;

    public Reports(Department dept) { this.dept = Validator.requireNonNull(dept, "Department"); }

    /** Active users per role: Student, Faculty, Registrar, Admin. */
    public LinkedHashMap<String, Integer> activeUsersByRole() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        for (String r : new String[]{"Student", "Faculty", "Registrar", "Admin"}) m.put(r, 0);
        for (Person p : dept.getPersonDirectory().getPersons()) {
            if (!p.isActive()) continue;
            for (Role r : p.getRoles()) m.put(r.getRoleName(), m.get(r.getRoleName()) + 1);
        }
        return m;
    }

    /** Number of offerings per semester. */
    public LinkedHashMap<String, Integer> coursesPerSemester() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        for (CourseSchedule cs : dept.getCourseSchedules()) m.put(cs.getSemester().getName(), cs.getOfferings().size());
        return m;
    }

    /** Rows: department, semester, course ID, title, enrolled. Pass null for all semesters. */
    public ArrayList<Object[]> enrollmentByCourse(Semester semester) {
        ArrayList<Object[]> rows = new ArrayList<>();
        for (CourseSchedule cs : dept.getCourseSchedules()) {
            if (semester != null && !cs.getSemester().equals(semester)) continue;
            for (CourseOffering o : cs.getOfferings()) {
                rows.add(new Object[]{dept.getName(), cs.getSemester().getName(), o.getCourse().getCourseId(), o.getTitle(), o.getEnrolledCount()});
            }
        }
        return rows;
    }

    /** Total tuition revenue: payments minus refunds across all students. */
    public double tuitionRevenue() {
        double sum = 0;
        for (StudentRole s : dept.getStudentDirectory().getStudents()) sum += s.getTuitionAccount().getNetPaid();
        return sum;
    }

    /** Tuition collected (net of refunds) for one semester. */
    public double tuitionCollected(Semester semester) {
        double sum = 0;
        for (StudentRole s : dept.getStudentDirectory().getStudents()) {
            for (PaymentRecord r : s.getTuitionAccount().getHistory()) if (r.getSemester().equalsIgnoreCase(semester.getName())) sum += r.getSignedAmount();
        }
        return sum;
    }

    /** Tuition billed but not yet paid for one semester. */
    public double unpaidTuition(Semester semester) {
        double sum = 0;
        for (StudentRole s : dept.getStudentDirectory().getStudents()) {
            CourseLoad cl = s.getTranscript().getCourseLoad(semester);
            if (cl == null) continue;
            for (SeatAssignment a : cl.getSeatAssignments()) if (!a.isPaid()) sum += a.getBilledAmount();
        }
        return sum;
    }

    /** Net revenue per department (from the payment history). */
    public LinkedHashMap<String, Double> revenueByDepartment() {
        LinkedHashMap<String, Double> m = new LinkedHashMap<>();
        for (StudentRole s : dept.getStudentDirectory().getStudents()) {
            for (PaymentRecord r : s.getTuitionAccount().getHistory()) {
                Double old = m.get(r.getDepartment());
                m.put(r.getDepartment(), (old == null ? 0 : old) + r.getSignedAmount());
            }
        }
        return m;
    }

    /** Rows: university ID, name, balance, status (Paid or Unpaid). */
    public ArrayList<Object[]> tuitionStatusRows() {
        ArrayList<Object[]> rows = new ArrayList<>();
        for (StudentRole s : dept.getStudentDirectory().getStudents()) {
            double b = s.getTuitionAccount().getBalance();
            rows.add(new Object[]{s.getPerson().getPersonId(), s.getPerson().getName(), b, b > 0 ? "Unpaid" : "Paid"});
        }
        return rows;
    }

    /** Rows: program, then one count per GPA_BUCKETS entry. Only students with graded courses are counted. */
    public ArrayList<Object[]> gpaDistribution() {
        LinkedHashMap<String, int[]> byProgram = new LinkedHashMap<>();
        for (StudentRole s : dept.getStudentDirectory().getStudents()) {
            boolean graded = false;
            for (CourseLoad cl : s.getTranscript().getCourseLoads()) if (cl.getGradedCredits() > 0) graded = true;
            if (!graded) continue;
            double gpa = s.getTranscript().getOverallGPA();
            int bucket = gpa >= 3.5 ? 0 : gpa >= 3.0 ? 1 : gpa >= 2.5 ? 2 : 3;
            int[] counts = byProgram.get(s.getProgram());
            if (counts == null) { counts = new int[4]; byProgram.put(s.getProgram(), counts); }
            counts[bucket]++;
        }
        ArrayList<Object[]> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> e : byProgram.entrySet()) {
            int[] c = e.getValue();
            rows.add(new Object[]{e.getKey(), c[0], c[1], c[2], c[3]});
        }
        return rows;
    }
}
