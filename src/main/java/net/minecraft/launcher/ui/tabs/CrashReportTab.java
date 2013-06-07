package net.minecraft.launcher.ui.tabs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;

public class CrashReportTab extends JPanel
{
  private final CompleteVersion version;
  private final File reportFile;
  private final String report;
  private final JEditorPane reportEditor = new JEditorPane();
  private final JScrollPane scrollPane = new JScrollPane(reportEditor);
  private final CrashInfoPane crashInfoPane = new CrashInfoPane();
  private final boolean isModded;

  public CrashReportTab(CompleteVersion version, File reportFile, String report)
  {
    super(true);
    this.version = version;
    this.reportFile = reportFile;
    this.report = report;

    if ((report.contains("Is Modded: Probably not")) || (report.contains("Is Modded: Unknown")))
      isModded = (!report.contains("Suspicious classes: No suspicious classes found."));
    else {
      isModded = true;
    }

    setLayout(new BorderLayout());
    createInterface();
  }

  protected void createInterface() {
    add(crashInfoPane, "North");
    add(scrollPane, "Center");

    reportEditor.setText(report);

    crashInfoPane.createInterface();
  }

  private class CrashInfoPane extends JPanel
    implements ActionListener
  {
    public static final String INFO_NORMAL = "<html><div style='width: 100%'><p><b>Uhoh, it looks like the game has crashed! Sorry for the inconvenience :(</b></p><p>Using magic and love, we've managed to gather some details about the crash and we can hopefully try to fix it if you report it to us.</p><p>You can see the full report below, and we'd really appreciate it if you send it to us to take a look at.</p></div></html>";
    public static final String INFO_MODDED = "<html><div style='width: 100%'><p><b>Uhoh, it looks like the game has crashed! Sorry for the inconvenience :(</b></p><p>We think your game may be modded, and as such we can't accept this crash report.</p><p>However, if you do indeed use mods, please send this to the mod authors to take a look at!</p></div></html>";
    private final JButton submitButton = new JButton("Report to Mojang");
    private final JButton openFileButton = new JButton("Open report file");

    protected CrashInfoPane() {
      submitButton.addActionListener(this);
      openFileButton.addActionListener(this);
    }

    protected void createInterface() {
      setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();

      constraints.anchor = 13;
      constraints.fill = 2;
      constraints.insets = new Insets(2, 2, 2, 2);

      constraints.gridx = 1;
      add(submitButton, constraints);
      constraints.gridy = 1;
      add(openFileButton, constraints);

      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.weightx = 1.0D;
      constraints.weighty = 1.0D;
      constraints.gridheight = 2;
      add(new JLabel(isModded ? INFO_MODDED : INFO_NORMAL), constraints);

      if (isModded)
        submitButton.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e)
    {
      if (e.getSource() == submitButton)
        try {
          Map<String, Object> args = new HashMap<String, Object>();

          args.put("pid", Integer.valueOf(10400));
          args.put("issuetype", Integer.valueOf(1));
          args.put("description", "Put the summary of the bug you're having here\n\n*What I expected to happen was...:*\nDescribe what you thought should happen here\n\n*What actually happened was...:*\nDescribe what happened here\n\n*Steps to Reproduce:*\n1. Put a step by step guide on how to trigger the bug here\n2. ...\n3. ...");

          args.put("environment", buildEnvironmentInfo());

          OperatingSystem.openLink(URI.create("https://mojang.atlassian.net/secure/CreateIssueDetails!init.jspa?" + Http.buildQuery(args)));
        } catch (Throwable ex) {
          Launcher.getInstance().println("Couldn't open bugtracker", ex);
        }
      else if (e.getSource() == openFileButton)
        OperatingSystem.openLink(reportFile.toURI());
    }

    private String buildEnvironmentInfo()
    {
      StringBuilder result = new StringBuilder();

      result.append("OS: ");
      result.append(System.getProperty("os.name"));
      result.append(" (ver ");
      result.append(System.getProperty("os.version"));
      result.append(", arch ");
      result.append(System.getProperty("os.arch"));
      result.append(")\nJava: ");
      result.append(System.getProperty("java.version"));
      result.append(" (by ");
      result.append(System.getProperty("java.vendor"));
      result.append(")\nLauncher: ");
      result.append(LauncherConstants.VERSION_NAME);
      result.append(" (bootstrap ");
      result.append(Launcher.getInstance().getBootstrapVersion());
      result.append(")\nMinecraft: ");
      result.append(version.getId());
      result.append(" (updated ");
      result.append(version.getUpdatedTime());
      result.append(")");

      return result.toString();
    }
  }
}