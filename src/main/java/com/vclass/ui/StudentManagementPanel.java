package com.vclass.ui;

import com.vclass.model.StudentRecord;
import com.vclass.service.StudentRepository;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

// Purpose: Admin screen for listing and managing student accounts.
public class StudentManagementPanel extends JPanel {
  private static final Color BG=new Color(246,247,249),PANEL=Color.WHITE,MUTED=new Color(95,99,104),ACCENT=new Color(70,74,78);
  private final StudentRepository repository=new StudentRepository();
  private final DefaultTableModel model=new DefaultTableModel(new Object[]{"ID","Student No.","Full Name","Email","Department","Semester","Status"},0) {
    @Override public boolean isCellEditable(int row,int column){return false;}
  };
  private final JTable table=new JTable(model);
  private final JTextField search=new JTextField(22);
  private JButton refreshButton;
  private JButton editButton;
  private JButton deleteButton;
  private final JLabel status=new JLabel(" ");

  public StudentManagementPanel() {
    setLayout(new BorderLayout(0,16));setOpaque(false);
    add(buildToolbar(),BorderLayout.NORTH);add(buildTable(),BorderLayout.CENTER);
    status.setForeground(MUTED);status.setFont(new Font("Segoe UI",Font.PLAIN,12));status.setBorder(new EmptyBorder(0,2,0,0));add(status,BorderLayout.SOUTH);
    table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())updateSelectionButtons();});
    search.addActionListener(e->loadStudents());
    loadStudents();
  }

// Purpose: Creates the student search and account action controls.
  private JPanel buildToolbar() {
    JPanel wrap=new JPanel(new BorderLayout(0,14));wrap.setOpaque(false);
    JPanel intro=new JPanel();intro.setOpaque(false);intro.setLayout(new BoxLayout(intro,BoxLayout.Y_AXIS));
    JLabel heading=new JLabel("Manage student accounts");heading.setForeground(new Color(32,33,36));heading.setFont(new Font("Segoe UI",Font.BOLD,19));
    JLabel sub=new JLabel("Create, update, search, and remove student profiles and login accounts.");sub.setForeground(MUTED);sub.setFont(new Font("Segoe UI",Font.PLAIN,12));
    intro.add(heading);intro.add(Box.createVerticalStrut(5));intro.add(sub);wrap.add(intro,BorderLayout.NORTH);
    JPanel actions=new JPanel(new GridLayout(2,1,0,6));actions.setOpaque(false);
    JPanel searchActions=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));searchActions.setOpaque(false);
    JPanel recordActions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));recordActions.setOpaque(false);
    search.setToolTipText("Search name, email, student number, or department");search.setPreferredSize(new Dimension(210,36));styleField(search);
    JButton searchButton=button("Search",false);searchButton.addActionListener(e->loadStudents());searchActions.add(new JLabel("Search"));searchActions.add(search);searchActions.add(searchButton);
    refreshButton=button("Refresh",false);refreshButton.addActionListener(e->loadStudents());searchActions.add(refreshButton);
    JButton addButton=button("+ Add student",true);addButton.addActionListener(e->openForm(null));recordActions.add(addButton);
    editButton=button("Edit selected",false);editButton.addActionListener(e->openSelectedForEdit());recordActions.add(editButton);
    deleteButton=button("Delete selected",false);deleteButton.addActionListener(e->deleteSelected());recordActions.add(deleteButton);
    updateSelectionButtons();actions.add(searchActions);actions.add(recordActions);wrap.add(actions,BorderLayout.CENTER);return wrap;
  }

// Purpose: Builds the student JTable and its scroll container.
  private JScrollPane buildTable() {
    table.setRowHeight(38);table.setFont(new Font("Segoe UI",Font.PLAIN,13));table.setForeground(new Color(32,33,36));
    table.setBackground(PANEL);table.setGridColor(new Color(218,221,225));table.setSelectionBackground(new Color(225,228,232));table.setSelectionForeground(new Color(32,33,36));
    table.setFillsViewportHeight(true);table.setAutoCreateRowSorter(true);table.setShowVerticalLines(false);
    table.getTableHeader().setReorderingAllowed(false);table.getTableHeader().setBackground(new Color(245,246,248));
    table.getTableHeader().setForeground(new Color(55,59,64));table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
    table.getColumnModel().getColumn(0).setMinWidth(0);table.getColumnModel().getColumn(0).setMaxWidth(0);table.getColumnModel().getColumn(0).setWidth(0);
    JScrollPane scroll=new JScrollPane(table);scroll.getViewport().setBackground(PANEL);scroll.setBorder(BorderFactory.createLineBorder(new Color(218,221,225)));return scroll;
  }

// Purpose: Creates a consistently styled button.
  private JButton button(String text,boolean primary) {
    JButton b=new JButton(text);b.setFont(new Font("Segoe UI",Font.BOLD,12));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    if(primary){b.setBackground(new Color(232,234,237));b.setForeground(new Color(32,33,36));b.setBorder(new EmptyBorder(9,14,9,14));}
    else{b.setBackground(new Color(245,246,248));b.setForeground(new Color(45,49,54));b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(8,12,8,12)));}
    return b;
  }

// Purpose: Applies the standard appearance to a text field.
  private void styleField(JTextField f) {
    f.setBackground(Color.WHITE);f.setForeground(new Color(32,33,36));f.setCaretColor(new Color(32,33,36));f.setFont(new Font("Segoe UI",Font.PLAIN,13));
    f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(0,10,0,10)));
  }

// Purpose: Loads searched student rows from MySQL without freezing Swing.
  private void loadStudents() {
    String query=search.getText().trim();setBusy(true);status.setText("Loading students...");
    new SwingWorker<List<StudentRecord>,Void>() {
      private Exception failure;
      @Override protected List<StudentRecord> doInBackground(){try{return repository.findAll(query);}catch(SQLException|java.io.IOException e){failure=e;return null;}}
      @Override protected void done(){setBusy(false);try{
        List<StudentRecord> rows=get();if(failure!=null){showFailure(failure);return;}
        model.setRowCount(0);for(StudentRecord s:rows)model.addRow(new Object[]{s.getStudentId(),s.getStudentNumber(),s.getFullName(),s.getEmail(),s.getDepartment()==null?"—":s.getDepartment(),s.getSemester()==null?"—":s.getSemester(),s.isActive()?"Active":"Inactive"});
        status.setText(rows.size()+" student(s)");
      }catch(Exception e){showFailure(e);}}
    }.execute();
  }

// Purpose: Displays the add or edit student form and saves validated input.
  private void openForm(StudentRecord existing) {
    JTextField name=new JTextField(existing==null?"":existing.getFullName(),22);
    JTextField email=new JTextField(existing==null?"":existing.getEmail(),22);
    JTextField number=new JTextField(existing==null?"":existing.getStudentNumber(),22);
    JTextField department=new JTextField(existing==null?"":safe(existing.getDepartment()),22);
    JComboBox<Integer> semester=new JComboBox<>();for(int i=1;i<=12;i++)semester.addItem(i);if(existing!=null&&existing.getSemester()!=null)semester.setSelectedItem(existing.getSemester());
    JPasswordField password=new JPasswordField(22);
    JPanel form=new JPanel(new GridLayout(0,2,10,10));form.setBorder(new EmptyBorder(8,4,4,4));
    form.add(new JLabel("Full name *"));form.add(name);form.add(new JLabel("Email *"));form.add(email);
    form.add(new JLabel("Student number *"));form.add(number);form.add(new JLabel("Department"));form.add(department);
    form.add(new JLabel("Semester"));form.add(semester);form.add(new JLabel(existing==null?"Password *":"New password"));form.add(password);
    String title=existing==null?"Add student":"Edit student";
    while(true) {
      int choice=JOptionPane.showConfirmDialog(this,form,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
      if(choice!=JOptionPane.OK_OPTION){Arrays.fill(password.getPassword(),'\0');return;}
      char[] secret=password.getPassword();
      String validation=validate(name.getText(),email.getText(),number.getText(),secret,existing==null);
      if(validation!=null){Arrays.fill(secret,'\0');JOptionPane.showMessageDialog(this,validation,"Check student details",JOptionPane.WARNING_MESSAGE);continue;}
      int term=(Integer)semester.getSelectedItem();
      setBusy(true);status.setText(existing==null?"Creating student account...":"Saving student changes...");
      new SwingWorker<Void,Void>() {
        private Exception failure;
        @Override protected Void doInBackground(){try{
          if(existing==null)repository.create(name.getText(),email.getText(),secret,number.getText(),department.getText(),term);
          else repository.update(existing,name.getText(),email.getText(),secret,number.getText(),department.getText(),term);
          return null;
        }catch(Exception e){failure=e;return null;}finally{Arrays.fill(secret,'\0');}}
        @Override protected void done(){setBusy(false);if(failure!=null){showFailure(failure);return;}status.setText(existing==null?"Student account created.":"Student details updated.");loadStudents();}
      }.execute();
      return;
    }
  }

// Purpose: Returns a helpful message if student form values are invalid.
  private String validate(String name,String email,String number,char[] password,boolean creating) {
    if(name.isBlank()||email.isBlank()||number.isBlank())return "Full name, email, and student number are required.";
    if(!email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))return "Enter a valid email address.";
    if(creating&&password.length==0)return "Enter a password for the student account.";
    if(password.length>0&&password.length<6)return "Password must contain at least 6 characters.";
    return null;
  }

// Purpose: Loads the selected student and displays it in the edit form.
  private void openSelectedForEdit() {
    int viewRow=table.getSelectedRow();if(viewRow<0)return;int row=table.convertRowIndexToModel(viewRow);int id=(Integer)model.getValueAt(row,0);
    new SwingWorker<StudentRecord,Void>() {
      private Exception failure;
      @Override protected StudentRecord doInBackground(){try{for(StudentRecord s:repository.findAll("") )if(s.getStudentId()==id)return s;return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){try{StudentRecord s=get();if(failure!=null)showFailure(failure);else if(s!=null)openForm(s);else loadStudents();}catch(Exception e){showFailure(e);}}
    }.execute();
  }

// Purpose: Confirms and deletes the selected student account.
  private void deleteSelected() {
    int viewRow=table.getSelectedRow();if(viewRow<0)return;int row=table.convertRowIndexToModel(viewRow);
    int id=(Integer)model.getValueAt(row,0);String studentName=String.valueOf(model.getValueAt(row,2));
    int answer=JOptionPane.showConfirmDialog(this,"Permanently delete "+studentName+"'s account? Related enrollments, attendance records, and submissions will also be deleted.","Delete student",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
    if(answer!=JOptionPane.YES_OPTION)return;
    setBusy(true);status.setText("Deleting student...");
    new SwingWorker<Void,Void>() {
      private Exception failure;
      @Override protected Void doInBackground(){try{repository.delete(id);return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){setBusy(false);if(failure!=null)showFailure(failure);else{status.setText("Student deleted.");loadStudents();}}
    }.execute();
  }

// Purpose: Enables edit and delete only when a table row is selected.
  private void updateSelectionButtons(){boolean selected=table.getSelectedRow()>=0;editButton.setEnabled(selected);deleteButton.setEnabled(selected);}
// Purpose: Disables the table and refresh button while a database request runs.
  private void setBusy(boolean busy){refreshButton.setEnabled(!busy);table.setEnabled(!busy);}
  private String safe(String value){return value==null?"":value;}
// Purpose: Displays a database or validation failure in a dialog.
  private void showFailure(Exception e){String message=e.getMessage()==null?e.toString():e.getMessage();
    if(message.contains("Duplicate entry"))message="That email or student number is already in use.";
    JOptionPane.showMessageDialog(this,message,"Student management error",JOptionPane.ERROR_MESSAGE);status.setText("Could not complete the operation.");}
}




















