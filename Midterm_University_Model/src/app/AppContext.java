package app;

import access.AccessControl;
import model.report.UniversityReports;
import model.university.University;

/**
 * One object that holds everything the screens share: the university model,
 * the access control layer and the reports. Create it once in Main (via
 * SampleData.load()) and pass it to LoginFrame and then MainFrame.
 *
 * @author Porhay Rouen
 */
public class AppContext {

    private final University university;
    private final AccessControl accessControl;
    private final UniversityReports reports;

    public AppContext(University university, AccessControl accessControl) {
        this.university = university;
        this.accessControl = accessControl;
        this.reports = new UniversityReports(university);
    }

    public University getUniversity() {
        return university;
    }

    public AccessControl getAccessControl() {
        return accessControl;
    }

    public UniversityReports getReports() {
        return reports;
    }
}
