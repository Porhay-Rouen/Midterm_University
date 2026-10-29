package model;

import java.util.ArrayList;

/** All Admin roles of a department. @author (your name) */
public class AdminDirectory {
    private ArrayList<AdminRole> list = new ArrayList<>();

    public AdminRole newAdminRole(Person person) {
        AdminRole r = new AdminRole(person);
        list.add(r);
        return r;
    }

    public void removeAdmin(AdminRole r) { list.remove(r); }
    public ArrayList<AdminRole> getAdmins() { return list; }
}
