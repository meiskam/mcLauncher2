package net.minecraft.launcher;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import net.minecraft.launcher.authentication.OldAuthentication;
import net.minecraft.launcher.authentication.OldAuthentication.StoredDetails;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.LauncherPanel;
import net.minecraft.launcher.ui.SidebarPanel;
import net.minecraft.launcher.ui.sidebar.login.LoginContainerForm;
import net.minecraft.launcher.ui.sidebar.login.NotLoggedInForm;
import net.minecraft.launcher.ui.tabs.ConsoleTab;
import net.minecraft.launcher.ui.tabs.LauncherTabPanel;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.download.DownloadJob;

public class Launcher
{
  private static Launcher instance;
  private static final List<String> delayedSysout = new ArrayList<String>();
  private final VersionManager versionManager;
  private final JFrame frame;
  private final LauncherPanel launcherPanel;
  private final GameLauncher gameLauncher;
  private final File workingDirectory;
  private final Proxy proxy;
  private final PasswordAuthentication proxyAuth;
  private final String[] additionalArgs;
  private final OldAuthentication authentication;
  private final Integer bootstrapVersion;
  private final ProfileManager profileManager;

  public Launcher(JFrame frame, File workingDirectory, Proxy proxy, PasswordAuthentication proxyAuth, String[] args)
  {
    this(frame, workingDirectory, proxy, proxyAuth, args, LauncherConstants.UNVERSIONED_BOOTSTRAP_VERSION);
  }

  public Launcher(JFrame frame, File workingDirectory, Proxy proxy, PasswordAuthentication proxyAuth, String[] args, Integer bootstrapVersion) {
    this.bootstrapVersion = bootstrapVersion;
    instance = this;
    setLookAndFeel();

    this.proxy = proxy;
    this.proxyAuth = proxyAuth;
    additionalArgs = args;
    this.workingDirectory = workingDirectory;
    this.frame = frame;
    gameLauncher = new GameLauncher(this);
    profileManager = new ProfileManager(this);
    authentication = new OldAuthentication(this, proxy);
    versionManager = new VersionManager(new LocalVersionList(workingDirectory), new RemoteVersionList(proxy));
    launcherPanel = new LauncherPanel(this);

    initializeFrame();

    for (String line : delayedSysout) {
      launcherPanel.getTabPanel().getConsole().print(line + "\n");
    }

    if (bootstrapVersion.intValue() < LauncherConstants.MINIMUM_BOOTSTRAP_SUPPORTED) {
      showOutdatedNotice();
      return;
    }

    downloadResources();
    refreshProfiles();
    refreshVersions();

    println("Launcher " + LauncherConstants.VERSION_NAME + " (through bootstrap " + bootstrapVersion + ") started on " + OperatingSystem.getCurrentPlatform().getName() + "...");

    if (!OperatingSystem.getCurrentPlatform().isSupported()) {
      println("This operating system is unknown or unsupported, we cannot guarantee that the game will launch.");
    }
    println("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
    println("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
    println("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
    println("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
    println("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
  }

  private void showOutdatedNotice() {
    String error = "Sorry, but your launcher is outdated! Please redownload it at " + LauncherConstants.URL_BOOTSTRAP_DOWNLOAD;

    frame.getContentPane().removeAll();

    int result = JOptionPane.showOptionDialog(frame, error, "Outdated launcher", 0, 0, null, LauncherConstants.LAUNCHER_OUT_OF_DATE_BUTTONS, LauncherConstants.LAUNCHER_OUT_OF_DATE_BUTTONS[0]);

    if (result == 0) {
      try {
        OperatingSystem.openLink(new URI(LauncherConstants.URL_BOOTSTRAP_DOWNLOAD));
      } catch (URISyntaxException e) {
        println("Couldn't open bootstrap download link", e);
      }
    }
    frame.dispatchEvent(new WindowEvent(frame, 201));
  }

  private static void setLookAndFeel() {
    JFrame frame = new JFrame();
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Throwable ignored) {
      try {
        getInstance().println("Your java failed to provide normal look and feel, trying the old fallback now");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Throwable t) {
        getInstance().println("Unexpected exception setting look and feel");
        t.printStackTrace();
      }
    }
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("test"));
    frame.add(panel);
    try
    {
      frame.pack();
    } catch (Throwable t) {
      getInstance().println("Custom (broken) theme detected, falling back onto x-platform theme");
      try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Throwable ex) {
        getInstance().println("Unexpected exception setting look and feel", ex);
      }
    }

    frame.dispose();
  }

  private void downloadResources() {
    final DownloadJob job = new DownloadJob("Resources", true, gameLauncher);
    gameLauncher.addJob(job);
    versionManager.getExecutorService().submit(new Runnable()
    {
      public void run() {
        try {
          versionManager.downloadResources(job);
          job.startDownloading(versionManager.getExecutorService());
        } catch (IOException e) {
          Launcher.getInstance().println("Unexpected exception queueing resource downloads", e);
        }
      }
    });
  }

  public void refreshVersions() {
    versionManager.getExecutorService().submit(new Runnable()
    {
      public void run() {
        try {
          versionManager.refreshVersions();
        } catch (Throwable e) {
          Launcher.getInstance().println("Unexpected exception refreshing version list", e);
        }
      }
    });
  }

  public void refreshProfiles() {
    versionManager.getExecutorService().submit(new Runnable()
    {
      public void run() {
        try {
          if (!profileManager.loadProfiles()) {
            OldAuthentication.StoredDetails result = authentication.getStoredDetails();

            if (result != null) {
              result = new OldAuthentication.StoredDetails(result.getUsername(), null, result.getDisplayName());
              Profile profile = profileManager.getSelectedProfile();
              profile.setAuthentication(result);
              profileManager.saveProfiles();

              println("Initialized default profile with old lastlogin details");
            } else {
              println("Created default profile with no authentication details");
            }
          } else {
            println("Loaded " + profileManager.getProfiles().size() + " profile(s); selected '" + profileManager.getSelectedProfile().getName() + "'");
          }
        } catch (Throwable e) {
          Launcher.getInstance().println("Unexpected exception refreshing profile list", e);
        }
        try
        {
          Profile profile = profileManager.getSelectedProfile();

          if (profile.getAuthentication() != null) {
            String username = profile.getAuthentication().getUsername();

            if ((username != null) && (username.length() > 0)) {
              NotLoggedInForm form = launcherPanel.getSidebar().getLoginForm().getNotLoggedInForm();
              form.getUsernameField().setText(username);
              String password = authentication.guessPasswordFromSillyOldFormat(username);

              if ((password != null) && (password.length() > 0)) {
                println("Going to log in with legacy stored username & password...");

                form.getPasswordField().setText(password);
                form.tryLogIn(false, false);
              }
            }
          }
        } catch (Throwable e) {
          Launcher.getInstance().println("Unexpected exception logging in with stored credentials", e);
        }
      }
    });
  }

  protected void initializeFrame() {
    frame.getContentPane().removeAll();
    frame.setTitle("Minecraft Launcher " + LauncherConstants.VERSION_NAME);
    frame.setPreferredSize(new Dimension(925, 525));
    frame.setDefaultCloseOperation(2);

    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e) {
        frame.setVisible(false);
        frame.dispose();
        versionManager.getExecutorService().shutdown();
      }
    });
    try
    {
      InputStream in = Launcher.class.getResourceAsStream("/favicon.png");
      if (in != null)
        frame.setIconImage(ImageIO.read(in));
    }
    catch (IOException localIOException)
    {
    }
    frame.add(launcherPanel);

    frame.pack();
    frame.setVisible(true);
  }

  public VersionManager getVersionManager() {
    return versionManager;
  }

  public JFrame getFrame() {
    return frame;
  }

  public LauncherPanel getLauncherPanel() {
    return launcherPanel;
  }

  public GameLauncher getGameLauncher() {
    return gameLauncher;
  }

  public File getWorkingDirectory() {
    return workingDirectory;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public PasswordAuthentication getProxyAuth() {
    return proxyAuth;
  }

  public String[] getAdditionalArgs() {
    return additionalArgs;
  }

  public OldAuthentication getAuthentication() {
    return authentication;
  }

  public void println(String line) {
    System.out.println(line);

    if (launcherPanel == null)
      delayedSysout.add(line);
    else
      launcherPanel.getTabPanel().getConsole().print(line + "\n");
  }

  public void println(String line, Throwable throwable)
  {
    println(line);
    println(throwable);
  }

  public void println(Throwable throwable) {
    StringWriter writer = null;
    PrintWriter printWriter = null;
    String result = throwable.toString();
    try
    {
      writer = new StringWriter();
      printWriter = new PrintWriter(writer);
      throwable.printStackTrace(printWriter);
      result = writer.toString();
    } finally {
      try {
        if (writer != null) writer.close();
        if (printWriter != null) printWriter.close(); 
      }
      catch (IOException localIOException1) {  }

    }
    println(result);
  }

  public int getBootstrapVersion() {
    return bootstrapVersion.intValue();
  }

  public static Launcher getInstance() {
    return instance;
  }

  public ProfileManager getProfileManager() {
    return profileManager;
  }
}