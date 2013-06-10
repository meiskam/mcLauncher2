package net.minecraft.launcher.ui.popups.profile;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
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

  public ProfileInfoPanel(ProfileEditorPopup editor) {
    this.editor = editor;

    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createTitledBorder("Profile Info"));

    createInterface();
    fillDefaultValues();
    addEventHandlers();

    List<VersionSyncInfo> versions = editor.getLauncher().getVersionManager().getVersions();

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

    add(new JLabel("Use version:"), constraints);
    constraints.fill = 2;
    constraints.weightx = 1.0D;
    add(versionList, constraints);
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
    List<VersionSyncInfo> versions = manager.getVersions();
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