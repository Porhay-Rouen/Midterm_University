/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.admin;

import access.Session;
import app.AppContext;

/**
 *
 * @author macbookpro
 */
public class RecordsPanel extends javax.swing.JPanel {
    private final java.util.List<model.person.Person> rowPersons = new java.util.ArrayList<>();
    private java.util.List<model.course.CourseOffering> offerings = new java.util.ArrayList<>();
    private java.util.List<model.person.FacultyRole> facultyList = new java.util.ArrayList<>();


    /**
     * Creates new form RecordsPanel
     */
    private AppContext ctx;
    private Session session;
    
    public RecordsPanel() {
        initComponents();
    }
    
    public RecordsPanel(AppContext ctx, Session session) {
        this();
        session.requireRole(model.person.Role.Type.ADMIN);
        this.ctx = ctx;
        this.session = session;
        
        tblRecords.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        // when a row is clicked, copy that person into the edit boxes
        tblRecords.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillForm();
            }
        });
        showResults(ctx.getUniversity().getPersons());
        loadOfferingChoices();                                  

        // reload every time this page is shown, so the data is never old
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                showResults(RecordsPanel.this.ctx.getUniversity().getPersons());
                loadOfferingChoices();                            
            }
        });
    }
    
    /** Shows only students and faculty (admins and registrars have their own pages). */
    private void showResults(java.util.List<model.person.Person> people) {
        javax.swing.table.DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel(
                new String[]{"University ID", "Name", "Role", "Department", "Email", "Phone", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // users cannot type in the table
            }
        };
        rowPersons.clear();
        for (model.person.Person p : people) {
            model.person.StudentRole student = p.getRole(model.person.StudentRole.class);
            model.person.FacultyRole faculty = p.getRole(model.person.FacultyRole.class);
            if (student == null && faculty == null) {
                continue;   // skip admin and registrar
            }
            model.university.Department dept = student != null ? student.getDepartment() : faculty.getDepartment();
            tableModel.addRow(new Object[]{
                p.getUniversityId(),
                p.getFullName(),
                student != null ? "Student" : "Faculty",
                dept == null ? "" : dept.getName(),
                p.getEmail(),
                p.getPhone(),
                p.isActive() ? "Active" : "Deactivated"});
            rowPersons.add(p);
        }
        tblRecords.setModel(tableModel);
        clearForm();
    }

    /** Copies the selected row into the edit boxes. */
    private void fillForm() {
        int row = tblRecords.getSelectedRow();
        if (row < 0) {
            return;
        }
        model.person.Person p = rowPersons.get(tblRecords.convertRowIndexToModel(row));
        txtFirst.setText(p.getFirstName());
        txtLast.setText(p.getLastName());
        txtEmail.setText(p.getEmail());
        txtPhone.setText(p.getPhone());
        chkActive.setSelected(p.isActive());
    }

    /** Empties the edit boxes. */
    private void clearForm() {
        txtFirst.setText("");
        txtLast.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        chkActive.setSelected(false);
    }
    private void loadOfferingChoices() {
        int keepOffering = cmbOffering.getSelectedIndex();   // so the choice does not jump back to the first one
        offerings = new java.util.ArrayList<>();
        facultyList = new java.util.ArrayList<>();
        cmbOffering.removeAllItems();
        cmbFaculty.removeAllItems();
        for (model.university.Department d : ctx.getUniversity().getAllDepartments()) {
            for (model.course.CourseSchedule schedule : d.getSchedules()) {
                for (model.course.CourseOffering o : schedule.getOfferings()) {
                    offerings.add(o);
                    cmbOffering.addItem(o.getSemester() + " | " + o.getCourse().getCourseId()
                            + " | " + o.getFacultyName());
                }
            }
            for (model.person.FacultyRole f : d.getFacultyDirectory().getFacultyList()) {
                facultyList.add(f);
                cmbFaculty.addItem(f.getPerson().getFullName());
            }
        }
        if (keepOffering >= 0 && keepOffering < cmbOffering.getItemCount()) {
            cmbOffering.setSelectedIndex(keepOffering);
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

        jLabel2 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnByName = new javax.swing.JButton();
        btnById = new javax.swing.JButton();
        btnByDept = new javax.swing.JButton();
        btnShowAll = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRecords = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtFirst = new javax.swing.JTextField();
        txtLast = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        chkActive = new javax.swing.JCheckBox();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        cmbOffering = new javax.swing.JComboBox<>();
        cmbFaculty = new javax.swing.JComboBox<>();
        btnReassign = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel2.setText("Search Records ");

        btnByName.setText("By name");
        btnByName.addActionListener(this::btnByNameActionPerformed);

        btnById.setText("By ID");
        btnById.addActionListener(this::btnByIdActionPerformed);

        btnByDept.setText("By department");
        btnByDept.addActionListener(this::btnByDeptActionPerformed);

        btnShowAll.setText("Show all");
        btnShowAll.addActionListener(this::btnShowAllActionPerformed);

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel3.setText("Records List");

        tblRecords.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "First", "Last", "Email", "Active"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblRecords);
        if (tblRecords.getColumnModel().getColumnCount() > 0) {
            tblRecords.getColumnModel().getColumn(1).setResizable(false);
            tblRecords.getColumnModel().getColumn(2).setResizable(false);
            tblRecords.getColumnModel().getColumn(3).setResizable(false);
        }

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel4.setText("Record Details");

        jLabel5.setText("First name");

        jLabel6.setText("Last name");

        jLabel7.setText("Email");

        jLabel8.setText("Phone");

        chkActive.setText("Active");

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnDelete.setText("Delete");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        jLabel9.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jLabel9.setText("Reassignment");

        jLabel10.setText("Offering");

        jLabel11.setText("New Faculty");

        btnReassign.setText("Reassign");
        btnReassign.addActionListener(this::btnReassignActionPerformed);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel1.setText("Records Management");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(307, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(431, 431, 431))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel5)
                                            .addComponent(jLabel7)
                                            .addComponent(chkActive)
                                            .addComponent(txtFirst)
                                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel10)
                                        .addComponent(cmbOffering, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(11, 11, 11)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addComponent(btnUpdate)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(btnDelete))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addComponent(jLabel6)
                                                    .addGap(169, 169, 169))
                                                .addComponent(txtLast, javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel8)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(cmbFaculty, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel11))
                                            .addGap(50, 50, 50))
                                        .addComponent(btnReassign, javax.swing.GroupLayout.Alignment.TRAILING)))
                                .addComponent(jScrollPane1)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btnByName)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btnById)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnByDept)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnShowAll)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(161, 161, 161)
                                .addComponent(jLabel1)))
                        .addGap(166, 166, 166)))
                .addGap(278, 278, 278))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel1)
                .addGap(27, 27, 27)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnByName)
                    .addComponent(btnById)
                    .addComponent(btnByDept)
                    .addComponent(btnShowAll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFirst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chkActive)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbOffering, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnUpdate)
                            .addComponent(btnDelete))
                        .addGap(29, 29, 29)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbFaculty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnReassign)))
                .addContainerGap(109, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    /** The person in the selected table row. Throws a clear message if no row is selected. */
    private model.person.Person selectedPerson() {
        int row = tblRecords.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Select a person in the table first.");
        }
        return rowPersons.get(tblRecords.convertRowIndexToModel(row));
    }
    
    private void btnByNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnByNameActionPerformed
        // TODO add your handling code here:
        
        try {
            showResults(ctx.getUniversity().searchByName(txtSearch.getText()));
            showNoResultsMessage();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Cannot search", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_btnByNameActionPerformed

    private void btnByIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnByIdActionPerformed
        // TODO add your handling code here:
        
        try {
            showResults(ctx.getUniversity().searchById(txtSearch.getText()));
            showNoResultsMessage();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Cannot search", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnByIdActionPerformed

    private void btnByDeptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnByDeptActionPerformed
        // TODO add your handling code here:
        
        try {
            showResults(ctx.getUniversity().searchByDepartment(txtSearch.getText()));
            showNoResultsMessage();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Cannot search", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_btnByDeptActionPerformed

    private void showNoResultsMessage() {
        if (rowPersons.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No matching students or faculty found.");
        }
    }    
    private void btnShowAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAllActionPerformed
        // TODO add your handling code here:
        
        txtSearch.setText("");
        showResults(ctx.getUniversity().getPersons());
    }//GEN-LAST:event_btnShowAllActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        try {
            model.person.Person p = selectedPerson();
            // check everything first, so a bad field cannot leave a half-edited record
            model.util.Validation.requireText(txtFirst.getText(), "First name");
            model.util.Validation.requireText(txtLast.getText(), "Last name");
            model.util.Validation.optionalPhone(txtPhone.getText());
            ctx.getUniversity().changeEmail(p, txtEmail.getText());   // blocks a duplicate email
            p.setFirstName(txtFirst.getText());
            p.setLastName(txtLast.getText());
            p.setPhone(txtPhone.getText());
            p.setActive(chkActive.isSelected());
            showResults(ctx.getUniversity().getPersons());
            javax.swing.JOptionPane.showMessageDialog(this, "Record updated.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Cannot update", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        
        try {
            model.person.Person p = selectedPerson();
            int answer = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Delete " + p.getFullName() + " (" + p.getUniversityId() + ")?\n"
                    + "This also removes their login and frees their seats.",
                    "Please confirm", javax.swing.JOptionPane.YES_NO_OPTION);
            if (answer != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
            model.person.StudentRole student = p.getRole(model.person.StudentRole.class);
            if (student != null) {
                student.releaseAllSeats();   // give their course seats back
            }
            String username = ctx.getAccessControl().usernameOf(p);
            ctx.getUniversity().deletePerson(p);
            if (username != null) {
                ctx.getAccessControl().deleteAccount(username);   // remove the login too
            }
            showResults(ctx.getUniversity().getPersons());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Cannot delete", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnReassignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReassignActionPerformed
        // TODO add your handling code here:
            try {
                int offeringIndex = cmbOffering.getSelectedIndex();
                int facultyIndex = cmbFaculty.getSelectedIndex();
                if (offeringIndex < 0 || facultyIndex < 0) {
                    throw new IllegalArgumentException("Choose an offering and a faculty member.");
                }
                model.course.CourseOffering offering = offerings.get(offeringIndex);
                model.person.FacultyRole faculty = facultyList.get(facultyIndex);
                offering.assignFaculty(faculty);
                loadOfferingChoices();   // refresh the offering text to show the new teacher
                javax.swing.JOptionPane.showMessageDialog(this, faculty.getPerson().getFullName()
                        + " now teaches " + offering.getCourse().getCourseId() + ".");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Cannot reassign", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        
    }//GEN-LAST:event_btnReassignActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnByDept;
    private javax.swing.JButton btnById;
    private javax.swing.JButton btnByName;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnReassign;
    private javax.swing.JButton btnShowAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox chkActive;
    private javax.swing.JComboBox<String> cmbFaculty;
    private javax.swing.JComboBox<String> cmbOffering;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRecords;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirst;
    private javax.swing.JTextField txtLast;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
