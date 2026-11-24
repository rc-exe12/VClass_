package com.vclass.ui;

import com.vclass.model.User;
import com.vclass.service.AuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

// Purpose: Displays the login form and opens the role dashboard after a valid login.
public class LoginFrame extends JFrame {
  private static final Color BG=new Color(246,247,249), PANEL=Color.WHITE, MUTED=new Color(95,99,104), ACCENT=new Color(85,89,94);
  private final JTextField email=new JTextField("admin@vclass.com",24);
  private final JPasswordField password=new JPasswordField("admin123",24);
  private final JButton signIn=new JButton("Sign in to VCLASS");
  private final AuthService auth=new AuthService();

  public LoginFrame() {
    setTitle("VCLASS | Virtual Classroom"); setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(900,600);
    setMinimumSize(new Dimension(760,520)); setLocationRelativeTo(null); getContentPane().setBackground(BG);
    add(buildContent()); getRootPane().setDefaultButton(signIn);
  }
// Purpose: Places the VCLASS heading, login fields, and sign-in button.
  private JPanel buildContent() {
    JPanel root=new JPanel(new GridBagLayout()); root.setBackground(BG);
    JPanel card=new JPanel(new GridBagLayout()); card.setBackground(PANEL);
    card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(218,221,225)),new EmptyBorder(36,42,32,42)));
    GridBagConstraints c=new GridBagConstraints(); c.gridx=0;c.gridy=0;c.gridwidth=2;c.anchor=GridBagConstraints.WEST;c.insets=new Insets(0,0,8,0);
    card.add(label("VCLASS",15,ACCENT,Font.BOLD),c); c.gridy++;c.insets=new Insets(0,0,6,0);
    card.add(label("Welcome back",30,new Color(32,33,36),Font.BOLD),c); c.gridy++;c.insets=new Insets(0,0,28,0);
    card.add(label("Sign in to your virtual classroom",14,MUTED,Font.PLAIN),c);
    c.gridy++;c.gridwidth=1;c.insets=new Insets(0,0,8,0);card.add(label("Email address",13,new Color(55,59,64),Font.PLAIN),c);
    c.gridy++;c.gridwidth=2;c.fill=GridBagConstraints.HORIZONTAL;c.insets=new Insets(0,0,18,0);styleField(email);email.setPreferredSize(new Dimension(330,44));card.add(email,c);
    c.gridy++;c.insets=new Insets(0,0,8,0);card.add(label("Password",13,new Color(55,59,64),Font.PLAIN),c);
    c.gridy++;c.insets=new Insets(0,0,23,0);styleField(password);password.setPreferredSize(new Dimension(330,44));card.add(password,c);
    c.gridy++;c.insets=new Insets(0,0,12,0);signIn.setPreferredSize(new Dimension(330,46));signIn.setBackground(new Color(232,234,237));signIn.setForeground(new Color(32,33,36));
    signIn.setFont(new Font("Segoe UI",Font.BOLD,14));signIn.setFocusPainted(false);signIn.setBorderPainted(false);
    signIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));signIn.addActionListener(e->attemptLogin());card.add(signIn,c);
    c.gridy++;c.insets=new Insets(2,0,0,0);card.add(label("Demo account: admin@vclass.com  ·  admin123",12,MUTED,Font.PLAIN),c);
    root.add(card);return root;
  }
  private JLabel label(String text,int size,Color color,int style) {
    JLabel l=new JLabel(text);l.setForeground(color);l.setFont(new Font("Segoe UI",style,size));return l;
  }
// Purpose: Applies the standard appearance to an input field.
  private void styleField(JTextField f) {
    f.setBackground(Color.WHITE);f.setForeground(new Color(32,33,36));f.setCaretColor(new Color(32,33,36));f.setFont(new Font("Segoe UI",Font.PLAIN,14));
    f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,208,212)),new EmptyBorder(0,12,0,12)));
  }
// Purpose: Validates the form and starts the database login in a background worker.
  private void attemptLogin() {
    String userEmail=email.getText().trim();char[] secret=password.getPassword();
    if(userEmail.isBlank()||secret.length==0){JOptionPane.showMessageDialog(this,"Enter your email and password.","Missing details",JOptionPane.WARNING_MESSAGE);return;}
    signIn.setEnabled(false);signIn.setText("Signing in...");
    new SwingWorker<User,Void>() {
      private Exception failure;
      protected User doInBackground() {
        try{return auth.login(userEmail,secret);}catch(SQLException|java.io.IOException e){failure=e;return null;}finally{Arrays.fill(secret,(char) 0);}
      }
      protected void done() {
        signIn.setEnabled(true);signIn.setText("Sign in to VCLASS");
        try {
          User u=get();
          if(failure!=null)JOptionPane.showMessageDialog(LoginFrame.this,"Could not connect or sign in. Check database.properties and confirm MySQL is running.\n\nDetails: "+failure.getMessage(),"Database error",JOptionPane.ERROR_MESSAGE);
          else if(u==null)JOptionPane.showMessageDialog(LoginFrame.this,"Email or password is incorrect.","Sign in failed",JOptionPane.WARNING_MESSAGE);
          else { new DashboardFrame(u).setVisible(true); LoginFrame.this.dispose(); }
        }catch(Exception e){JOptionPane.showMessageDialog(LoginFrame.this,e.getMessage(),"Sign in error",JOptionPane.ERROR_MESSAGE);}
      }
    }.execute();
  }
}








