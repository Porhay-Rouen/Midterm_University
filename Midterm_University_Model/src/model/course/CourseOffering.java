package model.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import model.person.FacultyRole;
import model.person.StudentRole;
import model.record.Grade;
import model.university.Course;
import model.util.Validation;

/**
 * A course offered in one semester (Task 4 item 5: the enrolled count is
 * the number of seats that hold a SeatAssignment). Owns its Seats and
 * Assignments. Create with CourseSchedule.newOffering(...).
 *
 * @author Porhay Rouen
 */
public class CourseOffering {

    private final CourseSchedule schedule;
    private final Course course;
    private FacultyRole faculty;
    private String title;
    private String description;
    private String room;
    private String meetingTime;
    private String syllabusPath = "";
    private boolean enrollmentOpen = true;
    private int nextSeatNumber = 1;
    private final List<Seat> seats = new ArrayList<>();
    private final List<Assignment> assignments = new ArrayList<>();

    CourseOffering(CourseSchedule schedule, Course course, FacultyRole faculty,
            int capacity, String room, String meetingTime) {
        this.schedule = schedule;
        this.course = course;
        this.title = course.getName();
        this.description = course.getDescription();
        this.room = Validation.optionalText(room);
        this.meetingTime = Validation.optionalText(meetingTime);
        Validation.requirePositive(capacity, "Capacity");
        for (int i = 0; i < capacity; i++) {
            seats.add(new Seat(this, nextSeatNumber++));
        }
        assignFaculty(faculty);
    }

    // ---- basic info ----

    public CourseSchedule getSchedule() {
        return schedule;
    }

    public Course getCourse() {
        return course;
    }

    public Semester getSemester() {
        return schedule.getSemester();
    }

    public FacultyRole getFaculty() {
        return faculty;
    }

    /** Assigns (or reassigns) the teacher; null clears it. */
    public void assignFaculty(FacultyRole newFaculty) {
        if (faculty != null) {
            faculty.detachOffering(this);
        }
        faculty = newFaculty;
        if (faculty != null) {
            faculty.attachOffering(this);
        }
    }

    public String getFacultyName() {
        return faculty == null ? "Unassigned" : faculty.getPerson().getFullName();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Validation.requireText(title, "Title");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Validation.optionalText(description);
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = Validation.optionalText(room);
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    /** Day/time schedule text, for example "Mon 6:00-9:00 PM". */
    public void setMeetingTime(String meetingTime) {
        this.meetingTime = Validation.optionalText(meetingTime);
    }

    public String getSyllabusPath() {
        return syllabusPath;
    }

    /** Upload or replace the syllabus (store the chosen file path). */
    public void setSyllabusPath(String syllabusPath) {
        this.syllabusPath = Validation.requireText(syllabusPath, "Syllabus file");
    }

    // ---- enrollment ----

    public boolean isEnrollmentOpen() {
        return enrollmentOpen;
    }

    public void setEnrollmentOpen(boolean open) {
        this.enrollmentOpen = open;
    }

    /** Faculty may only open/close enrollment before the semester starts. */
    public boolean canChangeEnrollment() {
        return !getSemester().hasStarted();
    }

    public int getCapacity() {
        return seats.size();
    }

    /** Changes capacity; cannot go below the number of enrolled students. */
    public void setCapacity(int capacity) {
        Validation.requirePositive(capacity, "Capacity");
        if (capacity < getEnrolledCount()) {
            throw new IllegalArgumentException("Capacity cannot be lower than the " + getEnrolledCount()
                    + " students already enrolled.");
        }
        while (seats.size() < capacity) {
            seats.add(new Seat(this, nextSeatNumber++));
        }
        for (int i = seats.size() - 1; i >= 0 && seats.size() > capacity; i--) {
            if (!seats.get(i).isOccupied()) {
                seats.remove(i);
            }
        }
    }

    /** Number of students signed up (seats holding a SeatAssignment). */
    public int getEnrolledCount() {
        int count = 0;
        for (Seat s : seats) {
            if (s.isOccupied()) {
                count++;
            }
        }
        return count;
    }

    public int getOpenSeats() {
        return getCapacity() - getEnrolledCount();
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    /** All SeatAssignments, in seat order (the enrolled-students table). */
    public List<SeatAssignment> getSeatAssignments() {
        List<SeatAssignment> result = new ArrayList<>();
        for (Seat s : seats) {
            if (s.isOccupied()) {
                result.add(s.getAssignment());
            }
        }
        return result;
    }

    /** Used by StudentRole.enroll; takes the first free seat. */
    public SeatAssignment assignSeat(StudentRole student) {
        if (!enrollmentOpen) {
            throw new IllegalStateException("Enrollment is closed for " + course.getCourseId() + ".");
        }
        for (Seat seat : seats) {
            if (!seat.isOccupied()) {
                SeatAssignment sa = new SeatAssignment(seat, student);
                seat.setAssignment(sa);
                return sa;
            }
        }
        throw new IllegalStateException("No open seats left in " + course.getCourseId() + ".");
    }

    /** Used by StudentRole.drop; frees the seat. */
    public void releaseSeat(SeatAssignment sa) {
        sa.getSeat().clear();
    }

    // ---- assignments ----

    public Assignment addAssignment(String title, double maxScore) {
        Assignment a = new Assignment(title, maxScore);
        assignments.add(a);
        return a;
    }

    public void removeAssignment(Assignment a) {
        if (assignments.remove(a)) {
            for (SeatAssignment sa : getSeatAssignments()) {
                sa.forgetAssignment(a);
            }
        }
    }

    public List<Assignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    // ---- reports ----

    /** Students ranked by total grade percentage, highest first. */
    public List<SeatAssignment> getRankedSeatAssignments() {
        List<SeatAssignment> list = getSeatAssignments();
        list.sort(Comparator.comparingDouble(SeatAssignment::getPercentage).reversed());
        return list;
    }

    /** Class GPA = average grade points of students that have a final grade (0 when none). */
    public double getClassGPA() {
        double sum = 0;
        int count = 0;
        for (SeatAssignment sa : getSeatAssignments()) {
            if (sa.getGrade().isFinal()) {
                sum += sa.getGrade().getPoints();
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    /** Average total percentage of all enrolled students. */
    public double getAveragePercentage() {
        List<SeatAssignment> list = getSeatAssignments();
        if (list.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (SeatAssignment sa : list) {
            sum += sa.getPercentage();
        }
        return sum / list.size();
    }

    /** How many students hold each letter grade (every Grade is a key). */
    public Map<Grade, Integer> getGradeDistribution() {
        Map<Grade, Integer> map = new EnumMap<>(Grade.class);
        for (Grade g : Grade.values()) {
            map.put(g, 0);
        }
        for (SeatAssignment sa : getSeatAssignments()) {
            map.merge(sa.getGrade(), 1, Integer::sum);
        }
        return map;
    }

    /** Tuition already paid by students enrolled in this offering. */
    public double getTuitionCollected() {
        double total = 0;
        for (SeatAssignment sa : getSeatAssignments()) {
            if (sa.isPaid()) {
                total += sa.getTuition();
            }
        }
        return total;
    }

    /** Tuition billed but not yet paid. */
    public double getTuitionUnpaid() {
        double total = 0;
        for (SeatAssignment sa : getSeatAssignments()) {
            if (!sa.isPaid()) {
                total += sa.getTuition();
            }
        }
        return total;
    }

    @Override
    public String toString() {
        return course.getCourseId() + " - " + title + " (" + getSemester() + ")";
    }
}
