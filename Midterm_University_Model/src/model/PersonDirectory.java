package model;

import java.util.ArrayList;

/** Every person of a department. Prevents duplicates and generates unique university IDs. @author (your name) */
public class PersonDirectory {
    private ArrayList<Person> persons = new ArrayList<>();
    private int nextNumber = 100001;

    /** Registers a new person. Throws IllegalArgumentException for missing fields or a duplicate email. */
    public Person newPerson(String name, String email, String phone, String department) {
        String cleanName = Validator.requireText(name, "Name");
        String cleanEmail = Validator.requireEmail(email);
        if (findByEmail(cleanEmail) != null) throw new IllegalArgumentException("A person with this email is already registered.");
        String id;
        do { id = "U" + nextNumber++; } while (findById(id) != null);
        Person p = new Person(id, cleanName, cleanEmail, phone, department);
        persons.add(p);
        return p;
    }

    /** Updates profile fields, keeping emails unique. */
    public void updateProfile(Person p, String name, String email, String phone) {
        Validator.requireNonNull(p, "Person");
        String cleanEmail = Validator.requireEmail(email);
        Person other = findByEmail(cleanEmail);
        if (other != null && other != p) throw new IllegalArgumentException("Another person already uses this email.");
        p.setName(name);
        p.setEmail(cleanEmail);
        p.setPhone(phone);
    }

    public Person findById(String id) {
        if (id == null) return null;
        for (Person p : persons) if (p.getPersonId().equalsIgnoreCase(id.trim())) return p;
        return null;
    }

    public Person findByEmail(String email) {
        if (email == null) return null;
        for (Person p : persons) if (p.getEmail().equalsIgnoreCase(email.trim())) return p;
        return null;
    }

    // ----- the three admin search methods (an empty keyword returns everyone) -----
    public ArrayList<Person> searchByName(String keyword) {
        String k = norm(keyword);
        ArrayList<Person> r = new ArrayList<>();
        for (Person p : persons) if (p.getName().toLowerCase().contains(k)) r.add(p);
        return r;
    }

    public ArrayList<Person> searchById(String keyword) {
        String k = norm(keyword);
        ArrayList<Person> r = new ArrayList<>();
        for (Person p : persons) if (p.getPersonId().toLowerCase().contains(k)) r.add(p);
        return r;
    }

    public ArrayList<Person> searchByDepartment(String keyword) {
        String k = norm(keyword);
        ArrayList<Person> r = new ArrayList<>();
        for (Person p : persons) if (p.getDepartment().toLowerCase().contains(k)) r.add(p);
        return r;
    }

    void remove(Person p) { persons.remove(p); }
    private String norm(String s) { return s == null ? "" : s.trim().toLowerCase(); }
    public ArrayList<Person> getPersons() { return persons; }
}
