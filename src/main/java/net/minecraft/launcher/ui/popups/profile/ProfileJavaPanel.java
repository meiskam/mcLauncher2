package net.minecraft.launcher.ui.popups.profile;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.profile.Profile;

public class ProfileJavaPanel extends JPanel
{
  private final ProfileEditorPopup editor;
  private final JCheckBox javaPathCustom = new JCheckBox("Executable:");
  private final JTextField javaPathField = new JTextField();
  private final JCheckBox javaArgsCustom = new JCheckBox("JVM Arguments:");
  private final JTextField javaArgsField = new JTextField();

  public ProfileJavaPanel(ProfileEditorPopup editor) {
    this.editor = editor;

    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createTitledBorder("Java Settings (Advanced)"));

    createInterface();
    fillDefaultValues();
    addEventHandlers();
  }

  protected void createInterface() {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(2, 2, 2, 2);
    constraints.anchor = 17;

    constraints.gridy = 0;

    add(javaPathCustom, constraints);
    constraints.fill = 2;
    constraints.weightx = 1.0D;
    add(javaPathField, constraints);
    constraints.weightx = 0.0D;
    constraints.fill = 0;

    constraints.gridy += 1;

    add(javaArgsCustom, constraints);
    constraints.fill = 2;
    constraints.weightx = 1.0D;
    add(javaArgsField, constraints);
    constraints.weightx = 0.0D;
    constraints.fill = 0;

    constraints.gridy += 1;
  }

  protected void fillDefaultValues() {
    String javaPath = editor.getProfile().getJavaPath();
    if (javaPath != null) {
      javaPathCustom.setSelected(true);
      javaPathField.setText(javaPath);
    } else {
      javaPathCustom.setSelected(false);
      javaPathField.setText(OperatingSystem.getCurrentPlatform().getJavaDir());
    }
    updateJavaPathState();

    String args = editor.getProfile().getJavaArgs();
    if (args != null) {
      javaArgsCustom.setSelected(true);
      javaArgsField.setText(args);
    } else {
      javaArgsCustom.setSelected(false);
      javaArgsField.setText(Profile.DEFAULT_JRE_ARGUMENTS);
    }
    updateJavaArgsState();
  }

  protected void addEventHandlers() {
    javaPathCustom.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileJavaPanel.this.updateJavaPathState();
      }
    });
    javaPathField.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e) {
        ProfileJavaPanel.this.updateJavaPath();
      }

      public void removeUpdate(DocumentEvent e)
      {
        ProfileJavaPanel.this.updateJavaPath();
      }

      public void changedUpdate(DocumentEvent e)
      {
        ProfileJavaPanel.this.updateJavaPath();
      }
    });
    javaArgsCustom.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileJavaPanel.this.updateJavaArgsState();
      }
    });
    javaArgsField.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e) {
        ProfileJavaPanel.this.updateJavaArgs();
      }

      public void removeUpdate(DocumentEvent e)
      {
        ProfileJavaPanel.this.updateJavaArgs();
      }

      public void changedUpdate(DocumentEvent e)
      {
        ProfileJavaPanel.this.updateJavaArgs();
      }
    });
  }

  private void updateJavaPath() {
    if (javaPathCustom.isSelected())
      editor.getProfile().setJavaDir(javaPathField.getText());
    else
      editor.getProfile().setJavaDir(null);
  }

  private void updateJavaPathState()
  {
    if (javaPathCustom.isSelected()) {
      javaPathField.setEnabled(true);
      editor.getProfile().setJavaDir(javaPathField.getText());
    } else {
      javaPathField.setEnabled(false);
      editor.getProfile().setJavaDir(null);
    }
  }

  private void updateJavaArgs() {
    if (javaArgsCustom.isSelected())
      editor.getProfile().setJavaArgs(javaArgsField.getText());
    else
      editor.getProfile().setJavaArgs(null);
  }

  private void updateJavaArgsState()
  {
    if (javaArgsCustom.isSelected()) {
      javaArgsField.setEnabled(true);
      editor.getProfile().setJavaArgs(javaArgsField.getText());
    } else {
      javaArgsField.setEnabled(false);
      editor.getProfile().setJavaArgs(null);
    }
  }
}