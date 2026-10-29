package model;

import java.util.ArrayList;

/** A student's academic history: all course loads. @author (your name) */
public class Transcript {
    public static final String[] COLUMNS = {"Term", "Academic Standing", "Course ID", "Course Name", "Grade", "Term GPA", "Overall GPA"};
    private ArrayList<CourseLoad> courseLoads = new ArrayList<>();

    public CourseLoad newCourseLoad(Semester semester) {
        CourseLoad existing = getCourseLoad(semester);
        if (existing != null) return existing;
        CourseLoad cl = new CourseLoad(semester);
        courseLoads.add(cl);
        return cl;
    }

    public CourseLoad getCourseLoad(Semester semester) {
        for (CourseLoad cl : courseLoads) if (cl.getSemester().equals(semester)) return cl;
        return null;
    }

    CourseLoad findLoadOf(SeatAssignment sa) {
        for (CourseLoad cl : courseLoads) if (cl.getSeatAssignments().contains(sa)) return cl;
        return null;
    }

    /** Overall GPA = total quality points / total credit hours over all terms. */
    public double getOverallGPA() {
        double q = 0;
        int c = 0;
        for (CourseLoad cl : courseLoads) { q += cl.getQualityPoints(); c += cl.getGradedCredits(); }
        return c == 0 ? 0 : q / c;
    }

    /** Good Standing, Academic Warning or Academic Probation. */
    public static String standingFor(double termGPA, double overallGPA) {
        if (overallGPA < 3.0) return "Academic Probation";
        if (termGPA < 3.0) return "Academic Warning";
        return "Good Standing";
    }

    /** Transcript rows for one semester, or for all semesters if semester is null. */
    public ArrayList<TranscriptRecord> getRecords(Semester semester) {
        ArrayList<TranscriptRecord> rows = new ArrayList<>();
        double overall = getOverallGPA();
        for (CourseLoad cl : courseLoads) {
            if (semester != null && !cl.getSemester().equals(semester)) continue;
            double term = cl.getTermGPA();
            String standing = cl.getGradedCredits() > 0 ? standingFor(term, overall) : "In Progress";
            for (SeatAssignment a : cl.getSeatAssignments()) {
                Course c = a.getOffering().getCourse();
                rows.add(new TranscriptRecord(cl.getSemester().getName(), standing, c.getCourseId(), a.getOffering().getTitle(), a.getGrade(), term, overall));
            }
        }
        return rows;
    }

    /** Credit hours of courses finished with a passing letter grade (not F, not in progress). */
    public int getEarnedCredits() {
        int total = 0;
        for (CourseLoad cl : courseLoads) for (SeatAssignment a : cl.getSeatAssignments()) if (a.getGradePoints() > 0) total += a.getCredits();
        return total;
    }

    public boolean hasPassed(String courseId) {
        for (CourseLoad cl : courseLoads) {
            for (SeatAssignment a : cl.getSeatAssignments()) {
                if (a.getGradePoints() > 0 && a.getOffering().getCourse().getCourseId().equalsIgnoreCase(courseId)) return true;
            }
        }
        return false;
    }

    public ArrayList<CourseLoad> getCourseLoads() { return courseLoads; }
}
