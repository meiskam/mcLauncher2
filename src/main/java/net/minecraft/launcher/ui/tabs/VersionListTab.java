package net.minecraft.launcher.ui.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class VersionListTab extends JScrollPane
  implements RefreshedVersionsListener
{
  private static final int COLUMN_NAME = 0;
  private static final int COLUMN_TYPE = 1;
  private static final int COLUMN_RELEASE_DATE = 2;
  private static final int COLUMN_UPDATE_DATE = 3;
  private static final int COLUMN_LIBRARIES = 4;
  private static final int COLUMN_STATUS = 5;
  private static final int NUM_COLUMNS = 6;
  private final Launcher launcher;
  private final VersionTableModel dataModel = new VersionTableModel();
  private final JTable table = new JTable(dataModel);

  public VersionListTab(Launcher launcher) {
    this.launcher = launcher;

    setViewportView(table);
    createInterface();

    launcher.getVersionManager().addRefreshedVersionsListener(this);
  }

  protected void createInterface() {
    table.setFillsViewportHeight(true);
  }

  public Launcher getLauncher() {
    return launcher;
  }

  public void onVersionsRefreshed(VersionManager manager)
  {
    dataModel.setVersions(manager.getLocalVersionList().getVersions());
  }

  public boolean shouldReceiveEventsInUIThread()
  {
    return true;
  }

  private class VersionTableModel extends AbstractTableModel {
    private final List<Version> versions = new ArrayList<Version>();

    private VersionTableModel() {
    }
    public int getRowCount() { return versions.size(); }


    public int getColumnCount()
    {
      return NUM_COLUMNS;
    }

    public Class<?> getColumnClass(int columnIndex)
    {
      if ((columnIndex == COLUMN_UPDATE_DATE) || (columnIndex == COLUMN_RELEASE_DATE)) {
        return Date.class;
      }

      return String.class;
    }

    public String getColumnName(int column)
    {
      switch (column) {
      case COLUMN_UPDATE_DATE:
        return "Last modified";
      case COLUMN_TYPE:
        return "Version type";
      case COLUMN_LIBRARIES:
        return "Library count";
      case COLUMN_NAME:
        return "Version name";
      case COLUMN_STATUS:
        return "Sync status";
      case COLUMN_RELEASE_DATE:
        return "Release Date";
      }
      return super.getColumnName(column);
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
      Version version = (Version)versions.get(rowIndex);

      switch (columnIndex) {
      case COLUMN_NAME:
        return version.getId();
      case COLUMN_UPDATE_DATE:
        return version.getUpdatedTime();
      case COLUMN_LIBRARIES:
        if ((version instanceof CompleteVersion)) {
          CompleteVersion complete = (CompleteVersion)version;
          int total = complete.getLibraries().size();
          int relevant = complete.getRelevantLibraries().size();
          if (total == relevant) {
            return Integer.valueOf(total);
          }
          return String.format("%d (%d relevant to %s)", new Object[] { Integer.valueOf(total), Integer.valueOf(relevant), OperatingSystem.getCurrentPlatform().getName() });
        }

        return "?";
      case COLUMN_STATUS:
        VersionSyncInfo syncInfo = launcher.getVersionManager().getVersionSyncInfo(version);
        if (syncInfo.isOnRemote()) {
          if (syncInfo.isUpToDate()) {
            return "Up to date with remote";
          }
          return "Update avail from remote";
        }

        return "Local only";
      case COLUMN_TYPE:
        return version.getType().getName();
      case COLUMN_RELEASE_DATE:
        return version.getReleaseTime();
      }

      return null;
    }

    public void setVersions(Collection<Version> versions) {
      this.versions.clear();
      this.versions.addAll(versions);
      fireTableDataChanged();
    }
  }
}