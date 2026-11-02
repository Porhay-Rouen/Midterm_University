package model.person;

/**
 * University administrator. Admin features (accounts, records, dashboard)
 * work through University and UniversityReports.
 *
 * @author Porhay Rouen
 */
public class AdminRole extends Role {

    public AdminRole(Person person) {
        super(person);
    }

    @Override
    public Type getType() {
        return Type.ADMIN;
    }
}
