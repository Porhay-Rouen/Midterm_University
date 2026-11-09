/*
 * registrar offerings page. this is where the registrar adds courses for a semester,
 * picks the teacher, sets how many seats, and changes the room or time.
 * made from my page template so it looks like the student register page.
 */
package ui.registrar;

import access.Session;
import app.AppContext;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.course.CourseOffering;
import model.course.CourseSchedule;
import model.course.Semester;
import model.person.FacultyRole;
import model.person.Person;
import model.person.Role;
import model.university.Course;
import model.university.Department;

/**
 * registrar offerings page
 *
 * @author raene
 */
public class RegOfferingsPanel extends javax.swing.JPanel {

    private AppContext ctx;
    private Session session;

    // these lists match the dropdowns and the table rows, same order
    private final List<Semester> semesterList = new ArrayList<>();
    private final List<Course> courseList = new ArrayList<>();
    private final List<FacultyRole> facultyList = new ArrayList<>(); // the first one is "unassigned"
    private final List<CourseOffering> shown = new ArrayList<>();
    private boolean loading = false; // so the semester dropdown doesn't reload the table while i'm still filling it

    public RegOfferingsPanel() {
        initComponents();
    }

    public RegOfferingsPanel(AppContext ctx, Session session) {
        this();
        session.requireRole(Role.Type.REGISTRAR);
        this.ctx = ctx;
        this.session = session;

        // when you click a row it fills in the form at the bottom
        tblOfferings.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });
        // reload everything each time you open this page
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
            }
        });
        refresh();
    }

    // fills the dropdowns and the table with the latest info
    public void refresh() {
        loading = true;
        int semIndex = cmbSemester.getSelectedIndex();

        cmbSemester.removeAllItems();
        semesterList.clear();
        for (Semester s : ctx.getUniversity().getSemesters()) {
            semesterList.add(s);
            cmbSemester.addItem(s.getName());
        }
        if (semIndex >= 0 && semIndex < cmbSemester.getItemCount()) {
            cmbSemester.setSelectedIndex(semIndex);
        }

        cmbCourse.removeAllItems();
        courseList.clear();
        for (Department d : ctx.getUniversity().getAllDepartments()) {
            for (Course c : d.getCourseCatalog().getCourses()) {
                courseList.add(c);
                cmbCourse.addItem(c.getCourseId() + " - " + c.getName());
            }
        }

        cmbFaculty.removeAllItems();
        facultyList.clear();
        cmbFaculty.addItem("Unassigned");
        facultyList.add(null);
        for (Person p : ctx.getUniversity().getPersons(Role.Type.FACULTY)) {
            FacultyRole f = p.getRole(FacultyRole.class);
            if (f != null) {
                facultyList.add(f);
                cmbFaculty.addItem(p.getFullName());
            }
        }
        loading = false;
        loadTable();
    }

    // the semester picked in the dropdown
    private Semester selectedSemester() {
        int i = cmbSemester.getSelectedIndex();
        return (i < 0 || i >= semesterList.size()) ? null : semesterList.get(i);
    }

    // shows all the courses for the semester you picked
    private void loadTable() {
        List<CourseOffering> list = new ArrayList<>();
        Semester sem = selectedSemester();
        if (sem != null) {
            for (Department d : ctx.getUniversity().getAllDepartments()) {
                CourseSchedule schedule = d.findSchedule(sem);
                if (schedule != null) {
                    list.addAll(schedule.getOfferings());
                }
            }
        }
        showRows(list);
    }

    // clears the table and puts these courses in it
    private void showRows(List<CourseOffering> list) {
        DefaultTableModel model = (DefaultTableModel) tblOfferings.getModel();
        model.setRowCount(0);
        shown.clear();
        for (CourseOffering o : list) {
            shown.add(o);
            model.addRow(new Object[] {o.getCourse().getCourseId(), o.getTitle(), o.getFacultyName(),
                o.getRoom(), o.getMeetingTime(), o.getCapacity(), o.getEnrolledCount()});
        }
    }

    // search by course number, like 6215 finds INFO6215
    private void find() {
        String text = txtFind.getText().trim();
        Semester sem = selectedSemester();
        if (text.isEmpty()) {
            error("Type a course number to search, e.g. INFO6215.");
            return;
        }
        if (sem == null) {
            error("Please choose a semester first.");
            return;
        }
        List<CourseOffering> found = new ArrayList<>();
        for (Department d : ctx.getUniversity().getAllDepartments()) {
            CourseSchedule schedule = d.findSchedule(sem);
            if (schedule != null) {
                found.addAll(schedule.searchByCourseId(text));
            }
        }
        showRows(found);
        if (found.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No offering matches \"" + text + "\".");
        }
    }

    // shows a red error popup
    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // the course in the row you clicked (or nothing)
    private CourseOffering selectedOffering() {
        int row = tblOfferings.getSelectedRow();
        return (row < 0 || row >= shown.size()) ? null : shown.get(row);
    }

    // the teacher picked in the dropdown
    private FacultyRole selectedFaculty() {
        int i = cmbFaculty.getSelectedIndex();
        return (i < 0 || i >= facultyList.size()) ? null : facultyList.get(i);
    }

    // puts the clicked row into the form so you can change it
    private void fillFormFromSelectedRow() {
        CourseOffering o = selectedOffering();
        if (o == null) {
            return;
        }
        cmbCourse.setSelectedIndex(courseList.indexOf(o.getCourse()));
        int f = facultyList.indexOf(o.getFaculty());
        cmbFaculty.setSelectedIndex(f < 0 ? 0 : f);
        spnCapacity.setValue(o.getCapacity());
        txtRoom.setText(o.getRoom());
        txtTime.setText(o.getMeetingTime());
    }

    // empties the form
    private void clearForm() {
        tblOfferings.clearSelection();
        if (cmbCourse.getItemCount() > 0) {
            cmbCourse.setSelectedIndex(0);
        }
        if (cmbFaculty.getItemCount() > 0) {
            cmbFaculty.setSelectedIndex(0);
        }
        spnCapacity.setValue(30);
        txtRoom.setText("");
        txtTime.setText("");
    }

    // checks the form, gives back a message if something is missing
    private String checkForm() {
        if (selectedSemester() == null) {
            return "Please choose a semester.";
        }
        if (txtRoom.getText().trim().isEmpty()) {
            return "Room is required (e.g. R101).";
        }
        if (txtTime.getText().trim().isEmpty()) {
            return "Meeting time is required (e.g. Mon 6-9 PM).";
        }
        return null;
    }

    // create offering button
    private void createOffering() {
        String problem = checkForm();
        int c = cmbCourse.getSelectedIndex();
        if (problem == null && (c < 0 || c >= courseList.size())) {
            problem = "Please choose a course.";
        }
        if (problem != null) {
            error(problem);
            return;
        }
        Course course = courseList.get(c);
        Semester sem = selectedSemester();
        try {
            Department dept = course.getDepartment();
            CourseSchedule schedule = dept.findSchedule(sem);
            if (schedule == null) {
                schedule = dept.newCourseSchedule(sem); // make a schedule if this semester doesn't have one yet
            }
            schedule.newOffering(course, selectedFaculty(), (Integer) spnCapacity.getValue(),
                    txtRoom.getText().trim(), txtTime.getText().trim());
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this, course.getCourseId() + " is now offered in " + sem + ".");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            error(ex.getMessage()); // shows what went wrong, like the course is already offered
        }
    }

    // update button, saves the changes to the course you clicked
    private void updateOffering() {
        CourseOffering o = selectedOffering();
        if (o == null) {
            error("Select an offering in the table first.");
            return;
        }
        String problem = checkForm();
        if (problem != null) {
            error(problem);
            return;
        }
        try {
            o.setCapacity((Integer) spnCapacity.getValue()); // can't go lower than how many students are already in it
            o.assignFaculty(selectedFaculty());
            o.setRoom(txtRoom.getText());
            o.setMeetingTime(txtTime.getText());
            loadTable();
            JOptionPane.showMessageDialog(this, o.getCourse().getCourseId() + " updated.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            error(ex.getMessage());
        }
    }

    // delete button
    private void deleteOffering() {
        CourseOffering o = selectedOffering();
        if (o == null) {
            error("Select an offering in the table first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete " + o.getCourse().getCourseId() + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            o.getSchedule().removeOffering(o); // won't let you delete it if students are in it
            loadTable();
            clearForm();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            error(ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlTop = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbSemester = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtFind = new javax.swing.JTextField();
        btnFind = new javax.swing.JButton();
        btnShowAll = new javax.swing.JButton();
        scrMain = new javax.swing.JScrollPane();
        tblOfferings = new javax.swing.JTable();
        pnlBottom = new javax.swing.JPanel();
        pnlRow1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        cmbCourse = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        cmbFaculty = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        spnCapacity = new javax.swing.JSpinner();
        pnlRow2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtRoom = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtTime = new javax.swing.JTextField();
        pnlButtons = new javax.swing.JPanel();
        btnCreate = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();

        setBackground(java.awt.Color.white);
        setLayout(new java.awt.BorderLayout());

        pnlTop.setBackground(java.awt.Color.white);
        pnlTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Semester:");
        pnlTop.add(jLabel1);

        cmbSemester.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        cmbSemester.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSemesterActionPerformed(evt);
            }
        });
        pnlTop.add(cmbSemester);

        jLabel2.setText("Course no.:");
        pnlTop.add(jLabel2);

        txtFind.setColumns(10);
        pnlTop.add(txtFind);

        btnFind.setText("Find");
        btnFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindActionPerformed(evt);
            }
        });
        pnlTop.add(btnFind);

        btnShowAll.setText("Show All");
        btnShowAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAllActionPerformed(evt);
            }
        });
        pnlTop.add(btnShowAll);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        scrMain.setBackground(java.awt.Color.white);

        tblOfferings.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Course ID", "Title", "Faculty", "Room", "Time", "Capacity", "Enrolled"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblOfferings.setFillsViewportHeight(true);
        scrMain.setViewportView(tblOfferings);

        add(scrMain, java.awt.BorderLayout.CENTER);

        pnlBottom.setBackground(java.awt.Color.white);
        pnlBottom.setLayout(new java.awt.GridLayout(3, 1));

        pnlRow1.setBackground(java.awt.Color.white);
        pnlRow1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel3.setText("Course:");
        pnlRow1.add(jLabel3);

        cmbCourse.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        pnlRow1.add(cmbCourse);

        jLabel4.setText("Faculty:");
        pnlRow1.add(jLabel4);

        cmbFaculty.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        pnlRow1.add(cmbFaculty);

        jLabel5.setText("Capacity:");
        pnlRow1.add(jLabel5);

        spnCapacity.setModel(new javax.swing.SpinnerNumberModel(30, 1, 500, 1));
        pnlRow1.add(spnCapacity);

        pnlBottom.add(pnlRow1);

        pnlRow2.setBackground(java.awt.Color.white);
        pnlRow2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel6.setText("Room:");
        pnlRow2.add(jLabel6);

        txtRoom.setColumns(8);
        pnlRow2.add(txtRoom);

        jLabel7.setText("Time:");
        pnlRow2.add(jLabel7);

        txtTime.setColumns(12);
        pnlRow2.add(txtTime);

        pnlBottom.add(pnlRow2);

        pnlButtons.setBackground(java.awt.Color.white);
        pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnCreate.setText("Create Offering");
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });
        pnlButtons.add(btnCreate);

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        pnlButtons.add(btnUpdate);

        btnDelete.setText("Delete Offering");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        pnlButtons.add(btnDelete);

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        pnlButtons.add(btnClear);

        pnlBottom.add(pnlButtons);

        add(pnlBottom, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbSemesterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSemesterActionPerformed
        // TODO add your handling code here:
        if (!loading) { loadTable(); }
    }//GEN-LAST:event_cmbSemesterActionPerformed

    private void btnFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindActionPerformed
        // TODO add your handling code here:
        find();
    }//GEN-LAST:event_btnFindActionPerformed

    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        // TODO add your handling code here:
        txtFind.setText(""); loadTable();
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        // TODO add your handling code here:
        createOffering();
    }//GEN-LAST:event_btnCreateActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        updateOffering();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        deleteOffering();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnCreate;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnFind;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbCourse;
    private javax.swing.JComboBox<String> cmbFaculty;
    private javax.swing.JComboBox<String> cmbSemester;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlRow1;
    private javax.swing.JPanel pnlRow2;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JScrollPane scrMain;
    private javax.swing.JSpinner spnCapacity;
    private javax.swing.JTable tblOfferings;
    private javax.swing.JTextField txtFind;
    private javax.swing.JTextField txtRoom;
    private javax.swing.JTextField txtTime;
    // End of variables declaration//GEN-END:variables
}
