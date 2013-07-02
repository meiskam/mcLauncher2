package net.minecraft.launcher.updater.download;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.launcher.Launcher;

public class DownloadJob
{
  private static final int MAX_ATTEMPTS_PER_FILE = 5;
  private static final int ASSUMED_AVERAGE_FILE_SIZE = 5242880;
  private final Queue<Downloadable> remainingFiles = new ConcurrentLinkedQueue<Downloadable>();
  private final List<Downloadable> allFiles = Collections.synchronizedList(new ArrayList<Downloadable>());
  private final List<Downloadable> failures = Collections.synchronizedList(new ArrayList<Downloadable>());
  private final List<Downloadable> successful = Collections.synchronizedList(new ArrayList<Downloadable>());
  private final List<ProgressContainer> progressContainers = Collections.synchronizedList(new ArrayList<ProgressContainer>());
  private final DownloadListener listener;
  private final String name;
  private final boolean ignoreFailures;
  private final AtomicInteger remainingThreads = new AtomicInteger();
  private boolean started;

  public DownloadJob(String name, boolean ignoreFailures, DownloadListener listener, Collection<Downloadable> files)
  {
    this.name = name;
    this.ignoreFailures = ignoreFailures;
    this.listener = listener;
    if (files != null) addDownloadables(files); 
  }

  public DownloadJob(String name, boolean ignoreFailures, DownloadListener listener)
  {
    this(name, ignoreFailures, listener, null);
  }

  public void addDownloadables(Collection<Downloadable> downloadables) {
    if (started) throw new IllegalStateException("Cannot add to download job that has already started");

    allFiles.addAll(downloadables);
    remainingFiles.addAll(downloadables);

    for (Downloadable downloadable : downloadables) {
      progressContainers.add(downloadable.getMonitor());
      if (downloadable.getExpectedSize() == 0L)
        downloadable.getMonitor().setTotal(ASSUMED_AVERAGE_FILE_SIZE);
      else {
        downloadable.getMonitor().setTotal(downloadable.getExpectedSize());
      }
      downloadable.getMonitor().setJob(this);
    }
  }

  public void addDownloadables(Downloadable[] downloadables) {
    if (started) throw new IllegalStateException("Cannot add to download job that has already started");

    for (Downloadable downloadable : downloadables) {
      allFiles.add(downloadable);
      remainingFiles.add(downloadable);
      progressContainers.add(downloadable.getMonitor());
      if (downloadable.getExpectedSize() == 0L)
        downloadable.getMonitor().setTotal(ASSUMED_AVERAGE_FILE_SIZE);
      else {
        downloadable.getMonitor().setTotal(downloadable.getExpectedSize());
      }
      downloadable.getMonitor().setJob(this);
    }
  }

  public void startDownloading(ThreadPoolExecutor executorService) {
    if (started) throw new IllegalStateException("Cannot start download job that has already started");
    started = true;

    if (allFiles.isEmpty()) {
      Launcher.getInstance().println("Download job '" + name + "' skipped as there are no files to download");
      listener.onDownloadJobFinished(this);
    } else {
      int threads = executorService.getMaximumPoolSize();
      remainingThreads.set(threads);
      Launcher.getInstance().println("Download job '" + name + "' started (" + threads + " threads, " + allFiles.size() + " files)");
      for (int i = 0; i < threads; i++)
        executorService.submit(new Runnable()
        {
          public void run() {
            DownloadJob.this.popAndDownload();
          }
        });
    }
  }

  private void popAndDownload()
  {
    Downloadable downloadable;
    while ((downloadable = remainingFiles.poll()) != null) {
      if (downloadable.getNumAttempts() > MAX_ATTEMPTS_PER_FILE) {
        if (!ignoreFailures) failures.add(downloadable);
        Launcher.getInstance().println("Gave up trying to download " + downloadable.getUrl() + " for job '" + name + "'");
      }
      else
      {
        try {
          String result = downloadable.download();
          successful.add(downloadable);
          Launcher.getInstance().println("Finished downloading " + downloadable.getTarget() + " for job '" + name + "'" + ": " + result);
        } catch (Throwable t) {
          Launcher.getInstance().println("Couldn't download " + downloadable.getUrl() + " for job '" + name + "'", t);
          remainingFiles.add(downloadable);
        }
      }
    }
    if (remainingThreads.decrementAndGet() <= 0)
      listener.onDownloadJobFinished(this);
  }

  public boolean shouldIgnoreFailures()
  {
    return ignoreFailures;
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isComplete() {
    return (started) && (remainingFiles.isEmpty()) && (remainingThreads.get() == 0);
  }

  public int getFailures() {
    return failures.size();
  }

  public int getSuccessful() {
    return successful.size();
  }

  public String getName() {
    return name;
  }

  public void updateProgress() {
    listener.onDownloadJobProgressChanged(this);
  }

  public float getProgress() {
    float current = 0.0F;
    float total = 0.0F;

    synchronized (progressContainers) {
      for (ProgressContainer progress : progressContainers) {
        total += (float)progress.getTotal();
        current += (float)progress.getCurrent();
      }
    }

    float result = -1.0F;
    if (total > 0.0F) result = current / total;
    return result;
  }
}