package model.course;

import java.time.LocalDate;
import model.util.Validation;

/**
 * One academic term, for example "Fall 2026". Equality is by name.
 *
 * @author Porhay Rouen
 */
public class Semester implements Comparable<Semester> {

    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Semester(String name, LocalDate startDate, LocalDate endDate) {
        this.name = Validation.requireText(name, "Semester name");
        this.startDate = Validation.requireNonNull(startDate, "Start date");
        this.endDate = Validation.requireNonNull(endDate, "End date");
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Semester end date must be after the start date.");
        }
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /** True when today is on or after the start date (faculty may only open/close enrollment before this). */
    public boolean hasStarted() {
        return !LocalDate.now().isBefore(startDate);
    }

    @Override
    public int compareTo(Semester other) {
        return startDate.compareTo(other.startDate);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Semester && ((Semester) o).name.equalsIgnoreCase(name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
