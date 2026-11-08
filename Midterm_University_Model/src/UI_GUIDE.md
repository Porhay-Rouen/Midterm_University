# UI Guide: Role Home Screens

How to build your role's home screen so every role looks and works the same way.
Pattern from Rae's `RegistrarHomePanel`; Mac fixes and this guide by Brendan Gilbert.
*Drafted with AI assistance (Claude); reviewed and tested by Brendan Gilbert on 11/5/2026.*

Working example: `ui/student/StudentHomePanel` (Student). Swap in your own role's names and color.

---

## 1. Structure

One home panel per role: a header on top, nav buttons on the left, and a content area that shows one page at a time.

```
<Role>HomePanel              (BorderLayout)
├── pnlHeader      → North   (lblTitle, lblUser, btnLogOut)
└── pnlBody        → Center  (BorderLayout)
    ├── pnlNavHolder → West  (BorderLayout)
    │   └── pnlNav   → North (GridLayout: rows = # of pages, columns = 1, vertical gap = 8)
    │       └── one JButton per page
    └── pnlContent   → Center (CardLayout: one page at a time)
```

Each page is its own JPanel Form in your package (e.g. `ui.faculty`) with the constructor `(AppContext ctx, Session session)`.

## 2. Build it in the Design tab

Use the **Navigator** pane (Window → Navigator). It is more reliable than dragging onto the canvas.

1. Right-click your package → **New → JPanel Form…** → name it `<Role>HomePanel`.
2. Navigator: right-click the top **[JPanel]** → **Set Layout → Border Layout**.
3. Right-click it → **Add From Palette → Swing Containers → Panel** (twice). Rename (right-click → **Change Variable Name…**) to `pnlHeader` and `pnlBody`. In Properties → **Layout → Direction**: `pnlHeader` = North, `pnlBody` = Center.
4. `pnlBody` → Set Layout → **Border Layout**. Add two panels inside it: `pnlNavHolder` (Direction = West, Set Layout → Border Layout) and `pnlContent` (Direction = Center, Set Layout → **Card Layout**).
5. Add one more panel **inside `pnlNavHolder`**: `pnlNav` (Direction = North, Set Layout → **Grid Layout**). Click the **GridLayout** item under it and set **Rows = number of pages, Columns = 1, Vertical Gap = 8**.
6. **Header:** drag a Label (`lblTitle`), a second Label (`lblUser`, text "Signed in as …") and a Button (`btnLogOut`, text "Log Out") onto `pnlHeader`. Set `pnlHeader` preferredSize to 400 × 50.
7. **Nav:** right-click `pnlNav` → Add From Palette → Swing Controls → **Button**, once per page. Rename them `btn<Page>` and set their text.

> **Why the extra `pnlNavHolder`?** The West side of a BorderLayout always runs full height, and GridLayout splits that height evenly, so 6 buttons would each be ~200 px tall. The holder fills the height; the grid sits at its top at normal size.

## 3. Style values (so all roles match)

| Element | Value |
|---|---|
| Header background | **your role's color** (below) |
| Header title `lblTitle` | Helvetica Neue, Bold, 13 · foreground white |
| `lblUser` | foreground white |
| `pnlBody`, `pnlNavHolder`, `pnlNav` background | white |
| Nav buttons | background **100, 116, 139** (gray) · foreground white · font Bold 12 |
| Active nav button | **37, 99, 235** (blue), set in code |
| Window size | 1100 × 700 (set in MainFrame, step 6) |

| Role | Header color (RGB) |
|---|---|
| Student | 102, 153, 255 |
| Faculty | 204, 0, 255 |
| Admin | 255, 0, 0 |
| Registrar | 0, 204, 102 |

How: select the component → Properties → **background** / **foreground** / **font** → **…** → RGB tab. Tip: ⌘-click (Ctrl-click on Windows) several buttons in the Navigator to set them all at once.

## 4. Mac fix: make button colors show

On macOS, the built-in button style ("Aqua") ignores your background color when a button is taller than normal and can draw it white, which hides white text. Select **all nav buttons** → Properties:
- **opaque** = checked
- **borderPainted** = unchecked

This gives flat, colored buttons on every computer.

## 5. Code (Source tab)

Imports (under your `package` line):
```java
import access.Session;
import app.AppContext;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JButton;
import model.person.Role;
import ui.main.PlaceholderPanel;
```

Fields + constructor (keep the empty `public <Role>HomePanel() { initComponents(); }`; the designer needs it):
```java
    private static final Color ACTIVE = new Color(37, 99, 235);     // blue: current page
    private static final Color INACTIVE = new Color(100, 116, 139); // gray: other pages
    private AppContext ctx;
    private Session session;

    public StudentHomePanel(AppContext ctx, Session session) {
        this();                                   // builds what you designed
        session.requireRole(Role.Type.STUDENT);   // only this role gets in
        this.ctx = ctx;
        this.session = session;
        lblUser.setText("Signed in as " + session.getPerson().getFullName());

        // One card per page, each with a name tag (PlaceholderPanel until the real page exists)
        pnlContent.add(new PlaceholderPanel("Register"), "Register");
        pnlContent.add(new PlaceholderPanel("Profile"), "Profile");
        // ...one line per page

        showPage("Register", btnRegister);        // starting page
    }

    /** Shows one page, updates the title, and highlights the active nav button. */
    private void showPage(String name, JButton activeButton) {
        ((CardLayout) pnlContent.getLayout()).show(pnlContent, name);
        lblTitle.setText("Student: " + name);
        for (Component c : pnlNav.getComponents()) {   // every nav button back to gray
            c.setBackground(INACTIVE);
        }
        activeButton.setBackground(ACTIVE);             // the current one turns blue
    }
```

Nav buttons: in the Design tab, **double-click** each nav button. NetBeans creates its click method; add one line:
```java
        showPage("Register", btnRegister);
```
The name tag must match the one used in `pnlContent.add(...)` letter for letter.

Log Out: double-click `btnLogOut` and add (same as Porhay's pages):
```java
        ctx.getAccessControl().logout(session);                  // ends the session
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();                                    // closes MainFrame
        }
        new ui.login.LoginFrame(ctx).setVisible(true);           // back to login
```

## 6. MainFrame: change only your role's case

In `ui/main/MainFrame.java` → `buildMenu()`, change **only your own `case`**:
```java
            case STUDENT:
                addPage("Student", new ui.student.StudentHomePanel(ctx, session));
                setSize(1100, 700);              // same window size for every role
                setLocationRelativeTo(null);     // re-center after resizing
                break;
```
Don't run Source → Format on MainFrame and don't touch other cases. Each of us edits a different case, so Git can merge everyone's changes without conflicts.

## 7. Checklist before your PR
- [ ] Runs; log in with your role; every nav button switches the page and turns blue
- [ ] Log Out returns to the login screen
- [ ] Commit only your files (not `nbproject/private/…` or `.DS_Store`)
- [ ] `@author <your name>` and a one-line description on every file you created
