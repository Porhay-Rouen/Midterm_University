package app;

import java.awt.EventQueue;
import ui.login.LoginFrame;

/**
 * Entry point of the Digital University System: loads the sample data and
 * opens the Login screen. Set this as the project's Main Class.
 *
 * @author Porhay Rouen
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        AppContext ctx = SampleData.load();
        EventQueue.invokeLater(() -> new LoginFrame(ctx).setVisible(true));
    }
}
