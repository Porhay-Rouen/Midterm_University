package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

/** One section of a course in one semester. @author (your name) */
public class CourseOffering {
    private Semester semester;
    private Course course;
    private FacultyRole faculty;
    private String title;
    private String description = "";
    private String schedule = "TBA";
    private String room = "TBA";
    private String syllabus = "";
    private boolean enrollmentOpen = true;
    private ArrayList<Seat> seats = new ArrayList<>();
    private ArrayList<Assignment> assignments = new ArrayList<>();

    public CourseOffering(Semester semester, Course course, FacultyRole faculty, int capacity) {
        this.semester = Validator.requireNonNull(semester, "Semester");
        this.course = Validator.requireNonNull(course, "Course");
        this.faculty = Validator.requireNonNull(faculty, "Faculty");
        this.title = course.getName();
        for (int i = 1; i <= Validator.requirePositive(capacity, "Capacity"); i++) seats.add(new Seat(i));
    }

    // ----- seats and enrollment -----
    public Seat findOpenSeat() {
        for (Seat s : seats) if (!s.isOccupied()) return s;
        return null;
    }

    public int getOpenSeatCount() { return seats.size() - getEnrolledCount(); }

    /** Number of students signed up (seats that have a SeatAssignment). */
    public int getEnrolledCount() {
        int n = 0;
        for (Seat s : seats) if (s.isOccupied()) n++;
        return n;
    }

    public ArrayList<SeatAssignment> getEnrolledAssignments() {
        ArrayList<SeatAssignment> list = new ArrayList<>();
        for (Seat s : seats) if (s.isOccupied()) list.add(s.getAssignment());
        return list;
    }

    public int getCapacity() { return seats.size(); }

    public void setCapacity(int newCapacity) {
        Validator.requirePositive(newCapacity, "Capacity");
        if (newCapacity < getEnrolledCount()) {
            throw new IllegalStateException("Capacity cannot be lower than the current enrollment (" + getEnrolledCount() + ").");
        }
        while (seats.size() < newCapacity) seats.add(new Seat(seats.size() + 1));
        while (seats.size() > newCapacity) {
            for (int i = seats.size() - 1; i >= 0; i--) {
                if (!seats.get(i).isOccupied()) { seats.remove(i); break; }
            }
        }
    }

    /** Faculty opens or closes enrollment. Only allowed before the semester starts. */
    public void setEnrollmentOpen(boolean open) {
        if (semester.hasStarted()) throw new IllegalStateException("Enrollment can only be changed before the semester starts.");
        this.enrollmentOpen = open;
    }

    /** System/registrar close, allowed at any time. */
    public void closeEnrollment() { this.enrollmentOpen = false; }

    // ----- coursework and grading -----
    public Assignment newAssignment(String title, double maxPoints) {
        Assignment a = new Assignment(title, maxPoints);
        assignments.add(a);
        return a;
    }

    /** Enrolled students sorted by total percentage, highest first (ungraded last). */
    public ArrayList<SeatAssignment> rankStudents() {
        ArrayList<SeatAssignment> list = getEnrolledAssignments();
        Collections.sort(list, new Comparator<SeatAssignment>() {
            @Override public int compare(SeatAssignment a, SeatAssignment b) { return Double.compare(b.getPercent(), a.getPercent()); }
        });
        return list;
    }

    /** Average grade points of students who have a final letter grade. */
    public double getClassGPA() {
        double sum = 0;
        int n = 0;
        for (SeatAssignment a : getEnrolledAssignments()) if (a.isGraded()) { sum += a.getGradePoints(); n++; }
        return n == 0 ? 0 : sum / n;
    }

    /** Average total percentage of students who have graded work. */
    public double getAveragePercent() {
        double sum = 0;
        int n = 0;
        for (SeatAssignment a : getEnrolledAssignments()) if (a.getPercent() >= 0) { sum += a.getPercent(); n++; }
        return n == 0 ? 0 : sum / n;
    }

    /** Count of each letter grade (all letters present, in grade order). */
    public LinkedHashMap<String, Integer> getGradeDistribution() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        for (String l : GradeScale.LETTERS) m.put(l, 0);
        for (SeatAssignment a : getEnrolledAssignments()) if (a.isGraded()) m.put(a.getGrade(), m.get(a.getGrade()) + 1);
        return m;
    }

    public double getTuitionCollected() {
        double sum = 0;
        for (SeatAssignment a : getEnrolledAssignments()) if (a.isPaid()) sum += a.getBilledAmount();
        return sum;
    }

    // ----- details -----
    public Semester getSemester() { return semester; }
    public Course getCourse() { return course; }
    public FacultyRole getFaculty() { return faculty; }
    public void setFaculty(FacultyRole faculty) { this.faculty = Validator.requireNonNull(faculty, "Faculty"); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = Validator.requireText(title, "Title"); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description.trim(); }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = Validator.requireText(schedule, "Schedule"); }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = Validator.requireText(room, "Room"); }
    public String getSyllabus() { return syllabus; }
    public void setSyllabus(String syllabus) { this.syllabus = syllabus == null ? "" : syllabus.trim(); }
    public boolean isEnrollmentOpen() { return enrollmentOpen; }
    public ArrayList<Seat> getSeats() { return seats; }
    public ArrayList<Assignment> getAssignments() { return assignments; }
    @Override public String toString() { return course.getCourseId() + " - " + title; }
}
