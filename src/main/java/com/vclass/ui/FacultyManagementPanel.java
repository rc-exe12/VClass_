package com.vclass.ui;

import com.vclass.model.FacultyRecord;
import com.vclass.service.FacultyRepository;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

// Purpose: Admin screen for listing and managing Faculty accounts.
public class FacultyManagementPanel extends JPanel {
  private static final Color PANEL=Color.WHITE,MUTED=new Color(95,99,104),ACCENT=new Color(85,89,94);
  private final FacultyRepository repository=new FacultyRepository();
  private final DefaultTableModel model=new DefaultTableModel(new Object[]{"ID","Employee No.","Full Name","Email","Department","Status"},0) {
    @Override public boolean isCellEditable(int row,int column){return false;}
  };
  private final JTable table=new JTable(model);
  private final JTextField search=new JTextField(22);
  private JButton refreshButton,editButton,deleteButton;
  private final JLabel status=new JLabel(" ");

  public FacultyManagementPanel() {
    setLayout(new BorderLayout(0,16));setOpaque(false);add(buildToolbar(),BorderLayout.NORTH);add(buildTable(),BorderLayout.CENTER);
    status.setForeground(MUTED);status.setFont(new Font("Segoe UI",Font.PLAIN,12));status.setBorder(new EmptyBorder(0,2,0,0));add(status,BorderLayout.SOUTH);
    table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())updateSelectionButtons();});
    search.addActionListener(e->loadFaculty());loadFaculty();
  }

// Purpose: Creates the Faculty search and account action controls.
  private JPanel buildToolbar() {
    JPanel wrap=new JPanel(new BorderLayout(0,14));wrap.setOpaque(false);
    JPanel intro=new JPanel();intro.setOpaque(false);intro.setLayout(new BoxLayout(intro,BoxLayout.Y_AXIS));
    JLabel heading=new JLabel("Manage faculty accounts");heading.setForeground(new Color(32,33,36));heading.setFont(new Font("Segoe UI",Font.BOLD,19));
    JLabel subtitle=new JLabel("Create and manage teacher profiles and login accounts.");subtitle.setForeground(MUTED);subtitle.setFont(new Font("Segoe UI",Font.PLAIN,12));
    intro.add(heading);intro.add(Box.createVerticalStrut(5));intro.add(subtitle);wrap.add(intro,BorderLayout.NORTH);
    JPanel actions=new JPanel(new GridLayout(2,1,0,6));actions.setOpaque(false);
    JPanel searchRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));searchRow.setOpaque(false);
    JPanel recordRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));recordRow.setOpaque(false);
    search.setToolTipText("Search by name, email, employee number, or department");search.setPreferredSize(new Dimension(210,36));styleField(search);
    JButton searchButton=button("Search",false);searchButton.addActionListener(e->loadFaculty());searchRow.add(new JLabel("Search"));searchRow.add(search);searchRow.add(searchButton);
    refreshButton=button("Refresh",false);refreshButton.addActionListener(e->loadFaculty());searchRow.add(refreshButton);
    JButton addButton=button("+ Add faculty",true);addButton.addActionListener(e->openForm(null));recordRow.add(addButton);
    editButton=button("Edit selected",false);editButton.addActionListener(e->openSelectedForEdit());recordRow.add(editButton);
    deleteButton=button("Delete selected",false);deleteButton.addActionListener(e->deleteSelected());recordRow.add(deleteButton);
    updateSelectionButtons();actions.add(searchRow);actions.add(recordRow);wrap.add(actions,BorderLayout.CENTER);return wrap;
  }

// Purpose: Builds the Faculty JTable and scroll container.
  private JScrollPane buildTable() {
    table.setRowHeight(38);table.setFont(new Font("Segoe UI",Font.PLAIN,13));table.setForeground(new Color(32,33,36));table.setBackground(PANEL);
    table.setGridColor(new Color(218,221,225));table.setSelectionBackground(new Color(225,228,232));table.setSelectionForeground(new Color(32,33,36));
    table.setFillsViewportHeight(true);table.setAutoCreateRowSorter(true);table.setShowVerticalLines(false);table.getTableHeader().setReorderingAllowed(false);
    table.getTableHeader().setBackground(new Color(245,246,248));table.getTableHeader().setForeground(new Color(55,59,64));table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
    table.getColumnModel().getColumn(0).setMinWidth(0);table.getColumnModel().getColumn(0).setMaxWidth(0);table.getColumnModel().getColumn(0).setWidth(0);
    JScrollPane scroll=new JScrollPane(table);scroll.getViewport().setBackground(PANEL);scroll.setBorder(BorderFactory.createLineBorder(new Color(218,221,225)));return scroll;
  }

  private JButton button(String text,boolean primary) {
    JButton b=new JButton(text);b.setFont(new Font("Segoe UI",Font.BOLD,12));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    if(primary){b.setBackground(new Color(232,234,237));b.setForeground(new Color(32,33,36));b.setBorder(new EmptyBorder(9,14,9,14));}
    else{b.setBackground(new Color(245,246,248));b.setForeground(new Color(45,49,54));b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(8,12,8,12)));}
    return b;
  }
// Purpose: Applies the standard appearance to a text field.
  private void styleField(JTextField f){f.setBackground(Color.WHITE);f.setForeground(new Color(32,33,36));f.setCaretColor(new Color(32,33,36));f.setFont(new Font("Segoe UI",Font.PLAIN,13));f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(0,10,0,10)));}

// Purpose: Loads searched Faculty rows from MySQL.
  private void loadFaculty(){
    String query=search.getText().trim();setBusy(true);status.setText("Loading faculty...");
    new SwingWorker<List<FacultyRecord>,Void>(){
      private Exception failure;
      @Override protected List<FacultyRecord> doInBackground(){try{return repository.findAll(query);}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){setBusy(false);try{List<FacultyRecord> rows=get();if(failure!=null){showFailure(failure);return;}model.setRowCount(0);
        for(FacultyRecord f:rows)model.addRow(new Object[]{f.getFacultyId(),f.getEmployeeNumber(),f.getFullName(),f.getEmail(),f.getDepartment()==null?"—":f.getDepartment(),f.isActive()?"Active":"Inactive"});
        status.setText(rows.size()+" faculty member(s)");
      }catch(Exception e){showFailure(e);}}
    }.execute();
  }

// Purpose: Displays the add or edit Faculty form and saves validated input.
  private void openForm(FacultyRecord existing){
    JTextField name=new JTextField(existing==null?"":existing.getFullName(),22),email=new JTextField(existing==null?"":existing.getEmail(),22);
    JTextField number=new JTextField(existing==null?"":existing.getEmployeeNumber(),22),department=new JTextField(existing==null?"":safe(existing.getDepartment()),22);
    JPasswordField password=new JPasswordField(22);JPanel form=new JPanel(new GridLayout(0,2,10,10));form.setBorder(new EmptyBorder(8,4,4,4));
    form.add(new JLabel("Full name *"));form.add(name);form.add(new JLabel("Email *"));form.add(email);form.add(new JLabel("Employee number *"));form.add(number);
    form.add(new JLabel("Department"));form.add(department);form.add(new JLabel(existing==null?"Password *":"New password"));form.add(password);
    while(true){int choice=JOptionPane.showConfirmDialog(this,form,existing==null?"Add faculty":"Edit faculty",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
      if(choice!=JOptionPane.OK_OPTION)return;char[] secret=password.getPassword();String error=validate(name.getText(),email.getText(),number.getText(),secret,existing==null);
      if(error!=null){Arrays.fill(secret,'\0');JOptionPane.showMessageDialog(this,error,"Check faculty details",JOptionPane.WARNING_MESSAGE);continue;}
      setBusy(true);status.setText(existing==null?"Creating faculty account...":"Saving faculty changes...");
      new SwingWorker<Void,Void>(){private Exception failure;
        @Override protected Void doInBackground(){try{if(existing==null)repository.create(name.getText(),email.getText(),secret,number.getText(),department.getText());else repository.update(existing,name.getText(),email.getText(),secret,number.getText(),department.getText());return null;}catch(Exception e){failure=e;return null;}finally{Arrays.fill(secret,'\0');}}
        @Override protected void done(){setBusy(false);if(failure!=null){showFailure(failure);return;}status.setText(existing==null?"Faculty account created.":"Faculty details updated.");loadFaculty();}
      }.execute();return;
    }
  }
// Purpose: Checks required Faculty fields, email format, and password length.
  private String validate(String name,String email,String number,char[] password,boolean creating){
    if(name.isBlank()||email.isBlank()||number.isBlank())return "Full name, email, and employee number are required.";
    if(!email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))return "Enter a valid email address.";
    if(creating&&password.length==0)return "Enter a password for the faculty account.";if(password.length>0&&password.length<6)return "Password must contain at least 6 characters.";return null;
  }
// Purpose: Loads the selected Faculty member into the edit form.
  private void openSelectedForEdit(){int view=table.getSelectedRow();if(view<0)return;int row=table.convertRowIndexToModel(view);int id=(Integer)model.getValueAt(row,0);
    new SwingWorker<FacultyRecord,Void>(){private Exception failure;@Override protected FacultyRecord doInBackground(){try{for(FacultyRecord f:repository.findAll(""))if(f.getFacultyId()==id)return f;return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){try{FacultyRecord f=get();if(failure!=null)showFailure(failure);else if(f!=null)openForm(f);else loadFaculty();}catch(Exception e){showFailure(e);}}}.execute();
  }
// Purpose: Confirms and deletes the selected Faculty account.
  private void deleteSelected(){int view=table.getSelectedRow();if(view<0)return;int row=table.convertRowIndexToModel(view);int id=(Integer)model.getValueAt(row,0);String facultyName=String.valueOf(model.getValueAt(row,2));
    int answer=JOptionPane.showConfirmDialog(this,"Delete "+facultyName+"'s faculty account? Any assigned courses will become unassigned.","Delete faculty",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);if(answer!=JOptionPane.YES_OPTION)return;
    setBusy(true);status.setText("Deleting faculty...");new SwingWorker<Void,Void>(){private Exception failure;@Override protected Void doInBackground(){try{repository.delete(id);return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){setBusy(false);if(failure!=null)showFailure(failure);else{status.setText("Faculty account deleted.");loadFaculty();}}}.execute();
  }
// Purpose: Enables edit and delete when a Faculty row is selected.
  private void updateSelectionButtons(){boolean selected=table.getSelectedRow()>=0;if(editButton!=null)editButton.setEnabled(selected);if(deleteButton!=null)deleteButton.setEnabled(selected);}
// Purpose: Disables relevant controls while a database request runs.
  private void setBusy(boolean busy){if(refreshButton!=null)refreshButton.setEnabled(!busy);table.setEnabled(!busy);}
  private String safe(String value){return value==null?"":value;}
  private void showFailure(Exception e){String message=e.getMessage()==null?e.toString():e.getMessage();if(message.contains("Duplicate entry"))message="That email or employee number is already in use.";JOptionPane.showMessageDialog(this,message,"Faculty management error",JOptionPane.ERROR_MESSAGE);status.setText("Could not complete the operation.");}
}











