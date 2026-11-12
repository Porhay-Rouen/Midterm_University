/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.faculty;

/**
 *
 * @author muykhim
 */
public class GradingPanel extends javax.swing.JPanel {

    /**
     * Creates new form GradingPanel
     */
    private app.AppContext ctx;
    private access.Session session;
    private java.util.List<model.course.CourseOffering> offerings = new java.util.ArrayList<>();
    private java.util.List<model.course.SeatAssignment> seats = new java.util.ArrayList<>();

    public GradingPanel(app.AppContext ctx, access.Session session) {
        initComponents();
        session.requireRole(model.person.Role.Type.FACULTY);
        this.ctx = ctx;
        this.session = session;
        loadSemesters();
        cmbSemester.addActionListener(e -> loadCourses());
        cmbCourse.addActionListener(e -> loadGrades());
        btnAddAssignment.addActionListener(e -> addAssignment());
        btnRecordScore.addActionListener(e -> recordScore());
        btnComputeOne.addActionListener(e -> computeSelected());
        btnComputeAll.addActionListener(e -> computeAll());
        
    }
    
    /** Fills the semester dropdown, then loads the courses. */
private void loadSemesters() {
    cmbSemester.removeAllItems();
    for (model.course.Semester s : ctx.getUniversity().getSemesters()) {
        cmbSemester.addItem(s.toString());
    }
    loadCourses();
}

/** Lists this faculty member's courses for the selected semester. */
private void loadCourses() {
    cmbCourse.removeAllItems();
    offerings.clear();
    int idx = cmbSemester.getSelectedIndex();
    if (idx >= 0) {
        model.course.Semester sem = ctx.getUniversity().getSemesters().get(idx);
        model.person.FacultyRole me = (model.person.FacultyRole) session.getRole();
        offerings.addAll(me.getOfferings(sem));
        for (model.course.CourseOffering o : offerings) {
            cmbCourse.addItem(o.getCourse().getCourseId());
        }
    }
    loadGrades();
}

/** The offering chosen in the course dropdown, or null. */
private model.course.CourseOffering currentOffering() {
    int idx = cmbCourse.getSelectedIndex();
    return (idx < 0 || idx >= offerings.size()) ? null : offerings.get(idx);
}

/** Formats a score without a trailing ".0". */
private String fmt(double d) {
    return d == Math.rint(d) ? String.valueOf((int) d) : String.valueOf(d);
}

/** Rebuilds the table: rank, one column per assignment, total %, final grade. */
private void loadGrades() {
    seats.clear();
    model.course.CourseOffering o = currentOffering();
    java.util.List<String> cols = new java.util.ArrayList<>();
    cols.add("Rank");
    cols.add("Student");
    if (o != null) {
        for (model.course.Assignment a : o.getAssignments()) {
            cols.add(a.getTitle() + " (/" + fmt(a.getMaxScore()) + ")");
        }
    }
    cols.add("Total %");
    cols.add("Final Grade");
    javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(cols.toArray(), 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    if (o != null) {
        seats.addAll(o.getRankedSeatAssignments());   // highest % first
        int rank = 0;
        double previous = Double.NaN;
        for (int i = 0; i < seats.size(); i++) {
            model.course.SeatAssignment sa = seats.get(i);
            if (Double.compare(sa.getPercentage(), previous) != 0) {
                rank = i + 1;                          // ties share a rank: 1, 2, 2, 4
                previous = sa.getPercentage();
            }
            java.util.List<Object> row = new java.util.ArrayList<>();
            row.add(rank);
            row.add(sa.getStudent().getPerson().getFullName());
            for (model.course.Assignment a : o.getAssignments()) {
                Double s = sa.getScore(a);
                row.add(s == null ? "-" : fmt(s));
            }
            row.add(String.format("%.1f%%", sa.getPercentage()));
            row.add(sa.getGrade().getLabel());
            m.addRow(row.toArray());
        }
    }
    tblGrades.setModel(m);
    refreshAssignmentCombo();
    updateSummary();
}

/** Refills the assignment dropdown for the current course, keeping the choice if possible. */
private void refreshAssignmentCombo() {
    int keep = cmbAssignment.getSelectedIndex();
    cmbAssignment.removeAllItems();
    model.course.CourseOffering o = currentOffering();
    if (o == null) {
        return;
    }
    for (model.course.Assignment a : o.getAssignments()) {
        cmbAssignment.addItem(a.getTitle());
    }
    if (keep >= 0 && keep < cmbAssignment.getItemCount()) {
        cmbAssignment.setSelectedIndex(keep);
    }
}

/** Ranking table: every student by rank, plus a last row with the class GPA. */
private void updateSummary() {
    javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(
            new Object[]{"Rank", "Student", "Total %", "Final Grade"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    model.course.CourseOffering o = currentOffering();
    if (o != null) {
        int rank = 0;
        double previous = Double.NaN;
        for (int i = 0; i < seats.size(); i++) {
            model.course.SeatAssignment sa = seats.get(i);
            if (Double.compare(sa.getPercentage(), previous) != 0) {
                rank = i + 1;                       // ties share a rank
                previous = sa.getPercentage();
            }
            m.addRow(new Object[]{rank, sa.getStudent().getPerson().getFullName(),
                String.format("%.1f%%", sa.getPercentage()), sa.getGrade().getLabel()});
        }
        boolean anyFinal = false;
        for (model.course.SeatAssignment sa : o.getSeatAssignments()) {
            if (sa.getGrade().isFinal()) {
                anyFinal = true;
                break;
            }
        }
        m.addRow(new Object[]{"", "CLASS GPA", anyFinal ? String.format("%.2f", o.getClassGPA()) : "n/a", ""});
    }
    tblSummary.setModel(m);
}

/** Adds a new assignment to the selected course after validating the inputs. */
private void addAssignment() {
    model.course.CourseOffering o = currentOffering();
    if (o == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "Select a course first.");
        return;
    }
    String title = txtAssignTitle.getText().trim();
    if (title.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Assignment title is required.");
        return;
    }
    double max;
    try {
        max = Double.parseDouble(txtAssignMax.getText().trim());
    } catch (NumberFormatException ex) {
        javax.swing.JOptionPane.showMessageDialog(this, "Max score must be a number.");
        return;
    }
    if (Double.isNaN(max) || Double.isInfinite(max) || max <= 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "Max score must be greater than 0.");
        return;
    }
    try {
        o.addAssignment(title, max);
    } catch (IllegalArgumentException | IllegalStateException ex) {
        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        return;
    }
    txtAssignTitle.setText("");
    txtAssignMax.setText("");
    loadGrades();
}

       /** The student row selected in the table, or null. */
private model.course.SeatAssignment selectedSeat() {
    int row = tblGrades.getSelectedRow();
    return (row < 0 || row >= seats.size()) ? null : seats.get(row);
}

/** Reloads the table but keeps the same student selected. */
private void reloadKeepSelection() {
    model.course.SeatAssignment keep = selectedSeat();
    loadGrades();
    int idx = keep == null ? -1 : seats.indexOf(keep);
    if (idx >= 0) {
        tblGrades.setRowSelectionInterval(idx, idx);
    }
}

/** Records the typed score for the selected student and assignment. */
private void recordScore() {
    model.course.CourseOffering o = currentOffering();
    model.course.SeatAssignment sa = selectedSeat();
    if (o == null || sa == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "Select a student in the table first.");
        return;
    }
    int ai = cmbAssignment.getSelectedIndex();
    if (ai < 0 || ai >= o.getAssignments().size()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Add an assignment first, then choose it.");
        return;
    }
    String text = txtScore.getText() == null ? "" : txtScore.getText().trim();
    if (text.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Enter a score.");
        return;
    }
    double score;
    try {
        score = Double.parseDouble(text);
    } catch (NumberFormatException ex) {
        javax.swing.JOptionPane.showMessageDialog(this, "Score must be a number.");
        return;
    }
    // parseDouble accepts "NaN" and "Infinity", and the model's range check lets NaN through
    if (Double.isNaN(score) || Double.isInfinite(score)) {
        javax.swing.JOptionPane.showMessageDialog(this, "Score must be a real number.");
        return;
    }
    try {
        sa.recordScore(o.getAssignments().get(ai), score);   // model rejects < 0 or > max
    } catch (IllegalArgumentException | IllegalStateException ex) {
        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        return;
    }
    txtScore.setText("");
    reloadKeepSelection();
}

/** Computes the final letter grade for the selected student. */
private void computeSelected() {
    model.course.SeatAssignment sa = selectedSeat();
    if (sa == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "Select a student in the table first.");
        return;
    }
    try {
        model.record.Grade g = sa.computeFinalGrade();   // throws if no scores yet
        reloadKeepSelection();
        javax.swing.JOptionPane.showMessageDialog(this, sa.getStudent().getPerson().getFullName()
                + " receives " + g.getLabel() + String.format(" (%.1f%%).", sa.getPercentage()));
    } catch (IllegalStateException | IllegalArgumentException ex) {
        javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
    }
}

/** Computes final grades for everyone who has at least one score. */
private void computeAll() {
    model.course.CourseOffering o = currentOffering();
    if (o == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "Select a course first.");
        return;
    }
    int done = 0;
    int skipped = 0;
    for (model.course.SeatAssignment sa : o.getSeatAssignments()) {
        if (sa.hasScores()) {
            sa.computeFinalGrade();
            done++;
        } else {
            skipped++;
        }
    }
    reloadKeepSelection();
    javax.swing.JOptionPane.showMessageDialog(this, "Final grades computed for " + done + " student(s)."
            + (skipped > 0 ? "\n" + skipped + " student(s) skipped: no scores yet." : ""));
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
        tblGrades = new javax.swing.JTable();
        btnAddAssignment = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cmbCourse = new javax.swing.JComboBox<>();
        txtAssignTitle = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtAssignMax = new javax.swing.JTextField();
        cmbAssignment = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtScore = new javax.swing.JTextField();
        btnRecordScore = new javax.swing.JButton();
        btnComputeOne = new javax.swing.JButton();
        btnComputeAll = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblSummary = new javax.swing.JTable();

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Semester");

        cmbSemester.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tblGrades.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblGrades);

        btnAddAssignment.setBackground(new java.awt.Color(204, 255, 255));
        btnAddAssignment.setText("Add Assignment");
        btnAddAssignment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAssignmentActionPerformed(evt);
            }
        });

        jLabel7.setText("New Assignment");

        jLabel8.setText("Course");

        cmbCourse.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtAssignTitle.setText("[title]");

        jLabel9.setText("Max Score");

        cmbAssignment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("Assignment");

        jLabel3.setText("Score");

        btnRecordScore.setBackground(new java.awt.Color(204, 255, 255));
        btnRecordScore.setText("Record Score");

        btnComputeOne.setText("Compute Final Grade (Selected)");

        btnComputeAll.setText("Compute All Final Grades");
        btnComputeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnComputeAllActionPerformed(evt);
            }
        });

        tblSummary.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblSummary);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSemester, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAssignMax)
                            .addComponent(cmbCourse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddAssignment, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAssignTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(btnComputeOne, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnComputeAll, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbAssignment, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(73, 73, 73)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtScore, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnRecordScore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1))))
                .addGap(47, 47, 47))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbSemester, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(cmbCourse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtAssignTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtAssignMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddAssignment))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAssignment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(txtScore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRecordScore))
                .addGap(34, 34, 34)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnComputeOne)
                    .addComponent(btnComputeAll))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnComputeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnComputeAllActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnComputeAllActionPerformed

    private void btnAddAssignmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAssignmentActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_btnAddAssignmentActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAssignment;
    private javax.swing.JButton btnComputeAll;
    private javax.swing.JButton btnComputeOne;
    private javax.swing.JButton btnRecordScore;
    private javax.swing.JComboBox<String> cmbAssignment;
    private javax.swing.JComboBox<String> cmbCourse;
    private javax.swing.JComboBox<String> cmbSemester;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblGrades;
    private javax.swing.JTable tblSummary;
    private javax.swing.JTextField txtAssignMax;
    private javax.swing.JTextField txtAssignTitle;
    private javax.swing.JTextField txtScore;
    // End of variables declaration//GEN-END:variables
}
