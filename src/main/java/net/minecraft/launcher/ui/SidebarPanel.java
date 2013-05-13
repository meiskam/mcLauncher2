package net.minecraft.launcher.ui;

import java.awt.Dimension;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.ui.sidebar.BetaNoticeForm;
import net.minecraft.launcher.ui.sidebar.StatusPanelForm;
import net.minecraft.launcher.ui.sidebar.VersionSelection;
import net.minecraft.launcher.ui.sidebar.login.LoginContainerForm;

public class SidebarPanel extends JPanel
{
  private final Launcher launcher;
  private final LoginContainerForm loginForm;
  private final VersionSelection versionSelection;
  private final StatusPanelForm serverStatus;

  public SidebarPanel(Launcher launcher)
  {
    this.launcher = launcher;

    setPreferredSize(new Dimension(250, 1));

    int border = 4;
    setBorder(new EmptyBorder(border, border, border, border));

    loginForm = new LoginContainerForm(launcher);
    versionSelection = new VersionSelection(launcher);
    serverStatus = new StatusPanelForm(launcher);

    createInterface();
  }

  protected void createInterface() {
    setLayout(new BoxLayout(this, 1));
    add(versionSelection);
    add(new BetaNoticeForm());
    add(serverStatus);

    add(new Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(2147483647, 2147483647)));

    add(loginForm);
  }

  public LoginContainerForm getLoginForm() {
    return loginForm;
  }

  public VersionSelection getVersionSelection() {
    return versionSelection;
  }

  public Launcher getLauncher() {
    return launcher;
  }
}