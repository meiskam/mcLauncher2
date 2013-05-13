package net.minecraft.launcher.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.ui.tabs.LauncherTabPanel;
import net.minecraft.launcher.ui.tabs.WebsiteTab;

public class LauncherPanel extends JPanel
{
  private final LauncherTabPanel tabPanel;
  private final SidebarPanel sidebar;
  private final JProgressBar progressBar;
  private final Launcher launcher;

  public LauncherPanel(Launcher launcher)
  {
    this.launcher = launcher;

    setLayout(new BorderLayout());

    progressBar = new JProgressBar();
    sidebar = new SidebarPanel(launcher);
    tabPanel = new LauncherTabPanel(launcher);
    createInterface();
  }

  protected void createInterface() {
    tabPanel.getBlog().setPage(LauncherConstants.URL_BLOG);

    progressBar.setVisible(false);
    progressBar.setMinimum(0);
    progressBar.setMaximum(100);

    add(tabPanel, "Center");
    add(sidebar, "East");
    add(progressBar, "South");
  }

  public LauncherTabPanel getTabPanel() {
    return tabPanel;
  }

  public SidebarPanel getSidebar() {
    return sidebar;
  }

  public JProgressBar getProgressBar() {
    return progressBar;
  }

  public Launcher getLauncher() {
    return launcher;
  }
}