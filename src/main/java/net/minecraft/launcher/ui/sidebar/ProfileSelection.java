package net.minecraft.launcher.ui.sidebar;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.events.RefreshedProfilesListener;
import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.popups.profile.ProfileEditorPopup;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class ProfileSelection extends SidebarGridForm
  implements ActionListener, ItemListener, RefreshedProfilesListener, RefreshedVersionsListener
{
  private final JComboBox profileList = new JComboBox();
  private final JLabel versionLabel = new JLabel();
  private final JLabel statusLabel = new JLabel();
  private final JButton newProfileButton = new JButton("New Profile");
  private final JButton editProfileButton = new JButton("Edit Profile");
  private final Launcher launcher;
  private boolean skipSelectionUpdate;

  public ProfileSelection(Launcher launcher)
  {
    super("Profile Selection");
    this.launcher = launcher;

    profileList.setRenderer(new ProfileListRenderer());
    profileList.addItemListener(this);
    profileList.addItem("Loading profiles...");

    newProfileButton.addActionListener(this);
    editProfileButton.addActionListener(this);

    createInterface();

    launcher.getProfileManager().addRefreshedProfilesListener(this);
    launcher.getVersionManager().addRefreshedVersionsListener(this);
  }

  protected void populateGrid(GridBagConstraints constraints)
  {
    constraints.fill = 2;
    add(new JLabel("Profile:", 4), constraints, 0, 0, 0, 1);
    add(profileList, constraints, 1, 0, 1, 1);

    add(new JLabel("Version:", 4), constraints, 0, 1, 0, 1);
    add(versionLabel, constraints, 1, 1, 1, 1);

    add(new JLabel("Status:", 4), constraints, 0, 2, 0, 1);
    add(statusLabel, constraints, 1, 2, 1, 1);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 2, 5, 0));
    buttonPanel.add(newProfileButton);
    buttonPanel.add(editProfileButton);

    add(buttonPanel, constraints, 0, 3, 0, 0);
  }

  public void onVersionsRefreshed(VersionManager manager)
  {
    updateProfileStatus();
  }

  public void onProfilesRefreshed(ProfileManager manager)
  {
    populateProfiles();
  }

  public boolean shouldReceiveEventsInUIThread()
  {
    return true;
  }

  public void populateProfiles() {
    String previous = launcher.getProfileManager().getSelectedProfile().getName();
    Profile selected = null;
    Collection<Profile> profiles = launcher.getProfileManager().getProfiles().values();
    profileList.removeAllItems();

    skipSelectionUpdate = true;

    for (Profile profile : profiles) {
      if (previous.equals(profile.getName())) {
        selected = profile;
      }

      profileList.addItem(profile);
    }

    if (selected == null) {
      if (profiles.isEmpty()) {
        selected = launcher.getProfileManager().getSelectedProfile();
        profileList.addItem(selected);
      }

      selected = (Profile)profiles.iterator().next();
    }

    skipSelectionUpdate = false;

    profileList.setSelectedItem(selected);
  }

  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() != 1) return;

    if ((!skipSelectionUpdate) && ((e.getItem() instanceof Profile))) {
      Profile profile = (Profile)e.getItem();
      launcher.getProfileManager().setSelectedProfile(profile.getName());
    }

    updateProfileStatus();
  }

  private void updateProfileStatus() {
    String id = launcher.getProfileManager().getSelectedProfile().getLastVersionId();
    VersionSyncInfo syncInfo = null;

    if (id != null) {
      syncInfo = launcher.getVersionManager().getVersionSyncInfo(id);
    }

    if (syncInfo == null) {
      List<VersionSyncInfo> versions = launcher.getVersionManager().getVersions(launcher.getProfileManager().getSelectedProfile().getVersionFilter());
      if (!versions.isEmpty()) {
        syncInfo = (VersionSyncInfo)versions.get(0);
      }
    }

    if ((syncInfo == null) || (syncInfo.getLatestVersion() == null)) {
      versionLabel.setText("??? (Latest Version)");
      statusLabel.setText("Updating profile & version list...");
    } else {
      Version version = syncInfo.getLatestVersion();
      versionLabel.setText(version.getType().getName() + " " + version.getId());

      if (syncInfo.isInstalled()) {
        if (syncInfo.isUpToDate())
          statusLabel.setText("Up to date.");
        else
          statusLabel.setText("<html><b>Will be updated.</b></html>");
      }
      else
        statusLabel.setText("<html><b>Will be installed.</b></html>");
    }
  }

  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == newProfileButton) {
      Profile profile = new Profile();
      profile.setName("New Profile");

      while (launcher.getProfileManager().getProfiles().containsKey(profile.getName())) {
        profile.setName(profile.getName() + "_");
      }

      ProfileEditorPopup.showEditProfileDialog(getLauncher(), profile);
    } else if (e.getSource() == editProfileButton) {
      ProfileEditorPopup.showEditProfileDialog(getLauncher(), launcher.getProfileManager().getSelectedProfile());
    }
  }

  public Launcher getLauncher()
  {
    return launcher;
  }

  private static class ProfileListRenderer extends BasicComboBoxRenderer
  {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      if ((value instanceof Profile)) {
        value = ((Profile)value).getName();
      }

      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      return this;
    }
  }
}