package net.minecraft.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import net.minecraft.launcher.authentication.OldAuthentication;
import net.minecraft.launcher.authentication.OldAuthentication.Response;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessRunnable;
import net.minecraft.launcher.process.LimitedCapacityList;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.LauncherPanel;
import net.minecraft.launcher.ui.SidebarPanel;
import net.minecraft.launcher.ui.sidebar.login.LoginContainerForm;
import net.minecraft.launcher.ui.tabs.CrashReportTab;
import net.minecraft.launcher.ui.tabs.LauncherTabPanel;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.updater.download.DownloadJob;
import net.minecraft.launcher.updater.download.DownloadListener;
import net.minecraft.launcher.updater.download.Downloadable;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ExtractRules;
import net.minecraft.launcher.versions.Library;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

public class GameLauncher
  implements JavaProcessRunnable, DownloadListener
{
  private final Object lock = new Object();
  private final Launcher launcher;
  private final List<DownloadJob> jobs = new ArrayList<DownloadJob>();
  private CompleteVersion version;
  private boolean isWorking = false;
  private File nativeDir = null;

  public GameLauncher(Launcher launcher) {
    this.launcher = launcher;
  }

  private void setWorking(boolean working) {
    synchronized (lock) {
      if (nativeDir != null) {
        Launcher.getInstance().println("Deleting " + nativeDir);
        if ((!nativeDir.isDirectory()) || (FileUtils.deleteQuietly(nativeDir))) {
          nativeDir = null;
        } else {
          Launcher.getInstance().println("Couldn't delete " + nativeDir + " - scheduling for deletion upon exit");
          try {
            FileUtils.forceDeleteOnExit(nativeDir);
          } catch (Throwable localThrowable) {
          }
        }
      }
      isWorking = working;
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run() {
          launcher.getLauncherPanel().getSidebar().getLoginForm().checkLoginState();
        }
      });
    }
  }

  public boolean isWorking() {
    return isWorking;
  }

  public void playGame() {
    synchronized (lock) {
      if (isWorking) {
        launcher.println("Tried to play game but game is already starting!");
        return;
      }

      setWorking(true);
    }

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run() {
        launcher.getLauncherPanel().getTabPanel().showConsole();
      }
    });
    launcher.println("Getting syncinfo for selected version");

    String lastVersionId = launcher.getProfileManager().getSelectedProfile().getLastVersionId();
    VersionSyncInfo syncInfo;
    if (lastVersionId != null)
      syncInfo = launcher.getVersionManager().getVersionSyncInfo(lastVersionId);
    else {
      syncInfo = (VersionSyncInfo)launcher.getVersionManager().getVersions().get(0);
    }

    if (syncInfo == null) {
      Launcher.getInstance().println("Tried to launch a version without a version being selected...");
      setWorking(false);
      return;
    }

    synchronized (lock) {
      launcher.println("Queueing library & version downloads");
      try
      {
        version = launcher.getVersionManager().getLatestCompleteVersion(syncInfo);
      } catch (IOException e) {
        Launcher.getInstance().println("Couldn't get complete version info for " + syncInfo.getLatestVersion(), e);
        setWorking(false);
        return;
      }

      if (version.getMinimumLauncherVersion() > LauncherConstants.VERSION_NUMERIC) {
        Launcher.getInstance().println("An update to your launcher is available and is required to play " + version.getId() + ". Please restart your launcher.");
        setWorking(false);
        return;
      }

      if (!syncInfo.isInstalled()) {
        try {
          VersionList localVersionList = launcher.getVersionManager().getLocalVersionList();
          if ((localVersionList instanceof LocalVersionList)) {
            ((LocalVersionList)localVersionList).saveVersion(version);
            Launcher.getInstance().println("Installed " + syncInfo.getLatestVersion());
          }
        } catch (IOException e) {
          Launcher.getInstance().println("Couldn't save version info to install " + syncInfo.getLatestVersion(), e);
          setWorking(false);
          return;
        }
      }
      try
      {
        DownloadJob job = new DownloadJob("Version & Libraries", false, this);
        addJob(job);
        launcher.getVersionManager().downloadVersion(syncInfo, job);
        job.startDownloading(launcher.getVersionManager().getExecutorService());
      } catch (IOException e) {
        Launcher.getInstance().println("Couldn't get version info for " + syncInfo.getLatestVersion(), e);
        setWorking(false);
        return;
      }
    }
  }

  protected void launchGame() {
    launcher.println("Launching game");
    Profile selectedProfile = launcher.getProfileManager().getSelectedProfile();

    nativeDir = new File(launcher.getWorkingDirectory(), "versions/" + version.getId() + "/" + version.getId() + "-natives-" + System.nanoTime());
    if (!nativeDir.isDirectory()) nativeDir.mkdirs();
    launcher.println("Unpacking natives to " + nativeDir);
    try {
      unpackNatives(version, nativeDir);
    } catch (IOException e) {
      Launcher.getInstance().println("Couldn't unpack natives!", e);
      return;
    }

    File gameDirectory = selectedProfile.getGameDir() == null ? launcher.getWorkingDirectory() : selectedProfile.getGameDir();
    Launcher.getInstance().println("Launching in " + gameDirectory);
    JavaProcessLauncher processLauncher = new JavaProcessLauncher(selectedProfile.getJavaPath(), new String[0]);
    processLauncher.directory(gameDirectory);

    if (OperatingSystem.getCurrentPlatform().equals(OperatingSystem.OSX)) {
      processLauncher.addCommands(new String[] { "-Xdock:icon=assets/icons/minecraft.icns", "-Xdock:name=Minecraft" });
    }

    String profileArgs = selectedProfile.getJavaArgs();

    if (profileArgs != null)
      processLauncher.addSplitCommands(profileArgs);
    else {
      processLauncher.addSplitCommands(Profile.DEFAULT_JRE_ARGUMENTS);
    }

    processLauncher.addCommands(new String[] { "-Djava.library.path=" + nativeDir.getAbsolutePath() });
    processLauncher.addCommands(new String[] { "-cp", constructClassPath(version) });
    processLauncher.addCommands(new String[] { version.getMainClass() });

    String[] args = getMinecraftArguments(version, launcher.getAuthentication().getLastSuccessfulResponse(), selectedProfile, gameDirectory, new File(launcher.getWorkingDirectory(), "assets"));
    if (args == null) return;
    processLauncher.addCommands(args);

    Proxy proxy = launcher.getProxy();
    PasswordAuthentication proxyAuth = launcher.getProxyAuth();
    if (!proxy.equals(Proxy.NO_PROXY)) {
      InetSocketAddress address = (InetSocketAddress)proxy.address();
      processLauncher.addCommands(new String[] { "--proxyHost", address.getHostName() });
      processLauncher.addCommands(new String[] { "--proxyPort", Integer.toString(address.getPort()) });
      if (proxyAuth != null) {
        processLauncher.addCommands(new String[] { "--proxyUser", proxyAuth.getUserName() });
        processLauncher.addCommands(new String[] { "--proxyPass", new String(proxyAuth.getPassword()) });
      }

    }

    processLauncher.addCommands(launcher.getAdditionalArgs());
    try
    {
      List<String> parts = processLauncher.getFullCommands();
      StringBuilder full = new StringBuilder();
      boolean first = true;

      for (String part : parts) {
        if (!first) full.append(" ");
        full.append(part);
        first = false;
      }

      Launcher.getInstance().println("Running " + full.toString());
      JavaProcess process = processLauncher.start();
      process.safeSetExitRunnable(this);
      Launcher.getInstance().println("---- YOU CAN CLOSE THIS LAUNCHER IF THE GAME STARTED OK ----");
      Launcher.getInstance().println("---- YOU CAN CLOSE THIS LAUNCHER IF THE GAME STARTED OK ----");
      Launcher.getInstance().println("---- YOU CAN CLOSE THIS LAUNCHER IF THE GAME STARTED OK ----");
      Launcher.getInstance().println("---- (We'll do this automatically later ;D) ----");
    } catch (IOException e) {
      Launcher.getInstance().println("Couldn't launch game", e);
      setWorking(false);
      return;
    }
  }

  private String[] getMinecraftArguments(CompleteVersion version, OldAuthentication.Response loginResponse, Profile selectedProfile, File gameDirectory, File assetsDirectory) {
    if (version.getMinecraftArguments() == null) {
      Launcher.getInstance().println("Can't run version, missing minecraftArguments");
      setWorking(false);
      return null;
    }

    Map<String, String> map = new HashMap<String, String>();
    StrSubstitutor substitutor = new StrSubstitutor(map);
    String[] split = version.getMinecraftArguments().split(" ");

    map.put("auth_username", loginResponse.getUsername());
    map.put("auth_player_name", loginResponse.getPlayerName());
    map.put("auth_uuid", loginResponse.getUUID());
    map.put("auth_session", loginResponse.getSessionId());

    map.put("profile_name", selectedProfile.getName());
    map.put("version_name", version.getId());

    map.put("game_directory", gameDirectory.getAbsolutePath());
    map.put("game_assets", assetsDirectory.getAbsolutePath());

    for (int i = 0; i < split.length; i++) {
      split[i] = substitutor.replace(split[i]);
    }

    return split;
  }

  private void unpackNatives(CompleteVersion version, File targetDir) throws IOException {
    OperatingSystem os = OperatingSystem.getCurrentPlatform();
    Collection<Library> libraries = version.getRelevantLibraries(os);

    for (Library library : libraries) {
      Map<OperatingSystem, String> nativesPerOs = library.getNatives();

      if ((nativesPerOs != null) && (nativesPerOs.get(os) != null)) {
        File file = new File(launcher.getWorkingDirectory(), library.getArtifactPath((String)nativesPerOs.get(os)));
        ZipFile zip = new ZipFile(file);
        ExtractRules extractRules = library.getExtractRules();
        try
        {
          Enumeration<? extends ZipEntry> entries = zip.entries();

          while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)entries.nextElement();

            if ((extractRules == null) || (extractRules.shouldExtract(entry.getName())))
            {
              File targetFile = new File(targetDir, entry.getName());
              if (targetFile.getParentFile() != null) targetFile.getParentFile().mkdirs();

              if (!entry.isDirectory()) {
                BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));

                byte[] buffer = new byte[2048];
                FileOutputStream outputStream = new FileOutputStream(targetFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                try
                {
                  int length;
                  while ((length = inputStream.read(buffer, 0, buffer.length)) != -1)
                    bufferedOutputStream.write(buffer, 0, length);
                }
                finally {
                  Downloadable.closeSilently(bufferedOutputStream);
                  Downloadable.closeSilently(outputStream);
                  Downloadable.closeSilently(inputStream);
                }
              }
            }
          }
        } finally { zip.close(); }
      }
    }
  }

  private String constructClassPath(CompleteVersion version)
  {
    StringBuilder result = new StringBuilder();
    Collection<File> classPath = version.getClassPath(OperatingSystem.getCurrentPlatform(), launcher.getWorkingDirectory());
    String separator = System.getProperty("path.separator");

    for (File file : classPath) {
      if (!file.isFile()) throw new RuntimeException("Classpath file not found: " + file);
      if (result.length() > 0) result.append(separator);
      result.append(file.getAbsolutePath());
    }

    return result.toString();
  }

  public void onJavaProcessEnded(JavaProcess process)
  {
    int exitCode = process.getExitCode();

    if (exitCode == 0) {
      Launcher.getInstance().println("Game ended with no troubles detected (exit code " + exitCode + ")");
    } else {
      Launcher.getInstance().println("Game ended with bad state (exit code " + exitCode + ")");

      String errorText = null;
      String[] sysOut = (String[])process.getSysOutLines().getItems();

      for (int i = sysOut.length - 1; i >= 0; i--) {
        String line = sysOut[i];
        String crashIdentifier = "#@!@#";
        int pos = line.lastIndexOf(crashIdentifier);

        if ((pos >= 0) && (pos < line.length() - crashIdentifier.length() - 1)) {
          errorText = line.substring(pos + crashIdentifier.length()).trim();
          break;
        }
      }

      if (errorText != null) {
        File file = new File(errorText);

        if (file.isFile()) {
          Launcher.getInstance().println("Crash report detected, opening: " + errorText);
          InputStream inputStream = null;
          try
          {
            inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
              if (result.length() > 0) result.append("\n");
              result.append(line);
            }

            reader.close();

            launcher.getLauncherPanel().getTabPanel().setCrashReport(new CrashReportTab(version, file, result.toString()));
          } catch (IOException e) {
            Launcher.getInstance().println("Couldn't open crash report", e);
          } finally {
            Downloadable.closeSilently(inputStream);
          }
        } else {
          Launcher.getInstance().println("Crash report detected, but unknown format: " + errorText);
        }
      }
    }

    setWorking(false);
  }

  public void onDownloadJobFinished(DownloadJob job)
  {
    updateProgressBar();
    synchronized (lock) {
      if (job.getFailures() > 0) {
        launcher.println("Job '" + job.getName() + "' finished with " + job.getFailures() + " failure(s)!");
        setWorking(false);
      } else {
        launcher.println("Job '" + job.getName() + "' finished successfully");

        if ((isWorking()) && (!hasRemainingJobs()))
          try {
            launchGame();
          } catch (Throwable ex) {
            Launcher.getInstance().println("Fatal error launching game. Report this to http://mojang.atlassian.net please!", ex);
          }
      }
    }
  }

  public void onDownloadJobProgressChanged(DownloadJob job)
  {
    updateProgressBar();
  }

  protected void updateProgressBar() {
    final float progress = getProgress();
    final boolean hasTasks = hasRemainingJobs();

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run() {
        launcher.getLauncherPanel().getProgressBar().setVisible(hasTasks);
        launcher.getLauncherPanel().getProgressBar().setValue((int)(progress * 100.0F));
      }
    });
  }

  protected float getProgress() {
    synchronized (lock) {
      float max = jobs.size();
      float result = 0.0F;

      for (DownloadJob job : jobs) {
        result += job.getProgress();
      }

      return result / max;
    }
  }

  public boolean hasRemainingJobs() {
    synchronized (lock) {
      for (DownloadJob job : jobs) {
        if (!job.isComplete()) return true;
      }
    }

    return false;
  }

  public void addJob(DownloadJob job) {
    synchronized (lock) {
      jobs.add(job);
    }
  }
}