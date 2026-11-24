package com.vclass.ui;

import com.vclass.model.AssignmentCourseChoice;
import com.vclass.model.AssignmentRecord;
import com.vclass.model.User;
import com.vclass.service.AssignmentRepository;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

// Purpose: Admin and Faculty screen for creating and managing assignments.
public class AssignmentManagementPanel extends JPanel {
  private static final Color MUTED=new Color(95,99,104);
  private final User user;
  private final AssignmentRepository repository=new AssignmentRepository();
  private final DefaultTableModel model=new DefaultTableModel(new Object[]{"ID","Course","Assignment","Due date","Max marks"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
  private final JTable table=new JTable(model);private final JTextField search=new JTextField(22);private final JLabel status=new JLabel(" ");
  private JButton refreshButton,editButton,deleteButton;

  public AssignmentManagementPanel(User user){this.user=user;setLayout(new BorderLayout(0,16));setOpaque(false);add(buildToolbar(),BorderLayout.NORTH);add(buildTable(),BorderLayout.CENTER);
    status.setForeground(MUTED);status.setFont(new Font("Segoe UI",Font.PLAIN,12));add(status,BorderLayout.SOUTH);
    table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())updateSelectionButtons();});search.addActionListener(e->load());load();}
// Purpose: Creates assignment search and create/edit/delete controls.
  private JPanel buildToolbar(){
    JPanel wrap=new JPanel(new BorderLayout(0,12));wrap.setOpaque(false);JPanel heading=new JPanel();heading.setOpaque(false);heading.setLayout(new BoxLayout(heading,BoxLayout.Y_AXIS));
    JLabel title=new JLabel("Manage assignments");title.setForeground(new Color(32,33,36));title.setFont(new Font("Segoe UI",Font.BOLD,19));
    JLabel subtitle=new JLabel("Create assignments for your courses and set a due date and maximum marks.");subtitle.setForeground(MUTED);subtitle.setFont(new Font("Segoe UI",Font.PLAIN,12));heading.add(title);heading.add(Box.createVerticalStrut(5));heading.add(subtitle);wrap.add(heading,BorderLayout.NORTH);
    JPanel actions=new JPanel(new GridLayout(2,1,0,6));actions.setOpaque(false);JPanel searchRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));searchRow.setOpaque(false);JPanel itemRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));itemRow.setOpaque(false);
    search.setPreferredSize(new Dimension(210,36));styleField(search);search.setToolTipText("Search assignment or course");searchRow.add(new JLabel("Search"));searchRow.add(search);
    JButton go=button("Search");go.addActionListener(e->load());searchRow.add(go);refreshButton=button("Refresh");refreshButton.addActionListener(e->load());searchRow.add(refreshButton);
    JButton add=button("+ Add assignment");add.addActionListener(e->openForm(null));itemRow.add(add);editButton=button("Edit selected");editButton.addActionListener(e->openSelectedForEdit());itemRow.add(editButton);
    deleteButton=button("Delete selected");deleteButton.addActionListener(e->deleteSelected());itemRow.add(deleteButton);updateSelectionButtons();actions.add(searchRow);actions.add(itemRow);wrap.add(actions,BorderLayout.CENTER);return wrap;
  }
// Purpose: Builds the assignment JTable and scroll container.
  private JScrollPane buildTable(){table.setRowHeight(38);table.setFont(new Font("Segoe UI",Font.PLAIN,13));table.setForeground(new Color(32,33,36));table.setBackground(Color.WHITE);table.setGridColor(new Color(218,221,225));table.setSelectionBackground(new Color(225,228,232));table.setSelectionForeground(new Color(32,33,36));table.setFillsViewportHeight(true);table.setAutoCreateRowSorter(true);table.setShowVerticalLines(false);table.getTableHeader().setReorderingAllowed(false);table.getTableHeader().setBackground(new Color(245,246,248));table.getTableHeader().setForeground(new Color(55,59,64));table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));table.getColumnModel().getColumn(0).setMinWidth(0);table.getColumnModel().getColumn(0).setMaxWidth(0);table.getColumnModel().getColumn(0).setWidth(0);JScrollPane scroll=new JScrollPane(table);scroll.getViewport().setBackground(Color.WHITE);scroll.setBorder(BorderFactory.createLineBorder(new Color(218,221,225)));return scroll;}
// Purpose: Creates a consistently styled assignment button.
  private JButton button(String text){JButton b=new JButton(text);b.setFont(new Font("Segoe UI",Font.BOLD,12));b.setBackground(new Color(245,246,248));b.setForeground(new Color(45,49,54));b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(8,12,8,12)));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
// Purpose: Applies the standard appearance to an input field.
  private void styleField(JTextField f){f.setBackground(Color.WHITE);f.setForeground(new Color(32,33,36));f.setCaretColor(new Color(32,33,36));f.setFont(new Font("Segoe UI",Font.PLAIN,13));f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(0,10,0,10)));}
// Purpose: Loads assignments the signed-in staff user is allowed to manage.
  private void load(){String q=search.getText().trim();busy(true);status.setText("Loading assignments...");new SwingWorker<List<AssignmentRecord>,Void>(){Exception failure;
    @Override protected List<AssignmentRecord> doInBackground(){try{return repository.findForStaff(user.getRole(),user.getId(),q);}catch(Exception e){failure=e;return null;}}
    @Override protected void done(){busy(false);try{List<AssignmentRecord> rows=get();if(failure!=null){fail(failure);return;}model.setRowCount(0);for(AssignmentRecord a:rows)model.addRow(new Object[]{a.getAssignmentId(),a.getCourseCode()+" — "+a.getCourseName(),a.getTitle(),a.getDueAt(),a.getMaxMarks()});status.setText(rows.size()+" assignment(s)");}catch(Exception e){fail(e);}}
  }.execute();}
// Purpose: Loads courses the staff user is allowed to use for an assignment.
  private void openForm(AssignmentRecord existing){busy(true);status.setText("Loading your courses...");new SwingWorker<List<AssignmentCourseChoice>,Void>(){Exception failure;
    @Override protected List<AssignmentCourseChoice> doInBackground(){try{return repository.findCoursesForStaff(user.getRole(),user.getId());}catch(Exception e){failure=e;return null;}}
    @Override protected void done(){busy(false);try{List<AssignmentCourseChoice> courses=get();if(failure!=null){fail(failure);return;}if(courses.isEmpty()){JOptionPane.showMessageDialog(AssignmentManagementPanel.this,"Create a course and assign it to your account before adding an assignment.","No courses available",JOptionPane.INFORMATION_MESSAGE);return;}showForm(existing,courses);}catch(Exception e){fail(e);}}
  }.execute();}
// Purpose: Collects and validates assignment details, then inserts or updates them.
  private void showForm(AssignmentRecord existing,List<AssignmentCourseChoice> courses){
    JComboBox<AssignmentCourseChoice> courseBox=new JComboBox<>(courses.toArray(new AssignmentCourseChoice[0]));if(existing!=null)for(int i=0;i<courseBox.getItemCount();i++)if(courseBox.getItemAt(i).getCourseId()==existing.getCourseId())courseBox.setSelectedIndex(i);
    JTextField title=new JTextField(existing==null?"":existing.getTitle(),24);JTextArea description=new JTextArea(existing==null?"":safe(existing.getDescription()),3,24);description.setLineWrap(true);description.setWrapStyleWord(true);
    JTextField due=new JTextField(existing==null?"":existing.getDueAt().toString().replace('T',' '),24);JTextField marks=new JTextField(existing==null?"100":existing.getMaxMarks().toPlainString(),24);
    JPanel form=new JPanel(new GridLayout(0,2,10,10));form.setBorder(new EmptyBorder(8,4,4,4));form.add(new JLabel("Course *"));form.add(courseBox);form.add(new JLabel("Title *"));form.add(title);form.add(new JLabel("Description"));form.add(new JScrollPane(description));form.add(new JLabel("Due date/time *"));form.add(due);form.add(new JLabel("Max marks *"));form.add(marks);
    while(true){int result=JOptionPane.showConfirmDialog(this,form,existing==null?"Add assignment":"Edit assignment",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);if(result!=JOptionPane.OK_OPTION)return;
      if(title.getText().isBlank()){JOptionPane.showMessageDialog(this,"Assignment title is required.","Check details",JOptionPane.WARNING_MESSAGE);continue;}
      try{Timestamp.valueOf(due.getText().trim());double value=Double.parseDouble(marks.getText().trim());if(value<=0)throw new IllegalArgumentException();}catch(Exception e){JOptionPane.showMessageDialog(this,"Enter the due date as YYYY-MM-DD HH:MM:SS and a positive maximum mark value.","Check details",JOptionPane.WARNING_MESSAGE);continue;}break;}
    AssignmentCourseChoice selected=(AssignmentCourseChoice)courseBox.getSelectedItem();Timestamp dueAt=Timestamp.valueOf(due.getText().trim());double maxMarks=Double.parseDouble(marks.getText().trim());busy(true);status.setText(existing==null?"Creating assignment...":"Saving assignment...");
    new SwingWorker<Void,Void>(){Exception failure;@Override protected Void doInBackground(){try{if(existing==null)repository.create(selected.getCourseId(),title.getText(),description.getText(),dueAt,maxMarks);else repository.update(existing,selected.getCourseId(),title.getText(),description.getText(),dueAt,maxMarks);return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){busy(false);if(failure!=null){fail(failure);return;}status.setText(existing==null?"Assignment created.":"Assignment updated.");load();}}
    .execute();
  }
// Purpose: Finds the selected assignment and opens the edit form.
  private void openSelectedForEdit(){int view=table.getSelectedRow();if(view<0)return;int row=table.convertRowIndexToModel(view),id=(Integer)model.getValueAt(row,0);new SwingWorker<AssignmentRecord,Void>(){Exception failure;
    @Override protected AssignmentRecord doInBackground(){try{for(AssignmentRecord a:repository.findForStaff(user.getRole(),user.getId(),""))if(a.getAssignmentId()==id)return a;return null;}catch(Exception e){failure=e;return null;}}
    @Override protected void done(){try{AssignmentRecord a=get();if(failure!=null)fail(failure);else if(a!=null)openForm(a);else load();}catch(Exception e){fail(e);}}
  }.execute();}
// Purpose: Confirms and removes the assignment and associated submissions/files.
  private void deleteSelected(){int view=table.getSelectedRow();if(view<0)return;int row=table.convertRowIndexToModel(view),id=(Integer)model.getValueAt(row,0);String title=String.valueOf(model.getValueAt(row,2));int result=JOptionPane.showConfirmDialog(this,"Delete '"+title+"' and its submissions/files?","Delete assignment",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);if(result!=JOptionPane.YES_OPTION)return;
    busy(true);new SwingWorker<Void,Void>(){Exception failure;@Override protected Void doInBackground(){try{repository.delete(id);return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){busy(false);if(failure!=null)fail(failure);else{status.setText("Assignment deleted.");load();}}}.execute();}
// Purpose: Enables edit/delete actions only after a row is selected.
  private void updateSelectionButtons(){boolean selected=table.getSelectedRow()>=0;if(editButton!=null)editButton.setEnabled(selected);if(deleteButton!=null)deleteButton.setEnabled(selected);}
// Purpose: Temporarily disables refresh/table controls during work.
  private void busy(boolean value){if(refreshButton!=null)refreshButton.setEnabled(!value);table.setEnabled(!value);}
  private String safe(String value){return value==null?"":value;}
// Purpose: Shows an assignment operation error to the user.
  private void fail(Exception e){String message=e.getMessage()==null?e.toString():e.getMessage();if(message.contains("Duplicate entry"))message="That assignment already exists.";JOptionPane.showMessageDialog(this,message,"Assignment error",JOptionPane.ERROR_MESSAGE);status.setText("Could not complete the operation.");}
}













