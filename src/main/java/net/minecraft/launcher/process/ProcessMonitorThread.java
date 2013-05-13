package net.minecraft.launcher.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.launcher.Launcher;

public class ProcessMonitorThread extends Thread
{
  private final JavaProcess process;

  public ProcessMonitorThread(JavaProcess process)
  {
    this.process = process;
  }

  public void run()
  {
    InputStreamReader reader = new InputStreamReader(process.getRawProcess().getInputStream());
    BufferedReader buf = new BufferedReader(reader);
    String line = null;

    while (process.isRunning()) {
      try {
        while ((line = buf.readLine()) != null) {
          Launcher.getInstance().println("Client> " + line);
          process.getSysOutLines().add(line);
        }
      } catch (IOException ex) {
        Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
        try {
          buf.close();
        } catch (IOException ex) {
          Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }

    JavaProcessRunnable onExit = process.getExitRunnable();

    if (onExit != null)
      onExit.onJavaProcessEnded(process);
  }
}