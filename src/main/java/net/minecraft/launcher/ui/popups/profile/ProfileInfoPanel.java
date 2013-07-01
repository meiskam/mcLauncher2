package net.minecraft.launcher.ui.popups.profile;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.Document;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.Profile.Resolution;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class ProfileInfoPanel extends JPanel
  implements RefreshedVersionsListener
{
  private final ProfileEditorPopup editor;
  private final JCheckBox gameDirCustom = new JCheckBox("Game Directory:");
  private final JTextField profileName = new JTextField();
  private final JTextField gameDirField = new JTextField();
  private final JComboBox versionList = new JComboBox();
  private final JCheckBox resolutionCustom = new JCheckBox("Resolution:");
  private final JTextField resolutionWidth = new JTextField();
  private final JTextField resolutionHeight = new JTextField();
  private final JCheckBox allowSnapshots = new JCheckBox("Enable experimental development versions (\"snapshots\")");

  public ProfileInfoPanel(ProfileEditorPopup editor) {
    this.editor = editor;

    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createTitledBorder("Profile Info"));

    createInterface();
    fillDefaultValues();
    addEventHandlers();

    List<VersionSyncInfo> versions = editor.getLauncher().getVersionManager().getVersions(editor.getProfile().getVersionFilter());

    if (versions.isEmpty())
      editor.getLauncher().getVersionManager().addRefreshedVersionsListener(this);
    else
      populateVersions(versions);
  }

  protected void createInterface()
  {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(2, 2, 2, 2);
    constraints.anchor = 17;

    constraints.gridy = 0;

    add(new JLabel("Profile Name:"), constraints);
    constraints.fill = 2;
    constraints.weightx = 1.0D;
    add(profileName, constraints);
    constraints.weightx = 0.0D;
    constraints.fill = 0;

    constraints.gridy += 1;

    add(gameDirCustom, constraints);
    constraints.fill = 2;
    constraints.weightx = 1.0D;
    add(gameDirField, constraints);
    constraints.weightx = 0.0D;
    constraints.fill = 0;

    constraints.gridy += 1;

    constraints.fill = 2;
    constraints.weightx = 1.0D;
    constraints.gridwidth = 0;
    add(allowSnapshots, constraints);
    constraints.gridwidth = 1;
    constraints.weightx = 0.0D;
    constraints.fill = 0;

    constraints.gridy += 1;

    add(new JLabel("Use version:"), constraints);
    constraints.fill = 2;
    constraints.weightx = 1.0D;
    add(versionList, constraints);
    constraints.weightx = 0.0D;
    constraints.fill = 0;

    constraints.gridy += 1;

    JPanel resolutionPanel = new JPanel();
    resolutionPanel.setLayout(new BoxLayout(resolutionPanel, 0));
    resolutionPanel.add(resolutionWidth);
    resolutionPanel.add(Box.createHorizontalStrut(5));
    resolutionPanel.add(new JLabel("x"));
    resolutionPanel.add(Box.createHorizontalStrut(5));
    resolutionPanel.add(resolutionHeight);

    add(resolutionCustom, constraints);
    constraints.fill = 2;
    constraints.weightx = 1.0D;
    add(resolutionPanel, constraints);
    constraints.weightx = 0.0D;
    constraints.fill = 0;

    constraints.gridy += 1;

    versionList.setRenderer(new VersionListRenderer());
  }

  protected void fillDefaultValues() {
    profileName.setText(editor.getProfile().getName());

    File gameDir = editor.getProfile().getGameDir();
    if (gameDir != null) {
      gameDirCustom.setSelected(true);
      gameDirField.setText(gameDir.getAbsolutePath());
    } else {
      gameDirCustom.setSelected(false);
      gameDirField.setText(editor.getLauncher().getWorkingDirectory().getAbsolutePath());
    }
    updateGameDirState();

    Profile.Resolution resolution = editor.getProfile().getResolution();
    resolutionCustom.setSelected(resolution != null);
    if (resolution == null) resolution = Profile.DEFAULT_RESOLUTION;
    resolutionWidth.setText(String.valueOf(resolution.getWidth()));
    resolutionHeight.setText(String.valueOf(resolution.getHeight()));
    updateResolutionState();

    allowSnapshots.setSelected(editor.getProfile().getVersionFilter().getTypes().contains(ReleaseType.SNAPSHOT));
  }

  protected void addEventHandlers() {
    profileName.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e) {
        ProfileInfoPanel.this.updateProfileName();
      }

      public void removeUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateProfileName();
      }

      public void changedUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateProfileName();
      }
    });
    gameDirCustom.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileInfoPanel.this.updateGameDirState();
      }
    });
    gameDirField.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e) {
        ProfileInfoPanel.this.updateGameDir();
      }

      public void removeUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateGameDir();
      }

      public void changedUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateGameDir();
      }
    });
    versionList.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileInfoPanel.this.updateVersionSelection();
      }
    });
    resolutionCustom.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileInfoPanel.this.updateResolutionState();
      }
    });
    DocumentListener resolutionListener = new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e) {
        ProfileInfoPanel.this.updateResolution();
      }

      public void removeUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateResolution();
      }

      public void changedUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateResolution();
      }
    };
    resolutionWidth.getDocument().addDocumentListener(resolutionListener);
    resolutionHeight.getDocument().addDocumentListener(resolutionListener);

    allowSnapshots.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileInfoPanel.this.updateCustomVersionFilter();
      }
    });
  }

  private void updateCustomVersionFilter() {
    Profile profile = editor.getProfile();

    if (allowSnapshots.isSelected()) {
      if (profile.getAllowedReleaseTypes() == null) {
        profile.setAllowedReleaseTypes(new HashSet<ReleaseType>(Profile.DEFAULT_RELEASE_TYPES));
      }

      profile.getAllowedReleaseTypes().add(ReleaseType.SNAPSHOT);
    } else if (profile.getAllowedReleaseTypes() != null) {
      profile.getAllowedReleaseTypes().remove(ReleaseType.SNAPSHOT);

      if (profile.getAllowedReleaseTypes().equals(Profile.DEFAULT_RELEASE_TYPES)) {
        profile.setAllowedReleaseTypes(null);
      }
    }

    populateVersions(editor.getLauncher().getVersionManager().getVersions(editor.getProfile().getVersionFilter()));
    editor.getLauncher().getVersionManager().removeRefreshedVersionsListener(this);
  }

  private void updateProfileName() {
    if (profileName.getText().length() > 0)
      editor.getProfile().setName(profileName.getText());
  }

  private void updateGameDirState()
  {
    if (gameDirCustom.isSelected()) {
      gameDirField.setEnabled(true);
      editor.getProfile().setGameDir(new File(gameDirField.getText()));
    } else {
      gameDirField.setEnabled(false);
      editor.getProfile().setGameDir(null);
    }
  }

  private void updateResolutionState() {
    if (resolutionCustom.isSelected()) {
      resolutionWidth.setEnabled(true);
      resolutionHeight.setEnabled(true);
      updateResolution();
    } else {
      resolutionWidth.setEnabled(false);
      resolutionHeight.setEnabled(false);
      editor.getProfile().setResolution(null);
    }
  }

  private void updateResolution() {
    try {
      int width = Integer.parseInt(resolutionWidth.getText());
      int height = Integer.parseInt(resolutionHeight.getText());

      editor.getProfile().setResolution(new Profile.Resolution(width, height));
    } catch (NumberFormatException ignored) {
      editor.getProfile().setResolution(null);
    }
  }

  private void updateVersionSelection() {
    Object selection = versionList.getSelectedItem();

    if ((selection instanceof VersionSyncInfo)) {
      Version version = ((VersionSyncInfo)selection).getLatestVersion();
      editor.getProfile().setLastVersionId(version.getId());
    } else {
      editor.getProfile().setLastVersionId(null);
    }
  }

  private void populateVersions(List<VersionSyncInfo> versions) {
    String previous = editor.getProfile().getLastVersionId();
    VersionSyncInfo selected = null;

    versionList.removeAllItems();
    versionList.addItem("Use Latest Version");

    for (VersionSyncInfo version : versions) {
      if (version.getLatestVersion().getId().equals(previous)) {
        selected = version;
      }

      versionList.addItem(version);
    }

    if ((selected == null) && (!versions.isEmpty()))
      versionList.setSelectedIndex(0);
    else
      versionList.setSelectedItem(selected);
  }

  public void onVersionsRefreshed(VersionManager manager)
  {
    List<VersionSyncInfo> versions = manager.getVersions(editor.getProfile().getVersionFilter());
    populateVersions(versions);
    editor.getLauncher().getVersionManager().removeRefreshedVersionsListener(this);
  }

  public boolean shouldReceiveEventsInUIThread()
  {
    return true;
  }

  private void updateGameDir() {
    File file = new File(gameDirField.getText());
    editor.getProfile().setGameDir(file);
  }

  private static class VersionListRenderer extends BasicComboBoxRenderer
  {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      if ((value instanceof VersionSyncInfo)) {
        VersionSyncInfo syncInfo = (VersionSyncInfo)value;
        Version version = syncInfo.getLatestVersion();

        value = String.format("%s %s", new Object[] { version.getType().getName(), version.getId() });
      }

      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      return this;
    }
  }
}