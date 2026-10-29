package model;

/** One row of the transcript table. @author (your name) */
public class TranscriptRecord {
    private String term, standing, courseId, courseName, grade;
    private double termGPA, overallGPA;

    public TranscriptRecord(String term, String standing, String courseId, String courseName, String grade, double termGPA, double overallGPA) {
        this.term = term; this.standing = standing; this.courseId = courseId; this.courseName = courseName;
        this.grade = grade; this.termGPA = termGPA; this.overallGPA = overallGPA;
    }

    /** Values in the transcript column order, GPAs rounded to 2 decimals. */
    public Object[] toRow() {
        return new Object[]{term, standing, courseId, courseName, grade, GradeScale.round(termGPA), GradeScale.round(overallGPA)};
    }

    public String getTerm() { return term; }
    public String getStanding() { return standing; }
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getGrade() { return grade; }
    public double getTermGPA() { return termGPA; }
    public double getOverallGPA() { return overallGPA; }
}
