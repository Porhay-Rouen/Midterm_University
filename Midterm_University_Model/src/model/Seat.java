package model;

/** One seat of a course offering. Holds at most one SeatAssignment. @author (your name) */
public class Seat {
    private int number;
    private SeatAssignment assignment;

    public Seat(int number) { this.number = number; }

    public boolean isOccupied() { return assignment != null; }
    public int getNumber() { return number; }
    public SeatAssignment getAssignment() { return assignment; }
    public void setAssignment(SeatAssignment assignment) { this.assignment = assignment; }
}
