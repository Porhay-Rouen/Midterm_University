package model.university;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.util.Validation;
import model.course.Semester;
import model.person.AdminRole;
import model.person.FacultyRole;
import model.person.Person;
import model.person.RegistrarRole;
import model.person.Role;
import model.person.StudentRole;

/**
 * Root of the model. Owns colleges, the semester list and the registry of
 * every Person (the "user accounts" the admin manages). Registration
 * blocks duplicate emails/IDs and auto-generates the university ID
 * (S001, F001, A001, R001 ...).
 *
 * Login credentials are handled by the provided Access Control package,
 * not by this model.
 *
 * @author Porhay Rouen
 */
public class University {

    private final String name;
    private final List<College> colleges = new ArrayList<>();
    private final List<Semester> semesters = new ArrayList<>();
    private final List<Person> persons = new ArrayList<>();
    private final Map<String, Integer> idCounters = new HashMap<>();

    public University(String name) {
        this.name = Validation.requireText(name, "University name");
    }

    public String getName() {
        return name;
    }

    // ---- structure ----

    public College newCollege(String collegeName) {
        College college = new College(collegeName);
        colleges.add(college);
        return college;
    }

    public List<College> getColleges() {
        return Collections.unmodifiableList(colleges);
    }

    public List<Department> getAllDepartments() {
        List<Department> all = new ArrayList<>();
        for (College c : colleges) {
            all.addAll(c.getDepartments());
        }
        return all;
    }

    public Department findDepartment(String departmentName) {
        for (Department d : getAllDepartments()) {
            if (d.getName().equalsIgnoreCase(Validation.optionalText(departmentName))) {
                return d;
            }
        }
        return null;
    }

    public Semester newSemester(String semesterName, LocalDate start, LocalDate end) {
        if (findSemester(semesterName) != null) {
            throw new IllegalArgumentException("Semester " + semesterName + " already exists.");
        }
        Semester semester = new Semester(semesterName, start, end);
        semesters.add(semester);
        return semester;
    }

    public Semester findSemester(String semesterName) {
        for (Semester s : semesters) {
            if (s.getName().equalsIgnoreCase(Validation.optionalText(semesterName))) {
                return s;
            }
        }
        return null;
    }

    /** Semesters sorted oldest to newest. */
    public List<Semester> getSemesters() {
        List<Semester> sorted = new ArrayList<>(semesters);
        Collections.sort(sorted);
        return sorted;
    }

    // ---- registration (blocks duplicates, auto-generates ID) ----

    public boolean isEmailTaken(String email) {
        return findPersonByEmail(email) != null;
    }

    public boolean isIdTaken(String universityId) {
        return findPersonById(universityId) != null;
    }

    /** Next free ID for the role type, for example S001, S002 ... */
    public String generateUniversityId(Role.Type type) {
        Validation.requireNonNull(type, "Role type");
        String prefix = type.getIdPrefix();
        int next = idCounters.getOrDefault(prefix, 0);
        String id;
        do {
            next++;
            id = String.format("%s%03d", prefix, next);
        } while (isIdTaken(id));
        idCounters.put(prefix, next);
        return id;
    }

    /** Validates the details, blocks duplicate email, generates the ID and registers the person. */
    private Person registerPerson(String firstName, String lastName, String email, String phone, Role.Type type) {
        String cleanEmail = Validation.requireEmail(email);
        if (isEmailTaken(cleanEmail)) {
            throw new IllegalArgumentException("A person with email " + cleanEmail + " is already registered.");
        }
        Person person = new Person(generateUniversityId(type), firstName, lastName, cleanEmail, phone);
        persons.add(person);
        return person;
    }

    /** Registers a new student; rolls back the person if the role cannot be created. */
    public StudentRole registerStudent(String firstName, String lastName, String email, String phone,
            Department department, Degree degree) {
        Validation.requireNonNull(department, "Department");
        Person person = registerPerson(firstName, lastName, email, phone, Role.Type.STUDENT);
        try {
            return department.getStudentDirectory().newStudentRole(person, degree);
        } catch (RuntimeException e) {
            persons.remove(person);
            throw e;
        }
    }

    public FacultyRole registerFaculty(String firstName, String lastName, String email, String phone,
            Department department) {
        Validation.requireNonNull(department, "Department");
        Person person = registerPerson(firstName, lastName, email, phone, Role.Type.FACULTY);
        try {
            return department.getFacultyDirectory().newFacultyRole(person);
        } catch (RuntimeException e) {
            persons.remove(person);
            throw e;
        }
    }

    public AdminRole registerAdmin(String firstName, String lastName, String email, String phone) {
        Person person = registerPerson(firstName, lastName, email, phone, Role.Type.ADMIN);
        try {
            return new AdminRole(person);
        } catch (RuntimeException e) {
            persons.remove(person);
            throw e;
        }
    }

    public RegistrarRole registerRegistrar(String firstName, String lastName, String email, String phone) {
        Person person = registerPerson(firstName, lastName, email, phone, Role.Type.REGISTRAR);
        try {
            return new RegistrarRole(person);
        } catch (RuntimeException e) {
            persons.remove(person);
            throw e;
        }
    }

    // ---- accounts and records ----

    public List<Person> getPersons() {
        return Collections.unmodifiableList(persons);
    }

    /** Everyone holding the role type (for the Students, Faculty and Registrars tables). */
    public List<Person> getPersons(Role.Type type) {
        List<Person> result = new ArrayList<>();
        for (Person p : persons) {
            if (p.hasRole(type)) {
                result.add(p);
            }
        }
        return result;
    }

    public Person findPersonById(String universityId) {
        String id = Validation.optionalText(universityId);
        for (Person p : persons) {
            if (p.getUniversityId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    public Person findPersonByEmail(String email) {
        String clean = Validation.optionalText(email);
        for (Person p : persons) {
            if (p.getEmail().equalsIgnoreCase(clean)) {
                return p;
            }
        }
        return null;
    }

    /** Changes an email while blocking duplicates (used by Records and Profile pages). */
    public void changeEmail(Person person, String newEmail) {
        Validation.requireNonNull(person, "Person");
        String clean = Validation.requireEmail(newEmail);
        Person owner = findPersonByEmail(clean);
        if (owner != null && owner != person) {
            throw new IllegalArgumentException("Email " + clean + " is already used by another person.");
        }
        person.setEmail(clean);
    }

    /** Deletes the account and detaches the person from directories. Faculty must have no offerings. */
    public void deletePerson(Person person) {
        Validation.requireNonNull(person, "Person");
        FacultyRole faculty = person.getRole(FacultyRole.class);
        if (faculty != null && !faculty.getOfferings().isEmpty()) {
            throw new IllegalStateException(person.getFullName()
                    + " still teaches courses. Reassign those offerings first.");
        }
        StudentRole student = person.getRole(StudentRole.class);
        if (student != null) {
            student.getDepartment().getStudentDirectory().removeStudent(student);
        }
        if (faculty != null) {
            faculty.getDepartment().getFacultyDirectory().removeFaculty(faculty);
        }
        persons.remove(person);
    }

    // ---- the 3 admin search methods ----

    public List<Person> searchByName(String keyword) {
        String key = Validation.optionalText(keyword).toLowerCase();
        List<Person> result = new ArrayList<>();
        for (Person p : persons) {
            if (p.getFullName().toLowerCase().contains(key)) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Person> searchById(String keyword) {
        String key = Validation.optionalText(keyword).toLowerCase();
        List<Person> result = new ArrayList<>();
        for (Person p : persons) {
            if (p.getUniversityId().toLowerCase().contains(key)) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Person> searchByDepartment(String keyword) {
        String key = Validation.optionalText(keyword).toLowerCase();
        List<Person> result = new ArrayList<>();
        for (Person p : persons) {
            for (Role r : p.getRoles()) {
                Department d = r.getDepartment();
                if (d != null && d.getName().toLowerCase().contains(key)) {
                    result.add(p);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
