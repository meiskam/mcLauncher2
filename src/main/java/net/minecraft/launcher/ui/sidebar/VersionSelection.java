package net.minecraft.launcher.ui.sidebar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.updater.events.RefreshedVersionsListener;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class VersionSelection extends SidebarGridForm
  implements ItemListener, RefreshedVersionsListener
{
  private final JComboBox versionList = new JComboBox();
  private final JLabel infoLabel = new JLabel();
  private final Launcher launcher;

  public VersionSelection(Launcher launcher)
  {
    super("Version Selection");
    this.launcher = launcher;
    setMaximumSize(new Dimension(2147483647, 300));

    versionList.setRenderer(new VersionListRenderer());
    versionList.addItemListener(this);

    versionList.addItem("Loading versions...");

    createInterface();

    launcher.getVersionManager().addRefreshedVersionsListener(this);
  }

  protected void populateGrid(GridBagConstraints constraints)
  {
    add(new JLabel("Version:", 4), constraints, 0, 0, 0, 1);
    add(versionList, constraints, 1, 0, 1, 1);

    add(new JLabel("Status:", 4), constraints, 0, 1, 0, 1);
    add(infoLabel, constraints, 1, 1, 1, 1);
  }

  public JComboBox getVersionList() {
    return versionList;
  }

  public void onVersionsRefreshed(VersionManager manager)
  {
    List<VersionSyncInfo> versions = manager.getVersions();
    Collections.sort(versions, new Comparator<VersionSyncInfo>()
    {
      public int compare(VersionSyncInfo a, VersionSyncInfo b) {
        Version aVer = a.getLatestVersion();
        Version bVer = b.getLatestVersion();

        if ((aVer.getReleaseTime() != null) && (bVer.getReleaseTime() != null)) {
          return bVer.getReleaseTime().compareTo(aVer.getReleaseTime());
        }
        return bVer.getUpdatedTime().compareTo(aVer.getUpdatedTime());
      }
    });
    populateVersions(versions);
  }

  public boolean shouldReceiveEventsInUIThread()
  {
    return true;
  }

  public VersionSyncInfo getSelectedVersion() {
    if ((versionList.getSelectedItem() instanceof VersionSyncInfo)) {
      return (VersionSyncInfo)versionList.getSelectedItem();
    }
    return null;
  }

  public void populateVersions(List<VersionSyncInfo> versions) {
    VersionSyncInfo previous = getSelectedVersion();
    versionList.removeAllItems();
    VersionSyncInfo selected = null;

    for (VersionSyncInfo version : versions) {
      if ((previous != null) && (version.getLatestVersion().getId().equals(previous.getLatestVersion().getId()))) {
        selected = version;
      }

      versionList.addItem(version);
    }

    if ((selected == null) && (!versions.isEmpty())) {
      selected = (VersionSyncInfo)versions.get(0);
    }

    versionList.setSelectedItem(selected);
  }

  public void itemStateChanged(ItemEvent e)
  {
    if (e.getItem() == null) {
      infoLabel.setText("");
    } else if ((e.getItem() instanceof VersionSyncInfo)) {
      VersionSyncInfo syncInfo = (VersionSyncInfo)e.getItem();

      if (syncInfo.isInstalled()) {
        if (syncInfo.isUpToDate())
          infoLabel.setText("Up to date.");
        else
          infoLabel.setText("<html><b>Update available!</b></html>");
      }
      else
        infoLabel.setText("<html><b>Will be installed.</b></html>");
    }
  }

  public Launcher getLauncher()
  {
    return launcher;
  }

  private static class VersionListRenderer extends BasicComboBoxRenderer
  {
    private final Font uninstalledFont;
    private final Font updatableFont;

    private VersionListRenderer()
    {
      uninstalledFont = new Font(getFont().getName(), 2, getFont().getSize());
      updatableFont = new Font(getFont().getName(), 1, getFont().getSize());
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      if ((value instanceof VersionSyncInfo)) {
        VersionSyncInfo syncInfo = (VersionSyncInfo)value;
        Version version = syncInfo.getLatestVersion();

        super.getListCellRendererComponent(list, String.format("%s %s", new Object[] { version.getType().getName(), version.getId() }), index, isSelected, cellHasFocus);

        if (syncInfo.isInstalled()) {
          if (!syncInfo.isUpToDate())
            setFont(updatableFont);
        }
        else
          setFont(uninstalledFont);
      }
      else {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }

      return this;
    }
  }
}