package com.vclass.ui;

import com.vclass.model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

// Purpose: Builds the main window and role-specific navigation.
public class DashboardFrame extends JFrame {
  private static final Color BG = new Color(246,247,249);
  private static final Color SIDEBAR = Color.WHITE;
  private static final Color PANEL = Color.WHITE;
  private static final Color MUTED = new Color(95,99,104);
  private static final Color ACCENT = new Color(85,89,94);
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel pageHolder = new JPanel(cardLayout);
  private final Map<String,JButton> navButtons = new LinkedHashMap<>();
  private final JLabel pageTitle = new JLabel("Overview");

  public DashboardFrame(User user) {
    setTitle("VCLASS | Dashboard");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(1180,760);
    setMinimumSize(new Dimension(980,650));
    setLocationRelativeTo(null);
    getContentPane().setBackground(BG);
    setLayout(new BorderLayout());
    add(buildSidebar(user),BorderLayout.WEST);
    add(buildMainArea(user),BorderLayout.CENTER);
    showPage("Overview");
  }

// Purpose: Creates the navigation menu and signed-in account section.
  private JPanel buildSidebar(User user) {
    JPanel side=new JPanel(new BorderLayout());
    side.setPreferredSize(new Dimension(245,0));
    side.setBackground(SIDEBAR);
    side.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(218,221,225)));
    JPanel brand=new JPanel();brand.setOpaque(false);brand.setLayout(new BoxLayout(brand,BoxLayout.Y_AXIS));
    brand.setBorder(new EmptyBorder(26,22,22,18));
    JLabel name=label("VCLASS",20,ACCENT,Font.BOLD);name.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel caption=label("VIRTUAL CLASSROOM",10,MUTED,Font.BOLD);caption.setAlignmentX(Component.LEFT_ALIGNMENT);
    brand.add(name);brand.add(Box.createVerticalStrut(4));brand.add(caption);
    side.add(brand,BorderLayout.NORTH);

    JPanel menu=new JPanel();menu.setOpaque(false);menu.setLayout(new BoxLayout(menu,BoxLayout.Y_AXIS));
    menu.setBorder(new EmptyBorder(8,12,12,12));
    for(String item:menuFor(user.getRole())) {
      JButton button=new JButton("  "+item);
      button.setHorizontalAlignment(SwingConstants.LEFT);
      button.setMaximumSize(new Dimension(Integer.MAX_VALUE,43));
      button.setPreferredSize(new Dimension(210,43));
      button.setFont(new Font("Segoe UI",Font.PLAIN,13));
      button.setForeground(new Color(45,49,54));button.setBackground(SIDEBAR);
      button.setBorder(new EmptyBorder(0,12,0,8));button.setFocusPainted(false);button.setContentAreaFilled(false);
      button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      button.addActionListener(e->showPage(item));
      navButtons.put(item,button);menu.add(button);menu.add(Box.createVerticalStrut(4));
    }
    side.add(menu,BorderLayout.CENTER);

    JPanel footer=new JPanel(new BorderLayout(0,10));footer.setOpaque(false);footer.setBorder(new EmptyBorder(12,18,18,18));
    JSeparator separator=new JSeparator();separator.setForeground(new Color(218,221,225));footer.add(separator,BorderLayout.NORTH);
    JPanel who=new JPanel();who.setOpaque(false);who.setLayout(new BoxLayout(who,BoxLayout.Y_AXIS));
    who.add(label(user.getFullName(),13,new Color(32,33,36),Font.BOLD));who.add(Box.createVerticalStrut(3));who.add(label(user.getRole(),11,MUTED,Font.PLAIN));
    footer.add(who,BorderLayout.CENTER);
    JButton logout=new JButton("Sign out");logout.setForeground(new Color(55,59,64));logout.setContentAreaFilled(false);logout.setFocusPainted(false);
    logout.setBorder(new EmptyBorder(8,0,0,0));logout.setHorizontalAlignment(SwingConstants.LEFT);
    logout.addActionListener(e->{dispose();new LoginFrame().setVisible(true);});footer.add(logout,BorderLayout.SOUTH);
    side.add(footer,BorderLayout.SOUTH);
    return side;
  }

// Purpose: Creates the CardLayout area containing dashboard pages.
  private JPanel buildMainArea(User user) {
    JPanel main=new JPanel(new BorderLayout());main.setBackground(BG);
    JPanel top=new JPanel(new BorderLayout());top.setBackground(BG);top.setBorder(new EmptyBorder(25,34,20,34));
    pageTitle.setForeground(new Color(32,33,36));pageTitle.setFont(new Font("Segoe UI",Font.BOLD,24));top.add(pageTitle,BorderLayout.WEST);
    JLabel role=label(user.getRole()+" PORTAL",11,MUTED,Font.BOLD);role.setHorizontalAlignment(SwingConstants.RIGHT);top.add(role,BorderLayout.EAST);
    main.add(top,BorderLayout.NORTH);
    pageHolder.setBackground(BG);pageHolder.setBorder(new EmptyBorder(0,34,30,34));
    pageHolder.add(buildOverview(user),"Overview");
    for(String page:menuFor(user.getRole())) {
      if(page.equals("Overview"))continue;
      if(page.equals("Student Management") && "ADMIN".equalsIgnoreCase(user.getRole()))pageHolder.add(new StudentManagementPanel(),page);
      else if(page.equals("Faculty Management") && "ADMIN".equalsIgnoreCase(user.getRole()))pageHolder.add(new FacultyManagementPanel(),page);
      else if(page.equals("Course Management") && "ADMIN".equalsIgnoreCase(user.getRole()))pageHolder.add(new CourseManagementPanel(),page);
      else if(page.equals("Assignment Management"))pageHolder.add(new AssignmentManagementPanel(user),page);
      // Both student menu entries use the same page: it lists assignments and each student's saved file submissions.
      else if(page.equals("Assignments") || page.equals("My Submissions"))pageHolder.add(new StudentAssignmentsPanel(user),page);
      else pageHolder.add(buildPlaceholder(page),page);
    }
    main.add(pageHolder,BorderLayout.CENTER);
    return main;
  }

// Purpose: Creates the welcome message and overview summary cards.
  private JPanel buildOverview(User user) {
    JPanel page=new JPanel(new BorderLayout(0,22));page.setOpaque(false);
    JPanel welcome=new JPanel();welcome.setOpaque(false);welcome.setLayout(new BoxLayout(welcome,BoxLayout.Y_AXIS));
    welcome.add(label("Good to see you, "+user.getFullName(),20,new Color(32,33,36),Font.BOLD));
    welcome.add(Box.createVerticalStrut(6));welcome.add(label("Here is your VCLASS workspace overview.",13,MUTED,Font.PLAIN));
    page.add(welcome,BorderLayout.NORTH);
    JPanel cards=new JPanel(new GridLayout(2,2,16,16));cards.setOpaque(false);
    cards.add(statCard("YOUR ROLE",user.getRole(),"Signed in account"));
    cards.add(statCard("COURSES","Ready","Course workspace"));
    cards.add(statCard("ASSIGNMENTS","Ready","Assignment workspace"));
    cards.add(statCard("ATTENDANCE","Ready","Attendance workspace"));
    page.add(cards,BorderLayout.CENTER);
    return page;
  }

// Purpose: Builds one overview information card.
  private JPanel statCard(String title,String value,String detail) {
    JPanel card=new JPanel();card.setBackground(PANEL);card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(218,221,225)),new EmptyBorder(20,22,18,18)));
    card.add(label(title,11,MUTED,Font.BOLD));card.add(Box.createVerticalStrut(16));card.add(label(value,22,new Color(32,33,36),Font.BOLD));
    card.add(Box.createVerticalStrut(7));card.add(label(detail,12,MUTED,Font.PLAIN));
    return card;
  }

// Purpose: Shows a simple message for a module not implemented yet.
  private JPanel buildPlaceholder(String module) {
    JPanel page=new JPanel(new GridBagLayout());page.setOpaque(false);
    JPanel box=new JPanel();box.setBackground(PANEL);box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS));
    box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(218,221,225)),new EmptyBorder(28,34,28,34)));
    JLabel title=label(module,20,new Color(32,33,36),Font.BOLD);title.setAlignmentX(Component.CENTER_ALIGNMENT);
    JLabel note=label("This section is ready for the next implementation step.",13,MUTED,Font.PLAIN);note.setAlignmentX(Component.CENTER_ALIGNMENT);
    box.add(title);box.add(Box.createVerticalStrut(10));box.add(note);page.add(box);return page;
  }

// Purpose: Returns the menu labels allowed for the signed-in role.
  private String[] menuFor(String role) {
    if("ADMIN".equalsIgnoreCase(role))return new String[]{"Overview","Student Management","Faculty Management","Course Management","Assignment Management","Attendance Management","Submission Management"};
    if("FACULTY".equalsIgnoreCase(role))return new String[]{"Overview","My Courses","Assignment Management","Attendance Management","Submission Management"};
    return new String[]{"Overview","My Courses","Assignments","My Attendance","My Submissions"};
  }

// Purpose: Shows one CardLayout page and marks its menu button selected.
  private void showPage(String name) {
    pageTitle.setText(name);cardLayout.show(pageHolder,name);
    for(Map.Entry<String,JButton> entry:navButtons.entrySet()) {
      boolean selected=entry.getKey().equals(name);JButton b=entry.getValue();
      b.setOpaque(selected);b.setBackground(selected?new Color(232,234,237):SIDEBAR);
      b.setForeground(selected?new Color(32,33,36):new Color(65,69,73));
      if(selected)b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,3,0,0,ACCENT),new EmptyBorder(0,9,0,8)));
      else b.setBorder(new EmptyBorder(0,12,0,8));
    }
  }

// Purpose: Creates a label with the dashboard font and color.
  private JLabel label(String text,int size,Color color,int style) {
    JLabel l=new JLabel(text);l.setForeground(color);l.setFont(new Font("Segoe UI",style,size));return l;
  }
}















