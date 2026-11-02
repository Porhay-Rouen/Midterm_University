package model.person;

import model.util.Validation;

/**
 * University registrar. Keeps office hours (contact info lives on Person).
 *
 * @author Porhay Rouen
 */
public class RegistrarRole extends Role {

    private String officeHours = "";

    public RegistrarRole(Person person) {
        super(person);
    }

    public String getOfficeHours() {
        return officeHours;
    }

    public void setOfficeHours(String officeHours) {
        this.officeHours = Validation.optionalText(officeHours);
    }

    @Override
    public Type getType() {
        return Type.REGISTRAR;
    }
}
