package net.minecraft.launcher.ui.tabs;

import java.awt.Component;
import javax.swing.JTabbedPane;
import net.minecraft.launcher.Launcher;

public class LauncherTabPanel extends JTabbedPane
{
  private final Launcher launcher;
  private final WebsiteTab blog;
  private final ConsoleTab console;
  private CrashReportTab crashReportTab;

  public LauncherTabPanel(Launcher launcher)
  {
    super(1);

    this.launcher = launcher;
    blog = new WebsiteTab(launcher);
    console = new ConsoleTab(launcher);

    createInterface();
  }

  protected void createInterface() {
    addTab("Update Notes", blog);
    addTab("Development Console", console);
    addTab("Local Version Editor (NYI)", new VersionListTab(launcher));
  }

  public Launcher getLauncher() {
    return launcher;
  }

  public WebsiteTab getBlog() {
    return blog;
  }

  public ConsoleTab getConsole() {
    return console;
  }

  public void showConsole() {
    setSelectedComponent(console);
  }

  public void setCrashReport(CrashReportTab newTab) {
    if (crashReportTab != null) removeTab(crashReportTab);
    crashReportTab = newTab;
    addTab("Crash Report", crashReportTab);
    setSelectedComponent(newTab);
  }

  protected void removeTab(Component tab) {
    for (int i = 0; i < getTabCount(); i++)
      if (getTabComponentAt(i) == tab) {
        removeTabAt(i);
        break;
      }
  }
}