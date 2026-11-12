/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.faculty;

import access.Session;
import app.AppContext;

/**
 *
 * @author macbookpro
 */
public class MyCoursesPanel extends javax.swing.JPanel {
    
    private AppContext ctx;
    private Session session;
    private model.course.CourseOffering selected;   // offering currently shown in the form
    /**
     * Creates new form MyCoursesPanel
     */
    public MyCoursesPanel() {
        initComponents();
    }
    
    public MyCoursesPanel(AppContext ctx, Session session) {
    this();
    session.requireRole(model.person.Role.Type.FACULTY);
    this.ctx = ctx;
    this.session = session;
    setupTable();
    loadSemesters();
    cmbSemester.addActionListener(e -> loadCourses());
    btnSave.addActionListener(evt -> btnSaveActionPerformed(evt));
    btnUploadSyllabus.addActionListener(evt -> uploadSyllabus());
    btnToggleEnrollment.addActionListener(evt -> toggleEnrollment());
    tblCourses.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        fillForm();
       
    }
});
    }
    /** Lets faculty pick a syllabus file and stores its path on the selected course. */
private void uploadSyllabus() {
    if (selected == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "Select a course first.");
        
        return;
    }
    javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
    if (chooser.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
        return;
    }
    java.io.File file = chooser.getSelectedFile();
    if (file == null || !file.isFile()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Please choose a valid file.");
        return;
    }
    try {
        selected.setSyllabusPath(file.getAbsolutePath());
    } catch (IllegalArgumentException | IllegalStateException ex) {
        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        return;
    }
    updateSyllabusAndEnrollmentControls();
    javax.swing.JOptionPane.showMessageDialog(this, "Syllabus saved.");
}

/** Opens or closes enrollment for the selected course (only allowed before the semester starts). */
private void toggleEnrollment() {
    if (selected == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "Select a course first.");
        
        return;
    }
    if (!selected.canChangeEnrollment()) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "Enrollment can only be changed before the semester starts.");
        return;
    }
    selected.setEnrollmentOpen(!selected.isEnrollmentOpen());
    int row = tblCourses.getSelectedRow();
    loadCourses();
    tblCourses.setRowSelectionInterval(row, row);
    updateSyllabusAndEnrollmentControls();
}

/** Refreshes the syllabus label and the enrollment button text for the selected course. */
private void updateSyllabusAndEnrollmentControls() {
    if (selected == null) {
        lblSyllabus.setText("Syllabus: none");
        btnToggleEnrollment.setText("Close Enrollment");
        return;
    }
    String path = selected.getSyllabusPath();
    lblSyllabus.setText("Syllabus: " + (path == null || path.isEmpty()
            ? "none" : new java.io.File(path).getName()));
    btnToggleEnrollment.setText(selected.isEnrollmentOpen() ? "Close Enrollment" : "Open Enrollment");
}
    /** Finds the offering for the selected table row. */
private model.course.CourseOffering getSelectedOffering() {
    int row = tblCourses.getSelectedRow();
    int idx = cmbSemester.getSelectedIndex();
    if (row < 0 || idx < 0) {
        return null;
    }
    model.course.Semester sem = ctx.getUniversity().getSemesters().get(idx);
    model.person.FacultyRole me = (model.person.FacultyRole) session.getRole();
    return me.getOfferings(sem).get(row);
}

/** Copies the selected offering into the form fields. */
private void fillForm() {
    selected = getSelectedOffering();
    if (selected == null) {
        txtTitle.setText("");
        txtDescription.setText("");
        txtRoom.setText("");
        txtTime.setText("");
        txtCapacity.setText("");
        updateSyllabusAndEnrollmentControls();
        return;
    }
    txtTitle.setText(selected.getTitle());
    txtDescription.setText(selected.getDescription());
    txtRoom.setText(selected.getRoom());
    txtTime.setText(selected.getMeetingTime());
    txtCapacity.setText(String.valueOf(selected.getCapacity()));
    updateSyllabusAndEnrollmentControls();
}

    
    /** Sets up the course table columns (read-only cells). */
private void setupTable() {
    tblCourses.setModel(new javax.swing.table.DefaultTableModel(
            new Object[]{"Course ID", "Title", "Time", "Room", "Capacity", "Enrolled", "Enrollment"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    });
}

/** Fills the dropdown with all semesters, then loads the courses. */
private void loadSemesters() {
    cmbSemester.removeAllItems();
    for (model.course.Semester s : ctx.getUniversity().getSemesters()) {
        cmbSemester.addItem(s.toString());
    }
    loadCourses();
}

/** Shows this faculty member's offerings for the selected semester. */
private void loadCourses() {
    javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) tblCourses.getModel();
    m.setRowCount(0);
    int idx = cmbSemester.getSelectedIndex();
    if (idx < 0) {
        return;
    }
    model.course.Semester sem = ctx.getUniversity().getSemesters().get(idx);
    model.person.FacultyRole me = (model.person.FacultyRole) session.getRole();
    for (model.course.CourseOffering o : me.getOfferings(sem)) {
        m.addRow(new Object[]{o.getCourse().getCourseId(), o.getTitle(), o.getMeetingTime(),
            o.getRoom(), o.getCapacity(), o.getEnrolledCount(),
            o.isEnrollmentOpen() ? "Open" : "Closed"});
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

        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cmbSemester = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCourses = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtTitle = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextField();
        txtRoom = new javax.swing.JTextField();
        txtTime = new javax.swing.JTextField();
        txtCapacity = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        btnSave = new javax.swing.JButton();
        btnUploadSyllabus = new javax.swing.JButton();
        btnToggleEnrollment = new javax.swing.JButton();
        lblSyllabus = new javax.swing.JLabel();

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Semester");

        cmbSemester.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tblCourses.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblCourses);

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel1.setText("Title");

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel3.setText("Description");

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel4.setText("Room");

        jLabel5.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel5.setText("Meeting time");

        jLabel6.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel6.setText("Capacity");

        btnSave.setBackground(new java.awt.Color(204, 255, 255));
        btnSave.setText("Save Changes");

        btnUploadSyllabus.setText("Upload Syllabus...");

        btnToggleEnrollment.setText("Close Enrollment");

        lblSyllabus.setText("Syllabus : none");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel1))
                                .addGap(37, 37, 37)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                                    .addComponent(txtRoom, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtDescription, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtTitle, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCapacity)))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnUploadSyllabus, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnToggleEnrollment)
                        .addGap(18, 18, 18)
                        .addComponent(lblSyllabus)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbSemester, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 628, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 29, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbSemester, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(txtRoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(txtCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnUploadSyllabus)
                    .addComponent(btnToggleEnrollment)
                    .addComponent(lblSyllabus))
                .addGap(23, 23, 23))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
    if (selected == null) {
    javax.swing.JOptionPane.showMessageDialog(this, "Select a course first.");
    return;
}
String title = txtTitle.getText().trim();
String room = txtRoom.getText().trim();
String time = txtTime.getText().trim();
if (title.isEmpty() || room.isEmpty() || time.isEmpty()) {
    javax.swing.JOptionPane.showMessageDialog(this, "Title, room and meeting time are required.");
    return;
}
int capacity;
try {
    capacity = Integer.parseInt(txtCapacity.getText().trim());
} catch (NumberFormatException ex) {
    javax.swing.JOptionPane.showMessageDialog(this, "Capacity must be a whole number.");
    return;
}
if (capacity <= 0) {
    javax.swing.JOptionPane.showMessageDialog(this, "Capacity must be greater than 0.");
    return;
}
try {
    selected.setCapacity(capacity);   // may reject a value below the enrolled count
    selected.setTitle(title);
    selected.setDescription(txtDescription.getText().trim());
    selected.setRoom(room);
    selected.setMeetingTime(time);
} catch (IllegalArgumentException | IllegalStateException ex) {
    javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
    return;
}
int row = tblCourses.getSelectedRow();
loadCourses();
tblCourses.setRowSelectionInterval(row, row);
javax.swing.JOptionPane.showMessageDialog(this, "Course updated.");
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnToggleEnrollment;
    private javax.swing.JButton btnUploadSyllabus;
    private javax.swing.JComboBox<String> cmbSemester;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblSyllabus;
    private javax.swing.JTable tblCourses;
    private javax.swing.JTextField txtCapacity;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtRoom;
    private javax.swing.JTextField txtTime;
    private javax.swing.JTextField txtTitle;
    // End of variables declaration//GEN-END:variables
}


