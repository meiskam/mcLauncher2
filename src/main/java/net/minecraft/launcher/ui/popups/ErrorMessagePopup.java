package net.minecraft.launcher.ui.popups;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ErrorMessagePopup
  implements Runnable
{
  private final Component component;
  private final String error;

  private ErrorMessagePopup(Component component, String error)
  {
    this.component = component;
    this.error = error;
  }

  public void run()
  {
    JOptionPane.showMessageDialog(component, error);
  }

  public static void show(Component component, String error) {
    SwingUtilities.invokeLater(new ErrorMessagePopup(component, error));
  }
}