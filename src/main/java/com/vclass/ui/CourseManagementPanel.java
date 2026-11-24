package com.vclass.ui;

import com.vclass.model.CourseRecord;
import com.vclass.model.FacultyChoice;
import com.vclass.service.CourseRepository;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Purpose: Admin screen for creating courses and assigning Faculty.
public class CourseManagementPanel extends JPanel {
  private static final Color PANEL=Color.WHITE,MUTED=new Color(95,99,104);
  private final CourseRepository repository=new CourseRepository();
  private final DefaultTableModel model=new DefaultTableModel(new Object[]{"ID","Course Code","Course Name","Faculty","Description"},0){
    @Override public boolean isCellEditable(int row,int column){return false;}
  };
  private final JTable table=new JTable(model);
  private final JTextField search=new JTextField(22);
  private JButton refreshButton,editButton,deleteButton;
  private final JLabel status=new JLabel(" ");

  public CourseManagementPanel(){
    setLayout(new BorderLayout(0,16));setOpaque(false);add(buildToolbar(),BorderLayout.NORTH);add(buildTable(),BorderLayout.CENTER);
    status.setForeground(MUTED);status.setFont(new Font("Segoe UI",Font.PLAIN,12));status.setBorder(new EmptyBorder(0,2,0,0));add(status,BorderLayout.SOUTH);
    table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())updateSelectionButtons();});search.addActionListener(e->loadCourses());loadCourses();
  }
// Purpose: Creates the course search and course action controls.
  private JPanel buildToolbar(){
    JPanel wrap=new JPanel(new BorderLayout(0,14));wrap.setOpaque(false);
    JPanel intro=new JPanel();intro.setOpaque(false);intro.setLayout(new BoxLayout(intro,BoxLayout.Y_AXIS));
    JLabel heading=new JLabel("Manage courses");heading.setForeground(new Color(32,33,36));heading.setFont(new Font("Segoe UI",Font.BOLD,19));
    JLabel subtitle=new JLabel("Create courses and assign an active faculty member.");subtitle.setForeground(MUTED);subtitle.setFont(new Font("Segoe UI",Font.PLAIN,12));
    intro.add(heading);intro.add(Box.createVerticalStrut(5));intro.add(subtitle);wrap.add(intro,BorderLayout.NORTH);
    JPanel actions=new JPanel(new GridLayout(2,1,0,6));actions.setOpaque(false);
    JPanel searchRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));searchRow.setOpaque(false);
    JPanel recordRow=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));recordRow.setOpaque(false);
    search.setToolTipText("Search course code, name, or assigned faculty");search.setPreferredSize(new Dimension(210,36));styleField(search);
    JButton searchButton=button("Search",false);searchButton.addActionListener(e->loadCourses());searchRow.add(new JLabel("Search"));searchRow.add(search);searchRow.add(searchButton);
    refreshButton=button("Refresh",false);refreshButton.addActionListener(e->loadCourses());searchRow.add(refreshButton);
    JButton addButton=button("+ Add course",true);addButton.addActionListener(e->openForm(null));recordRow.add(addButton);
    editButton=button("Edit selected",false);editButton.addActionListener(e->openSelectedForEdit());recordRow.add(editButton);
    deleteButton=button("Delete selected",false);deleteButton.addActionListener(e->deleteSelected());recordRow.add(deleteButton);
    updateSelectionButtons();actions.add(searchRow);actions.add(recordRow);wrap.add(actions,BorderLayout.CENTER);return wrap;
  }
// Purpose: Builds the course JTable and scroll container.
  private JScrollPane buildTable(){
    table.setRowHeight(38);table.setFont(new Font("Segoe UI",Font.PLAIN,13));table.setForeground(new Color(32,33,36));table.setBackground(PANEL);
    table.setGridColor(new Color(218,221,225));table.setSelectionBackground(new Color(225,228,232));table.setSelectionForeground(new Color(32,33,36));
    table.setFillsViewportHeight(true);table.setAutoCreateRowSorter(true);table.setShowVerticalLines(false);table.getTableHeader().setReorderingAllowed(false);
    table.getTableHeader().setBackground(new Color(245,246,248));table.getTableHeader().setForeground(new Color(55,59,64));table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
    table.getColumnModel().getColumn(0).setMinWidth(0);table.getColumnModel().getColumn(0).setMaxWidth(0);table.getColumnModel().getColumn(0).setWidth(0);
    table.getColumnModel().getColumn(4).setPreferredWidth(260);
    JScrollPane scroll=new JScrollPane(table);scroll.getViewport().setBackground(PANEL);scroll.setBorder(BorderFactory.createLineBorder(new Color(218,221,225)));return scroll;
  }
// Purpose: Creates a consistently styled course action button.
  private JButton button(String text,boolean primary){
    JButton b=new JButton(text);b.setFont(new Font("Segoe UI",Font.BOLD,12));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    if(primary){b.setBackground(new Color(232,234,237));b.setForeground(new Color(32,33,36));b.setBorder(new EmptyBorder(9,14,9,14));}
    else{b.setBackground(new Color(245,246,248));b.setForeground(new Color(45,49,54));b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(8,12,8,12)));}return b;
  }
// Purpose: Applies the standard appearance to a text field.
  private void styleField(JTextField f){f.setBackground(Color.WHITE);f.setForeground(new Color(32,33,36));f.setCaretColor(new Color(32,33,36));f.setFont(new Font("Segoe UI",Font.PLAIN,13));f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(0,10,0,10)));}

// Purpose: Loads searched course rows from MySQL.
  private void loadCourses(){String query=search.getText().trim();setBusy(true);status.setText("Loading courses...");
    new SwingWorker<List<CourseRecord>,Void>(){private Exception failure;
      @Override protected List<CourseRecord> doInBackground(){try{return repository.findAll(query);}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){setBusy(false);try{List<CourseRecord> rows=get();if(failure!=null){showFailure(failure);return;}model.setRowCount(0);
        for(CourseRecord c:rows)model.addRow(new Object[]{c.getCourseId(),c.getCourseCode(),c.getCourseName(),c.getFacultyName()==null?"Unassigned":c.getFacultyName(),c.getDescription()==null?"—":c.getDescription()});status.setText(rows.size()+" course(s)");
      }catch(Exception e){showFailure(e);}}
    }.execute();
  }

// Purpose: Loads active Faculty choices before opening the course form.
  private void openForm(CourseRecord existing){setBusy(true);status.setText("Loading faculty list...");
    new SwingWorker<List<FacultyChoice>,Void>(){private Exception failure;
      @Override protected List<FacultyChoice> doInBackground(){try{return repository.findActiveFaculty();}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){setBusy(false);try{List<FacultyChoice> faculty=get();if(failure!=null){showFailure(failure);return;}showCourseForm(existing,faculty);}catch(Exception e){showFailure(e);}}
    }.execute();
  }

// Purpose: Validates and saves course fields selected in the form.
  private void showCourseForm(CourseRecord existing,List<FacultyChoice> faculty){
    JTextField code=new JTextField(existing==null?"":existing.getCourseCode(),22),name=new JTextField(existing==null?"":existing.getCourseName(),22);
    JTextArea description=new JTextArea(existing==null?"":safe(existing.getDescription()),4,22);description.setLineWrap(true);description.setWrapStyleWord(true);
    JComboBox<FacultyChoice> facultyBox=new JComboBox<>(faculty.toArray(new FacultyChoice[0]));
    if(existing!=null&&existing.getFacultyId()!=null)for(int i=0;i<facultyBox.getItemCount();i++)if(facultyBox.getItemAt(i).getFacultyId()==existing.getFacultyId())facultyBox.setSelectedIndex(i);
    JPanel form=new JPanel(new GridLayout(0,2,10,10));form.setBorder(new EmptyBorder(8,4,4,4));
    form.add(new JLabel("Course code *"));form.add(code);form.add(new JLabel("Course name *"));form.add(name);
    form.add(new JLabel("Faculty"));form.add(facultyBox);form.add(new JLabel("Description"));form.add(new JScrollPane(description));
    while(true){
      int choice=JOptionPane.showConfirmDialog(this,form,existing==null?"Add course":"Edit course",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
      if(choice!=JOptionPane.OK_OPTION)return;
      if(code.getText().isBlank()||name.getText().isBlank()){JOptionPane.showMessageDialog(this,"Course code and course name are required.","Check course details",JOptionPane.WARNING_MESSAGE);continue;}
      break;
    }
    FacultyChoice selected=(FacultyChoice)facultyBox.getSelectedItem();Integer facultyId=selected==null||selected.getFacultyId()==0?null:selected.getFacultyId();
    setBusy(true);status.setText(existing==null?"Creating course...":"Saving course changes...");
    new SwingWorker<Void,Void>(){private Exception failure;
      @Override protected Void doInBackground(){try{if(existing==null)repository.create(code.getText(),name.getText(),description.getText(),facultyId);else repository.update(existing,code.getText(),name.getText(),description.getText(),facultyId);return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){setBusy(false);if(failure!=null){showFailure(failure);return;}status.setText(existing==null?"Course created.":"Course updated.");loadCourses();}
    }.execute();
  }

// Purpose: Loads the selected course and opens its edit form.
  private void openSelectedForEdit(){int view=table.getSelectedRow();if(view<0)return;int row=table.convertRowIndexToModel(view);int id=(Integer)model.getValueAt(row,0);
    new SwingWorker<CourseRecord,Void>(){private Exception failure;
      @Override protected CourseRecord doInBackground(){try{for(CourseRecord c:repository.findAll(""))if(c.getCourseId()==id)return c;return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){try{CourseRecord c=get();if(failure!=null)showFailure(failure);else if(c!=null)openForm(c);else loadCourses();}catch(Exception e){showFailure(e);}}
    }.execute();
  }
// Purpose: Confirms and deletes a course and related database rows.
  private void deleteSelected(){int view=table.getSelectedRow();if(view<0)return;int row=table.convertRowIndexToModel(view);int id=(Integer)model.getValueAt(row,0);String courseName=String.valueOf(model.getValueAt(row,2));
    String warning="Delete course '"+courseName+"'? Its enrollments, assignments, attendance, and submissions will also be deleted.";
    int answer=JOptionPane.showConfirmDialog(this,warning,"Delete course",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);if(answer!=JOptionPane.YES_OPTION)return;
    setBusy(true);status.setText("Deleting course...");new SwingWorker<Void,Void>(){private Exception failure;
      @Override protected Void doInBackground(){try{repository.delete(id);return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){setBusy(false);if(failure!=null)showFailure(failure);else{status.setText("Course deleted.");loadCourses();}}
    }.execute();
  }
// Purpose: Enables row actions only when a course is selected.
  private void updateSelectionButtons(){boolean selected=table.getSelectedRow()>=0;if(editButton!=null)editButton.setEnabled(selected);if(deleteButton!=null)deleteButton.setEnabled(selected);}
// Purpose: Disables controls while a database request runs.
  private void setBusy(boolean busy){if(refreshButton!=null)refreshButton.setEnabled(!busy);table.setEnabled(!busy);}
  private String safe(String value){return value==null?"":value;}
// Purpose: Displays course database errors in a dialog.
  private void showFailure(Exception e){String message=e.getMessage()==null?e.toString():e.getMessage();if(message.contains("Duplicate entry"))message="That course code is already in use.";JOptionPane.showMessageDialog(this,message,"Course management error",JOptionPane.ERROR_MESSAGE);status.setText("Could not complete the operation.");}
}














