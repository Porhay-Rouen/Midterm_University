package model;

import java.time.LocalDate;

/** A term such as "Fall 2026" with its start date. @author (your name) */
public class Semester {
    private final String name;
    private final LocalDate startDate;

    public Semester(String name, LocalDate startDate) {
        this.name = Validator.requireText(name, "Semester name");
        this.startDate = Validator.requireNonNull(startDate, "Start date");
    }

    public boolean hasStarted() { return !LocalDate.now().isBefore(startDate); }

    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }

    @Override public boolean equals(Object o) { return (o instanceof Semester) && ((Semester) o).name.equalsIgnoreCase(name); }
    @Override public int hashCode() { return name.toLowerCase().hashCode(); }
    @Override public String toString() { return name; }
}
