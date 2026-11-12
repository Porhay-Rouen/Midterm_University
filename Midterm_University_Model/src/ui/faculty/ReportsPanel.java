/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.faculty;

/**
 *
 * @author muykhim
 */
public class ReportsPanel extends javax.swing.JPanel {

    /**
     * Creates new form ReportsPanel
     */
    private app.AppContext ctx;
private model.person.FacultyRole me;
private static final String ALL = "All Semesters";
    public ReportsPanel(app.AppContext ctx, access.Session session) {
        initComponents();
        session.requireRole(model.person.Role.Type.FACULTY);
        this.ctx = ctx;
        this.me = (model.person.FacultyRole) session.getRole();
        loadSemesters();
        cmbSemester.addActionListener(e -> loadReport());
        btnExport.addActionListener(e -> exportCsv());
        loadReport();
    }
    /** "All Semesters" first, then each semester. */
private void loadSemesters() {
    cmbSemester.removeAllItems();
    cmbSemester.addItem(ALL);
    for (model.course.Semester s : ctx.getUniversity().getSemesters()) {
        cmbSemester.addItem(s.toString());
    }
}

/** This faculty's offerings for the chosen semester (or all). */
private java.util.List<model.course.CourseOffering> selectedOfferings() {
    int idx = cmbSemester.getSelectedIndex();
    if (idx <= 0) {
        return new java.util.ArrayList<>(me.getOfferings());
    }
    java.util.List<model.course.Semester> sems = ctx.getUniversity().getSemesters();
    if (idx - 1 >= sems.size()) {
        return new java.util.ArrayList<>();
    }
    return me.getOfferings(sems.get(idx - 1));
}

private javax.swing.table.DefaultTableModel readOnly(Object[] cols) {
    return new javax.swing.table.DefaultTableModel(cols, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
}

private String money(double d) {
    return String.format(java.util.Locale.US, "$%,.2f", d);
}

/** Average % over students who have a score. Ungraded students are left out so they don't drag it down. NaN if nobody is graded. */
private double averageOfGraded(model.course.CourseOffering o) {
    double sum = 0;
    int n = 0;
    for (model.course.SeatAssignment sa : o.getSeatAssignments()) {
        if (sa.hasScores()) {
            sum += sa.getPercentage();
            n++;
        }
    }
    return n == 0 ? Double.NaN : sum / n;
}

private boolean anyFinalGrade(model.course.CourseOffering o) {
    for (model.course.SeatAssignment sa : o.getSeatAssignments()) {
        if (sa.getGrade().isFinal()) {
            return true;
        }
    }
    return false;
}



private void loadReport() {
    java.util.List<model.course.CourseOffering> offerings = selectedOfferings();
    boolean all = cmbSemester.getSelectedIndex() <= 0;

    // 1) Course performance
    javax.swing.table.DefaultTableModel sum = readOnly(new Object[]{"Semester", "Course ID",
        "Title", "Enrolled", "Capacity", "Avg % (graded)", "Avg Grade", "Class GPA"});
    double collected = 0;
    double unpaid = 0;
    for (model.course.CourseOffering o : offerings) {
        double avg = averageOfGraded(o);
        boolean graded = !Double.isNaN(avg);
        sum.addRow(new Object[]{o.getSemester().toString(), o.getCourse().getCourseId(),
            o.getTitle(), o.getEnrolledCount(), o.getCapacity(),
            graded ? String.format("%.1f%%", avg) : "-",
            graded ? model.record.Grade.fromPercentage(avg).getLabel() : "-",
            anyFinalGrade(o) ? String.format("%.2f", o.getClassGPA()) : "-"});
        collected += o.getTuitionCollected();
        unpaid += o.getTuitionUnpaid();
    }
    tblSummary.setModel(sum);

    // 2) Grade distribution: one row per grade, one column per course
    java.util.List<Object> cols = new java.util.ArrayList<>();
    cols.add("Grade");
    for (model.course.CourseOffering o : offerings) {
        cols.add(all ? o.getCourse().getCourseId() + " (" + o.getSemester() + ")"
                : o.getCourse().getCourseId());
    }
    cols.add("Total");
    javax.swing.table.DefaultTableModel dist = readOnly(cols.toArray());
    for (model.record.Grade g : model.record.Grade.values()) {
        java.util.List<Object> row = new java.util.ArrayList<>();
        row.add(g == model.record.Grade.IP ? "IP (in progress)" : g.getLabel());
        int total = 0;
        for (model.course.CourseOffering o : offerings) {
            int n = o.getGradeDistribution().getOrDefault(g, 0);
            row.add(n);
            total += n;
        }
        row.add(total);
        dist.addRow(row.toArray());
    }
    tblDistribution.setModel(dist);

    // 3) Tuition
    javax.swing.table.DefaultTableModel tui = readOnly(new Object[]{
        "Collected (selected)", "Unpaid (selected)", "Collected (all semesters)"});
    tui.addRow(new Object[]{money(collected), money(unpaid), money(me.getTotalTuitionCollected())});
    tblTuition.setModel(tui);

    btnExport.setEnabled(!offerings.isEmpty());
}



private void exportCsv() {
    if (tblSummary.getRowCount() == 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "There is nothing to export for this semester.");
        return;
    }
    javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
    String sem = String.valueOf(cmbSemester.getSelectedItem()).replaceAll("[^A-Za-z0-9]+", "_");
    chooser.setSelectedFile(new java.io.File("course_report_" + sem + ".csv"));
    if (chooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
        return;
    }
    java.io.File file = chooser.getSelectedFile();
    if (file == null) {
        return;
    }
    if (!file.getName().toLowerCase().endsWith(".csv")) {
        file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
    }
    if (file.exists() && javax.swing.JOptionPane.showConfirmDialog(this,
            file.getName() + " already exists. Replace it?", "Confirm",
            javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) {
        return;
    }
    try (java.io.Writer out = java.nio.file.Files.newBufferedWriter(file.toPath(),
            java.nio.charset.StandardCharsets.UTF_8)) {
        out.write("Course performance report - " + csv(String.valueOf(cmbSemester.getSelectedItem())) + "\n");
        out.write("Faculty," + csv(me.getPerson().getFullName()) + "\n\n");
        writeTable(out, tblSummary.getModel());
        out.write("\nGrade distribution\n");
        writeTable(out, tblDistribution.getModel());
        out.write("\nTuition\n");
        writeTable(out, tblTuition.getModel());
    } catch (java.io.IOException ex) {
        javax.swing.JOptionPane.showMessageDialog(this, "Could not save the report: " + ex.getMessage());
        return;
    }
    javax.swing.JOptionPane.showMessageDialog(this, "Report saved to " + file.getAbsolutePath());
}

private void writeTable(java.io.Writer out, javax.swing.table.TableModel m) throws java.io.IOException {
    StringBuilder sb = new StringBuilder();
    for (int c = 0; c < m.getColumnCount(); c++) {
        sb.append(c == 0 ? "" : ",").append(csv(m.getColumnName(c)));
    }
    sb.append('\n');
    for (int r = 0; r < m.getRowCount(); r++) {
        for (int c = 0; c < m.getColumnCount(); c++) {
            Object v = m.getValueAt(r, c);
            sb.append(c == 0 ? "" : ",").append(csv(v == null ? "" : v.toString()));
        }
        sb.append('\n');
    }
    out.write(sb.toString());
}

/** Quotes a field that has a comma, quote or line break. */
private String csv(String value) {
    String v = value == null ? "" : value;
    if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
        v = "\"" + v.replace("\"", "\"\"") + "\"";
    }
    return v;
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbSemester = new javax.swing.JComboBox<>();
        btnExport = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSummary = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDistribution = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblTuition = new javax.swing.JTable();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Semester");

        cmbSemester.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnExport.setBackground(new java.awt.Color(204, 255, 255));
        btnExport.setText("Export CSV");

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
        jScrollPane1.setViewportView(tblSummary);

        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel2.setText("Course performance");

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel3.setText("Grade distribution (students per grade)");

        tblDistribution.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblDistribution);

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel4.setText("Tuition");

        tblTuition.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(tblTuition);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(cmbSemester, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(btnExport))
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane3))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbSemester, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExport))
                .addGap(24, 24, 24)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JComboBox<String> cmbSemester;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable tblDistribution;
    private javax.swing.JTable tblSummary;
    private javax.swing.JTable tblTuition;
    // End of variables declaration//GEN-END:variables
}
