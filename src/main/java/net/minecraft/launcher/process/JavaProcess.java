package net.minecraft.launcher.process;

import java.util.List;

public class JavaProcess
{
  private static final int MAX_SYSOUT_LINES = 5;
  private final List<String> commands;
  private final Process process;
  private final LimitedCapacityList<String> sysOutLines = new LimitedCapacityList<String>(String.class, MAX_SYSOUT_LINES);
  private JavaProcessRunnable onExit;
  private ProcessMonitorThread monitor = new ProcessMonitorThread(this);

  public JavaProcess(List<String> commands, Process process) {
    this.commands = commands;
    this.process = process;

    monitor.start();
  }

  public Process getRawProcess() {
    return process;
  }

  public List<String> getStartupCommands() {
    return commands;
  }

  public String getStartupCommand() {
    return JavaProcessLauncher.buildCommands(commands);
  }

  public LimitedCapacityList<String> getSysOutLines() {
    return sysOutLines;
  }

  public boolean isRunning() {
    try {
      process.exitValue();
    } catch (IllegalThreadStateException ex) {
      return true;
    }

    return false;
  }

  public void setExitRunnable(JavaProcessRunnable runnable) {
    onExit = runnable;
  }

  public void safeSetExitRunnable(JavaProcessRunnable runnable) {
    setExitRunnable(runnable);

    if ((!isRunning()) && 
      (runnable != null))
      runnable.onJavaProcessEnded(this);
  }

  public JavaProcessRunnable getExitRunnable()
  {
    return onExit;
  }

  public int getExitCode() {
    try {
      return process.exitValue();
    } catch (IllegalThreadStateException ex) {
      ex.fillInStackTrace();
      throw ex;
    }
  }

  public String toString()
  {
    return "JavaProcess[commands=" + commands + ", isRunning=" + isRunning() + "]";
  }

  public void stop() {
    process.destroy();
  }
}