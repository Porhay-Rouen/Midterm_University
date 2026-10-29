package model;

import java.util.ArrayList;

/** All Registrar roles of a department. @author (your name) */
public class RegistrarDirectory {
    private ArrayList<RegistrarRole> list = new ArrayList<>();

    public RegistrarRole newRegistrarRole(Person person) {
        RegistrarRole r = new RegistrarRole(person);
        list.add(r);
        return r;
    }

    public void removeRegistrar(RegistrarRole r) { list.remove(r); }
    public ArrayList<RegistrarRole> getRegistrars() { return list; }
}
