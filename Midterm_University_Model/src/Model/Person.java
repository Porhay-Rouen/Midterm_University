/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.util.ArrayList;
import javax.management.relation.Role;

/**
 *
 * @author macbookpro
 */
public class Person {
    
    private String personId;
    private String name;
    private String email;
    private ArrayList<Role> roles = new ArrayList<>();

    public Person(String personId, String name, String email) {
        this.personId = personId;
        this.name = name;
        this.email = email;
    }

    /** Called by the Role constructor. */
    void addRole(Role r) { roles.add(r); }

    /** Returns this person's role of the given type, or null. Example: getRole(StudentRole.class) */
    public <T extends Role> T getRole(Class<T> type) {
        for (Role r : roles) {
            if (type.isInstance(r)) return type.cast(r);
        }
        return null;
    }

    public String getPersonId() { return personId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public ArrayList<Role> getRoles() { return roles; }
    @Override public String toString() { return name; }

    
}
