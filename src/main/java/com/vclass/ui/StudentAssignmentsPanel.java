package com.vclass.ui;

import com.vclass.model.AssignmentRecord;
import com.vclass.model.User;
import com.vclass.service.AssignmentRepository;
import com.vclass.service.SubmissionFileStorage;
import com.vclass.service.SubmissionRepository;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

// Purpose: Student screen for viewing assignments and submitting local files.
public class StudentAssignmentsPanel extends JPanel {
  private static final Color MUTED=new Color(95,99,104);
  private final User user;private final AssignmentRepository assignments=new AssignmentRepository();private final SubmissionRepository submissions=new SubmissionRepository();
  private final DefaultTableModel model=new DefaultTableModel(new Object[]{"ID","Course","Assignment","Due date","Max marks","Submission status","File","Stored path"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
  private final JTable table=new JTable(model);private final JTextField search=new JTextField(20);private final JTextField comments=new JTextField(28);
  private final JLabel selectedFileLabel=new JLabel("No file attached");private final JLabel status=new JLabel(" ");private JButton refreshButton;
  private File selectedFile;

  public StudentAssignmentsPanel(User user){this.user=user;setLayout(new BorderLayout(0,14));setOpaque(false);add(buildToolbar(),BorderLayout.NORTH);add(buildTable(),BorderLayout.CENTER);
    status.setForeground(MUTED);status.setFont(new Font("Segoe UI",Font.PLAIN,12));add(status,BorderLayout.SOUTH);
search.addActionListener(e->load());load();}
// Purpose: Creates search, file chooser, comment, submit, and open controls.
  private JPanel buildToolbar(){
    JPanel wrap=new JPanel(new BorderLayout(0,12));wrap.setOpaque(false);JPanel heading=new JPanel();heading.setOpaque(false);heading.setLayout(new BoxLayout(heading,BoxLayout.Y_AXIS));
    JLabel title=new JLabel("Assignments");title.setForeground(new Color(32,33,36));title.setFont(new Font("Segoe UI",Font.BOLD,19));
    JLabel subtitle=new JLabel("Choose an assignment, attach your work, and submit it. All file types are allowed up to 25 MB.");subtitle.setForeground(MUTED);subtitle.setFont(new Font("Segoe UI",Font.PLAIN,12));heading.add(title);heading.add(Box.createVerticalStrut(5));heading.add(subtitle);wrap.add(heading,BorderLayout.NORTH);
    JPanel controls=new JPanel();controls.setOpaque(false);controls.setLayout(new BoxLayout(controls,BoxLayout.Y_AXIS));
    JPanel searchRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));searchRow.setOpaque(false);search.setPreferredSize(new Dimension(210,36));styleField(search);searchRow.add(new JLabel("Search"));searchRow.add(search);
    JButton go=button("Search");go.addActionListener(e->load());searchRow.add(go);refreshButton=button("Refresh");refreshButton.addActionListener(e->load());searchRow.add(refreshButton);controls.add(searchRow);
    JPanel fileRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));fileRow.setOpaque(false);JButton choose=button("Attach file from computer...");choose.addActionListener(e->chooseFile());fileRow.add(choose);selectedFileLabel.setForeground(MUTED);fileRow.add(selectedFileLabel);JButton clear=button("Clear");clear.addActionListener(e->clearSelectedFile());fileRow.add(clear);controls.add(fileRow);
    JPanel submitRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));submitRow.setOpaque(false);submitRow.add(new JLabel("Comment (optional)"));comments.setPreferredSize(new Dimension(240,32));styleField(comments);submitRow.add(comments);
    JButton submit=button("Submit / Replace file");submit.addActionListener(e->submitFile());submitRow.add(submit);JButton open=button("Open submitted file");open.addActionListener(e->openSubmittedFile());submitRow.add(open);controls.add(submitRow);
    wrap.add(controls,BorderLayout.CENTER);return wrap;
  }
// Purpose: Builds the assignment JTable and hides internal database columns.
  private JScrollPane buildTable(){table.setRowHeight(36);table.setFont(new Font("Segoe UI",Font.PLAIN,13));table.setForeground(new Color(32,33,36));table.setBackground(Color.WHITE);table.setGridColor(new Color(218,221,225));table.setSelectionBackground(new Color(225,228,232));table.setSelectionForeground(new Color(32,33,36));table.setFillsViewportHeight(true);table.setAutoCreateRowSorter(true);table.setShowVerticalLines(false);table.getTableHeader().setReorderingAllowed(false);table.getTableHeader().setBackground(new Color(245,246,248));table.getTableHeader().setForeground(new Color(55,59,64));table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));table.getColumnModel().getColumn(0).setMinWidth(0);table.getColumnModel().getColumn(0).setMaxWidth(0);table.getColumnModel().getColumn(0).setWidth(0);table.getColumnModel().getColumn(7).setMinWidth(0);table.getColumnModel().getColumn(7).setMaxWidth(0);table.getColumnModel().getColumn(7).setWidth(0);JScrollPane scroll=new JScrollPane(table);scroll.getViewport().setBackground(Color.WHITE);scroll.setBorder(BorderFactory.createLineBorder(new Color(218,221,225)));return scroll;}
// Purpose: Creates a consistently styled student action button.
  private JButton button(String text){JButton b=new JButton(text);b.setFont(new Font("Segoe UI",Font.BOLD,12));b.setBackground(new Color(245,246,248));b.setForeground(new Color(45,49,54));b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(8,12,8,12)));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
// Purpose: Applies the standard appearance to a text field.
  private void styleField(JTextField f){f.setBackground(Color.WHITE);f.setForeground(new Color(32,33,36));f.setCaretColor(new Color(32,33,36));f.setFont(new Font("Segoe UI",Font.PLAIN,13));f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(0,10,0,10)));}
// Purpose: Loads assignments and the students saved submission details.
  private void load(){String q=search.getText().trim();busy(true);status.setText("Loading assignments...");new SwingWorker<List<AssignmentRecord>,Void>(){Exception failure;
    @Override protected List<AssignmentRecord> doInBackground(){try{return assignments.findForStudent(user.getId(),q);}catch(Exception e){failure=e;return null;}}
    @Override protected void done(){busy(false);try{List<AssignmentRecord> rows=get();if(failure!=null){fail(failure);return;}model.setRowCount(0);for(AssignmentRecord a:rows)model.addRow(new Object[]{a.getAssignmentId(),a.getCourseCode()+" — "+a.getCourseName(),a.getTitle(),a.getDueAt(),a.getMaxMarks(),a.getStatus()==null?"Not submitted":a.getStatus(),fileName(a.getFilePath()),a.getFilePath()});status.setText(rows.size()+" assignment(s)");}catch(Exception e){fail(e);}}
  }.execute();}
// Purpose: Opens a local file picker and stores the chosen file for upload.
  private void chooseFile(){JFileChooser chooser=new JFileChooser();chooser.setDialogTitle("Select a file to upload as your assignment submission");chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);chooser.setMultiSelectionEnabled(false);chooser.setAcceptAllFileFilterUsed(true);if(chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){File chosen=chooser.getSelectedFile();if(chosen.length()>SubmissionFileStorage.MAX_FILE_SIZE){JOptionPane.showMessageDialog(this,"Choose a file smaller than 25 MB.","File too large",JOptionPane.WARNING_MESSAGE);return;}selectedFile=chosen;long mb=(selectedFile.length()+1024*1024-1)/(1024*1024);selectedFileLabel.setText("Attached: "+selectedFile.getName()+" ("+mb+" MB)");status.setText("File selected. Select an assignment, then click Submit / Replace file.");}}
// Purpose: Clears the selected local file before it is uploaded.
  private void clearSelectedFile(){selectedFile=null;selectedFileLabel.setText("No file attached");}
// Purpose: Validates the selected assignment and file, then submits in the background.
  private void submitFile(){int view=table.getSelectedRow();if(view<0){JOptionPane.showMessageDialog(this,"Select an assignment first.","No assignment selected",JOptionPane.INFORMATION_MESSAGE);return;}if(selectedFile==null){JOptionPane.showMessageDialog(this,"Choose a file to submit.","No file selected",JOptionPane.INFORMATION_MESSAGE);return;}
    if(selectedFile.length()>SubmissionFileStorage.MAX_FILE_SIZE){JOptionPane.showMessageDialog(this,"Choose a file smaller than 25 MB.","File too large",JOptionPane.WARNING_MESSAGE);return;}
    int row=table.convertRowIndexToModel(view),assignmentId=(Integer)model.getValueAt(row,0);String title=String.valueOf(model.getValueAt(row,2));File source=selectedFile;String note=comments.getText();
    int confirm=JOptionPane.showConfirmDialog(this,"Submit '"+source.getName()+"' for "+title+"?","Confirm submission",JOptionPane.YES_NO_OPTION);if(confirm!=JOptionPane.YES_OPTION)return;
    busy(true);status.setText("Copying file and saving submission...");new SwingWorker<Void,Void>(){Exception failure;
      @Override protected Void doInBackground(){try{submissions.submit(user.getId(),assignmentId,source,note);return null;}catch(Exception e){failure=e;return null;}}
      @Override protected void done(){busy(false);if(failure!=null){fail(failure);return;}selectedFile=null;selectedFileLabel.setText("No file attached");comments.setText("");status.setText("File uploaded and submission saved.");load();}}
    .execute();
  }
// Purpose: Opens the students saved submission using the computers default application.
  private void openSubmittedFile(){int view=table.getSelectedRow();if(view<0){JOptionPane.showMessageDialog(this,"Select an assignment first.","No assignment selected",JOptionPane.INFORMATION_MESSAGE);return;}int row=table.convertRowIndexToModel(view);int id=(Integer)model.getValueAt(row,0);String path=null;
    path=(String)model.getValueAt(row,7);if(path==null||path.isBlank()){JOptionPane.showMessageDialog(this,"You have not submitted a file for this assignment yet.","No submission file",JOptionPane.INFORMATION_MESSAGE);return;}
    File file=SubmissionFileStorage.resolve(path);if(!file.exists()){JOptionPane.showMessageDialog(this,"The saved file cannot be found on this computer.","File missing",JOptionPane.ERROR_MESSAGE);return;}
    try{if(!Desktop.isDesktopSupported())throw new UnsupportedOperationException("This computer cannot open files from the app.");Desktop.getDesktop().open(file);}catch(Exception e){fail(e);}
  }
// Purpose: Removes the generated unique prefix to display the original filename.
  private String fileName(String path){if(path==null||path.isBlank())return "—";return new File(path).getName().replaceFirst("^[0-9a-fA-F-]{36}_","");}
// Purpose: Disables controls while a file/database request runs.
  private void busy(boolean value){if(refreshButton!=null)refreshButton.setEnabled(!value);table.setEnabled(!value);}
// Purpose: Shows upload, file, or database errors.
  private void fail(Exception e){String message=e.getMessage()==null?e.toString():e.getMessage();JOptionPane.showMessageDialog(this,message,"Submission error",JOptionPane.ERROR_MESSAGE);status.setText("Could not complete the operation.");}
}
















