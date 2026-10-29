package model;

import java.util.ArrayList;

/** A real person. A person can hold roles (student, faculty, admin, registrar). @author (your name) */
public class Person {
    private String personId;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String status = "Active";
    private ArrayList<Role> roles = new ArrayList<>();

    public Person(String personId, String name, String email, String phone, String department) {
        this.personId = Validator.requireText(personId, "University ID");
        this.name = Validator.requireText(name, "Name");
        this.email = Validator.requireEmail(email);
        this.phone = phone == null ? "" : phone.trim();
        this.department = department == null ? "" : department.trim();
    }

    void addRole(Role r) { roles.add(r); }

    /** Returns this person's role of the given type, or null. Example: getRole(StudentRole.class) */
    public <T extends Role> T getRole(Class<T> type) {
        for (Role r : roles) if (type.isInstance(r)) return type.cast(r);
        return null;
    }

    public String getPersonId() { return personId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Validator.requireText(name, "Name"); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = Validator.requireEmail(email); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone == null ? "" : phone.trim(); }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department == null ? "" : department.trim(); }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        String s = Validator.requireText(status, "Status");
        if (!s.equals("Active") && !s.equals("Inactive") && !s.equals("Graduated")) {
            throw new IllegalArgumentException("Status must be Active, Inactive or Graduated.");
        }
        this.status = s;
    }
    public boolean isActive() { return "Active".equals(status); }
    public ArrayList<Role> getRoles() { return roles; }
    @Override public String toString() { return name; }
}
