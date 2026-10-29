package model;

import java.util.ArrayList;

/** The main entry point of the model. UI panels receive a Department. @author (your name) */
public class Department {
    private String name;
    private CourseCatalog courseCatalog = new CourseCatalog();
    private PersonDirectory personDirectory = new PersonDirectory();
    private StudentDirectory studentDirectory = new StudentDirectory();
    private FacultyDirectory facultyDirectory = new FacultyDirectory();
    private RegistrarDirectory registrarDirectory = new RegistrarDirectory();
    private AdminDirectory adminDirectory = new AdminDirectory();
    private ArrayList<Degree> degrees = new ArrayList<>();
    private ArrayList<CourseSchedule> schedules = new ArrayList<>();

    public Department(String name) { this.name = Validator.requireText(name, "Department name"); }

    public Degree newDegree(String degreeName) {
        Degree d = new Degree(degreeName);
        degrees.add(d);
        return d;
    }

    /** Returns the schedule of the semester, creating it if needed. */
    public CourseSchedule newCourseSchedule(Semester semester) {
        CourseSchedule existing = getCourseSchedule(semester);
        if (existing != null) return existing;
        CourseSchedule cs = new CourseSchedule(semester);
        schedules.add(cs);
        return cs;
    }

    public CourseSchedule getCourseSchedule(Semester semester) {
        for (CourseSchedule cs : schedules) if (cs.getSemester().equals(semester)) return cs;
        return null;
    }

    public ArrayList<CourseOffering> getAllOfferings() {
        ArrayList<CourseOffering> all = new ArrayList<>();
        for (CourseSchedule cs : schedules) all.addAll(cs.getOfferings());
        return all;
    }

    public ArrayList<CourseOffering> getOfferingsByFaculty(FacultyRole f) {
        ArrayList<CourseOffering> r = new ArrayList<>();
        for (CourseSchedule cs : schedules) r.addAll(cs.getOfferingsByFaculty(f));
        return r;
    }

    /** Admin: registers a person with the given role. Duplicate emails are rejected and a unique ID is generated. */
    public Person registerPerson(RoleType type, String personName, String email, String phone) {
        Validator.requireNonNull(type, "Role");
        Person p = personDirectory.newPerson(personName, email, phone, name);
        switch (type) {
            case STUDENT: studentDirectory.newStudentRole(p); break;
            case FACULTY: facultyDirectory.newFacultyRole(p); break;
            case REGISTRAR: registrarDirectory.newRegistrarRole(p); break;
            default: adminDirectory.newAdminRole(p); break;
        }
        return p;
    }

    /**
     * Admin: deletes a person. A student's enrollments are dropped (seats freed, tuition cancelled or refunded).
     * A faculty member who still teaches an offering cannot be deleted until the offering is reassigned.
     * Remember to also call AccessControl.removeAccountsFor(person).
     */
    public void deletePerson(Person p) {
        Validator.requireNonNull(p, "Person");
        FacultyRole f = p.getRole(FacultyRole.class);
        if (f != null && !getOfferingsByFaculty(f).isEmpty()) {
            throw new IllegalStateException("Reassign this faculty member's courses before deleting.");
        }
        StudentRole s = p.getRole(StudentRole.class);
        if (s != null) {
            for (CourseLoad cl : new ArrayList<>(s.getTranscript().getCourseLoads())) {
                for (SeatAssignment sa : new ArrayList<>(cl.getSeatAssignments())) s.drop(sa);
            }
            studentDirectory.removeStudent(s);
        }
        if (f != null) facultyDirectory.removeFaculty(f);
        RegistrarRole r = p.getRole(RegistrarRole.class);
        if (r != null) registrarDirectory.removeRegistrar(r);
        AdminRole a = p.getRole(AdminRole.class);
        if (a != null) adminDirectory.removeAdmin(a);
        personDirectory.remove(p);
    }

    public String getName() { return name; }
    public CourseCatalog getCourseCatalog() { return courseCatalog; }
    public PersonDirectory getPersonDirectory() { return personDirectory; }
    public StudentDirectory getStudentDirectory() { return studentDirectory; }
    public FacultyDirectory getFacultyDirectory() { return facultyDirectory; }
    public RegistrarDirectory getRegistrarDirectory() { return registrarDirectory; }
    public AdminDirectory getAdminDirectory() { return adminDirectory; }
    public ArrayList<Degree> getDegrees() { return degrees; }
    public ArrayList<CourseSchedule> getCourseSchedules() { return schedules; }
    @Override public String toString() { return name; }
}
