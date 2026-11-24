package com.vclass;
import com.vclass.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
// Purpose: Starts the desktop application.
public class Main {
// Purpose: Creates the Swing login window on the Swing event thread.
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
      new LoginFrame().setVisible(true);
    });
  }
}


