package net.minecraft.launcher.updater.download;

public class ProgressContainer
{
  private long total;
  private long current;
  private DownloadJob job;

  public DownloadJob getJob()
  {
    return job;
  }

  public void setJob(DownloadJob job) {
    this.job = job;
    if (job != null) job.updateProgress(); 
  }

  public long getTotal()
  {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
    if (job != null) job.updateProgress(); 
  }

  public long getCurrent()
  {
    return current;
  }

  public void setCurrent(long current) {
    this.current = current;
    if (current > total) total = current;
    if (job != null) job.updateProgress(); 
  }

  public void addProgress(long amount)
  {
    setCurrent(getCurrent() + amount);
  }

  public float getProgress() {
    if (total == 0L) return 0.0F;
    return (float)current / (float)total;
  }
}