/*
 * Registrar > Enrollment. Same layout as the student "Register for Courses" screen,
 * plus a student picker so the registrar can enroll or drop any student.
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
import model.course.SeatAssignment;
import model.course.Semester;
import model.person.Person;
import model.person.Role;
import model.person.StudentRole;
import model.record.CourseLoad;
import model.university.Department;

/**
 * Registrar > Enrollment. Enroll a student into an offering or drop them.
 * Uses the same model rules as the student page (8-credit limit, open seats, refunds).
 *
 * @author raene
 */
public class RegEnrollmentPanel extends javax.swing.JPanel {

    private AppContext ctx;
    private Session session;

    private final List<StudentRole> studentList = new ArrayList<>();
    private final List<Semester> semesterList = new ArrayList<>();
    private final List<CourseOffering> offerings = new ArrayList<>(); // left table rows
    private final List<SeatAssignment> mySeats = new ArrayList<>();   // right table rows
    private boolean loading = false;

    public RegEnrollmentPanel() {
        initComponents();
    }

    public RegEnrollmentPanel(AppContext ctx, Session session) {
        this();
        session.requireRole(Role.Type.REGISTRAR);
        this.ctx = ctx;
        this.session = session;

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
            }
        });
        refresh();
    }

    /** Refills the student and semester combo boxes, keeping what was picked. */
    public void refresh() {
        loading = true;
        int studentIndex = cmbStudent.getSelectedIndex();
        int semIndex = cmbSemester.getSelectedIndex();

        cmbStudent.removeAllItems();
        studentList.clear();
        for (Person p : ctx.getUniversity().getPersons(Role.Type.STUDENT)) {
            StudentRole s = p.getRole(StudentRole.class);
            if (s != null) {
                studentList.add(s);
                cmbStudent.addItem(p.getFullName() + " (" + p.getUniversityId() + ")");
            }
        }
        cmbSemester.removeAllItems();
        semesterList.clear();
        for (Semester s : ctx.getUniversity().getSemesters()) {
            semesterList.add(s);
            cmbSemester.addItem(s.getName());
        }
        if (studentIndex >= 0 && studentIndex < cmbStudent.getItemCount()) {
            cmbStudent.setSelectedIndex(studentIndex);
        }
        if (semIndex >= 0 && semIndex < cmbSemester.getItemCount()) {
            cmbSemester.setSelectedIndex(semIndex);
        }
        loading = false;
        loadTables();
    }

    private StudentRole selectedStudent() {
        int i = cmbStudent.getSelectedIndex();
        return (i < 0 || i >= studentList.size()) ? null : studentList.get(i);
    }

    private Semester selectedSemester() {
        int i = cmbSemester.getSelectedIndex();
        return (i < 0 || i >= semesterList.size()) ? null : semesterList.get(i);
    }

    private void loadTables() {
        DefaultTableModel left = (DefaultTableModel) tblOfferings.getModel();
        DefaultTableModel right = (DefaultTableModel) tblMyCourses.getModel();
        left.setRowCount(0);
        right.setRowCount(0);
        offerings.clear();
        mySeats.clear();

        Semester sem = selectedSemester();
        StudentRole student = selectedStudent();
        if (sem == null) {
            lblCredits.setText("No semester selected.");
            return;
        }
        // Left: every offering this semester
        for (Department d : ctx.getUniversity().getAllDepartments()) {
            CourseSchedule schedule = d.findSchedule(sem);
            if (schedule == null) {
                continue;
            }
            for (CourseOffering o : schedule.getOfferings()) {
                offerings.add(o);
                left.addRow(new Object[] {o.getCourse().getCourseId(), o.getTitle(),
                    o.getFacultyName(), o.getCourse().getCredits(), o.getOpenSeats()});
            }
        }
        if (student == null) {
            lblCredits.setText("Choose a student.");
            return;
        }
        // Right: this student's courses this semester
        CourseLoad load = student.getTranscript().findCourseLoad(sem);
        int credits = 0;
        if (load != null) {
            for (SeatAssignment sa : load.getSeatAssignments()) {
                mySeats.add(sa);
                right.addRow(new Object[] {sa.getCourse().getCourseId(), sa.getCourseOffering().getTitle(),
                    sa.getCredits(), String.format("$%,.2f", sa.getTuition()), sa.isPaid() ? "Yes" : "No"});
            }
            credits = load.getTotalCredits();
        }
        lblCredits.setText("Total credits: " + credits + " / " + CourseLoad.MAX_CREDITS
                + "      Balance: " + String.format("$%,.2f", student.getBalance()));
    }

    private void enroll() {
        StudentRole student = selectedStudent();
        int row = tblOfferings.getSelectedRow();
        if (student == null) {
            error("Choose a student first.");
            return;
        }
        if (row < 0 || row >= offerings.size()) {
            error("Select an offering in the left table first.");
            return;
        }
        CourseOffering o = offerings.get(row);
        try {
            student.enroll(o); // model checks: 8 credits, open seats, already enrolled
            loadTables();
            JOptionPane.showMessageDialog(this, "Enrolled in " + o.getCourse().getCourseId() + ".");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            error(ex.getMessage());
        }
    }

    private void drop() {
        StudentRole student = selectedStudent();
        int row = tblMyCourses.getSelectedRow();
        if (student == null) {
            error("Choose a student first.");
            return;
        }
        if (row < 0 || row >= mySeats.size()) {
            error("Select a course in the right table first.");
            return;
        }
        CourseOffering o = mySeats.get(row).getCourseOffering();
        int answer = JOptionPane.showConfirmDialog(this, "Drop " + o.getCourse().getCourseId()
                + "? Paid tuition will be refunded.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            student.drop(o); // model adds a refund if it was paid
            loadTables();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            error(ex.getMessage());
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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
        lblStudent = new javax.swing.JLabel();
        cmbStudent = new javax.swing.JComboBox<>();
        lblSemester = new javax.swing.JLabel();
        cmbSemester = new javax.swing.JComboBox<>();
        pnlCenter = new javax.swing.JPanel();
        pnlAvailable = new javax.swing.JPanel();
        lblAvailable = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblOfferings = new javax.swing.JTable();
        pnlButtons = new javax.swing.JPanel();
        btnEnroll = new javax.swing.JButton();
        btnDrop = new javax.swing.JButton();
        pnlLoad = new javax.swing.JPanel();
        lblLoad = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblMyCourses = new javax.swing.JTable();
        lblCredits = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setLayout(new java.awt.BorderLayout(0, 8));

        pnlTop.setOpaque(false);
        pnlTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblStudent.setText("Student");
        pnlTop.add(lblStudent);

        cmbStudent.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        cmbStudent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbStudentActionPerformed(evt);
            }
        });
        pnlTop.add(cmbStudent);

        lblSemester.setText("   Semester");
        pnlTop.add(lblSemester);

        cmbSemester.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        cmbSemester.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSemesterActionPerformed(evt);
            }
        });
        pnlTop.add(cmbSemester);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        pnlCenter.setOpaque(false);
        pnlCenter.setLayout(new java.awt.GridLayout(2, 1, 0, 12));

        pnlAvailable.setOpaque(false);
        pnlAvailable.setLayout(new java.awt.BorderLayout(0, 6));

        lblAvailable.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblAvailable.setText("Available offerings");
        pnlAvailable.add(lblAvailable, java.awt.BorderLayout.NORTH);

        tblOfferings.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Course ID", "Title", "Faculty", "Credits", "Open Seats"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblOfferings.setFillsViewportHeight(true);
        tblOfferings.setRowHeight(24);
        jScrollPane1.setViewportView(tblOfferings);

        pnlAvailable.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pnlButtons.setOpaque(false);
        pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnEnroll.setBackground(new java.awt.Color(37, 99, 235));
        btnEnroll.setForeground(new java.awt.Color(255, 255, 255));
        btnEnroll.setText("Enroll");
        btnEnroll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnrollActionPerformed(evt);
            }
        });
        pnlButtons.add(btnEnroll);

        btnDrop.setBackground(new java.awt.Color(220, 38, 38));
        btnDrop.setForeground(new java.awt.Color(255, 255, 255));
        btnDrop.setText("Drop");
        btnDrop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDropActionPerformed(evt);
            }
        });
        pnlButtons.add(btnDrop);

        pnlAvailable.add(pnlButtons, java.awt.BorderLayout.SOUTH);

        pnlCenter.add(pnlAvailable);

        pnlLoad.setOpaque(false);
        pnlLoad.setLayout(new java.awt.BorderLayout(0, 6));

        lblLoad.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLoad.setText("Student's course load");
        pnlLoad.add(lblLoad, java.awt.BorderLayout.NORTH);

        tblMyCourses.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Course ID", "Title", "Credits", "Tuition", "Paid"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMyCourses.setFillsViewportHeight(true);
        tblMyCourses.setRowHeight(24);
        jScrollPane2.setViewportView(tblMyCourses);

        pnlLoad.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        lblCredits.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblCredits.setText("Total credits: 0 / 8");
        pnlLoad.add(lblCredits, java.awt.BorderLayout.SOUTH);

        pnlCenter.add(pnlLoad);

        add(pnlCenter, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbStudentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStudentActionPerformed
        if (!loading) { loadTables(); }
    }//GEN-LAST:event_cmbStudentActionPerformed

    private void cmbSemesterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSemesterActionPerformed
        if (!loading) { loadTables(); }
    }//GEN-LAST:event_cmbSemesterActionPerformed

    private void btnEnrollActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnrollActionPerformed
        enroll();
    }//GEN-LAST:event_btnEnrollActionPerformed

    private void btnDropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDropActionPerformed
        drop();
    }//GEN-LAST:event_btnDropActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDrop;
    private javax.swing.JButton btnEnroll;
    private javax.swing.JComboBox<String> cmbSemester;
    private javax.swing.JComboBox<String> cmbStudent;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAvailable;
    private javax.swing.JLabel lblCredits;
    private javax.swing.JLabel lblLoad;
    private javax.swing.JLabel lblSemester;
    private javax.swing.JLabel lblStudent;
    private javax.swing.JPanel pnlAvailable;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlCenter;
    private javax.swing.JPanel pnlLoad;
    private javax.swing.JPanel pnlTop;
    private javax.swing.JTable tblMyCourses;
    private javax.swing.JTable tblOfferings;
    // End of variables declaration//GEN-END:variables
}
