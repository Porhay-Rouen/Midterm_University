package model.report;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.course.CourseOffering;
import model.course.CourseSchedule;
import model.course.Semester;
import model.person.Person;
import model.person.Role;
import model.person.StudentRole;
import model.record.Transcript;
import model.university.Degree;
import model.university.Department;
import model.university.University;
import model.util.Validation;

/**
 * Read-only reports for the Admin dashboard and the Registrar pages.
 * Methods that return List&lt;Object[]&gt; give one Object[] per table row so they
 * can go straight into a DefaultTableModel (column names are noted on each method).
 *
 * @author Porhay Rouen
 */
public class UniversityReports {

    private final University university;

    public UniversityReports(University university) {
        this.university = Validation.requireNonNull(university, "University");
    }

    // ---------------- Admin dashboard ----------------

    /** Active users per role, e.g. Student=10, Faculty=10. */
    public Map<String, Integer> activeUsersByRole() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Role.Type type : Role.Type.values()) {
            map.put(type.toString(), 0);
        }
        for (Person p : university.getPersons()) {
            if (p.isActive()) {
                for (Role r : p.getRoles()) {
                    map.merge(r.getType().toString(), 1, Integer::sum);
                }
            }
        }
        return map;
    }

    /** Number of course offerings per semester (all departments). */
    public Map<String, Integer> coursesOfferedPerSemester() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Semester s : university.getSemesters()) {
            map.put(s.getName(), 0);
        }
        for (Department d : university.getAllDepartments()) {
            for (CourseSchedule schedule : d.getSchedules()) {
                map.merge(schedule.getSemester().getName(), schedule.getOfferings().size(), Integer::sum);
            }
        }
        return map;
    }

    /** Columns: Semester, Course ID, Course Name, Faculty, Enrolled, Capacity. */
    public List<Object[]> enrolledPerCourse() {
        List<Object[]> rows = new ArrayList<>();
        for (Department d : university.getAllDepartments()) {
            for (CourseSchedule schedule : d.getSchedules()) {
                for (CourseOffering o : schedule.getOfferings()) {
                    rows.add(new Object[] {schedule.getSemester().getName(), o.getCourse().getCourseId(),
                        o.getTitle(), o.getFacultyName(), o.getEnrolledCount(), o.getCapacity()});
                }
            }
        }
        return rows;
    }

    /** Sum of paid tuition records from all students (payments minus refunds). */
    public double tuitionRevenueTotal() {
        double total = 0;
        for (Department d : university.getAllDepartments()) {
            for (StudentRole s : d.getStudentDirectory().getStudents()) {
                total += s.getNetTuitionPaid();
            }
        }
        return total;
    }

    // ---------------- Registrar finance ----------------

    /** Columns: Student ID, Name, Department, Balance, Status (Paid / Unpaid). */
    public List<Object[]> paymentStatusOfAllStudents() {
        List<Object[]> rows = new ArrayList<>();
        for (Department d : university.getAllDepartments()) {
            for (StudentRole s : d.getStudentDirectory().getStudents()) {
                double balance = s.getBalance();
                rows.add(new Object[] {s.getPerson().getUniversityId(), s.getPerson().getFullName(),
                    d.getName(), balance, balance > 0 ? "Unpaid" : "Paid"});
            }
        }
        return rows;
    }

    /** Tuition already paid for offerings in the semester. */
    public double tuitionCollected(Semester semester) {
        double total = 0;
        for (Department d : university.getAllDepartments()) {
            CourseSchedule schedule = d.findSchedule(semester);
            if (schedule != null) {
                for (CourseOffering o : schedule.getOfferings()) {
                    total += o.getTuitionCollected();
                }
            }
        }
        return total;
    }

    /** Tuition billed but unpaid for offerings in the semester. */
    public double tuitionUnpaid(Semester semester) {
        double total = 0;
        for (Department d : university.getAllDepartments()) {
            CourseSchedule schedule = d.findSchedule(semester);
            if (schedule != null) {
                for (CourseOffering o : schedule.getOfferings()) {
                    total += o.getTuitionUnpaid();
                }
            }
        }
        return total;
    }

    /** Department name to tuition collected in the semester. */
    public Map<String, Double> revenueByDepartment(Semester semester) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Department d : university.getAllDepartments()) {
            double total = 0;
            CourseSchedule schedule = d.findSchedule(semester);
            if (schedule != null) {
                for (CourseOffering o : schedule.getOfferings()) {
                    total += o.getTuitionCollected();
                }
            }
            map.put(d.getName(), total);
        }
        return map;
    }

    // ---------------- Registrar reports ----------------

    /** Columns: Department, Semester, Course ID, Course Name, Enrolled. */
    public List<Object[]> enrollmentByDepartmentAndCourse() {
        List<Object[]> rows = new ArrayList<>();
        for (Department d : university.getAllDepartments()) {
            for (CourseSchedule schedule : d.getSchedules()) {
                for (CourseOffering o : schedule.getOfferings()) {
                    rows.add(new Object[] {d.getName(), schedule.getSemester().getName(),
                        o.getCourse().getCourseId(), o.getTitle(), o.getEnrolledCount()});
                }
            }
        }
        return rows;
    }

    /**
     * Overall-GPA distribution per degree program.
     * Columns: Program, 3.5 - 4.0, 3.0 - 3.49, 2.0 - 2.99, Below 2.0, No GPA yet.
     */
    public List<Object[]> gpaDistributionByProgram() {
        Map<String, int[]> map = new LinkedHashMap<>();
        for (Department d : university.getAllDepartments()) {
            for (Degree degree : d.getDegrees()) {
                map.put(degree.getName(), new int[5]);
            }
        }
        for (Department d : university.getAllDepartments()) {
            for (StudentRole s : d.getStudentDirectory().getStudents()) {
                String program = s.getDegree() == null ? "No program" : s.getDegree().getName();
                int[] bands = map.computeIfAbsent(program, k -> new int[5]);
                Transcript t = s.getTranscript();
                if (!t.hasGradedCourses()) {
                    bands[4]++;
                } else {
                    double gpa = t.getOverallGPA();
                    if (gpa >= 3.5) bands[0]++;
                    else if (gpa >= 3.0) bands[1]++;
                    else if (gpa >= 2.0) bands[2]++;
                    else bands[3]++;
                }
            }
        }
        List<Object[]> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> e : map.entrySet()) {
            int[] b = e.getValue();
            rows.add(new Object[] {e.getKey(), b[0], b[1], b[2], b[3], b[4]});
        }
        return rows;
    }
}
