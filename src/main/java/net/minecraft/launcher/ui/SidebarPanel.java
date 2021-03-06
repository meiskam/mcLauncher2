package net.minecraft.launcher.ui;

import java.awt.Dimension;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.ui.sidebar.ProfileSelection;
import net.minecraft.launcher.ui.sidebar.StatusPanelForm;
import net.minecraft.launcher.ui.sidebar.login.LoginContainerForm;

public class SidebarPanel extends JPanel
{
  private final Launcher launcher;
  private final LoginContainerForm loginForm;
  private final ProfileSelection profileSelection;
  private final StatusPanelForm serverStatus;

  public SidebarPanel(Launcher launcher)
  {
    this.launcher = launcher;

    int border = 4;
    setBorder(new EmptyBorder(border, border, border, border));

    loginForm = new LoginContainerForm(launcher);
    profileSelection = new ProfileSelection(launcher);
    serverStatus = new StatusPanelForm(launcher);

    createInterface();
  }

  protected void createInterface() {
    setLayout(new BoxLayout(this, 1));
    add(profileSelection);
    add(serverStatus);

    add(new Filler(new Dimension(0, 0), new Dimension(0, 32767), new Dimension(0, 32767)));

    add(loginForm);
  }

  public LoginContainerForm getLoginForm() {
    return loginForm;
  }

  public Launcher getLauncher() {
    return launcher;
  }
}