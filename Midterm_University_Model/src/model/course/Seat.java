package model.course;

/**
 * One seat in a course offering. It is either free or holds one SeatAssignment.
 *
 * @author Porhay Rouen
 */
public class Seat {

    private final CourseOffering offering;
    private final int seatNumber;
    private SeatAssignment assignment;

    Seat(CourseOffering offering, int seatNumber) {
        this.offering = offering;
        this.seatNumber = seatNumber;
    }

    public CourseOffering getCourseOffering() {
        return offering;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public SeatAssignment getAssignment() {
        return assignment;
    }

    public boolean isOccupied() {
        return assignment != null;
    }

    void setAssignment(SeatAssignment assignment) {
        this.assignment = assignment;
    }

    void clear() {
        this.assignment = null;
    }
}
