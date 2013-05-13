package net.minecraft.launcher.ui.sidebar;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public abstract class SidebarForm extends JPanel
{
  public SidebarForm(String name)
  {
    setBorder(BorderFactory.createTitledBorder(name));
  }

  protected abstract void createInterface();
}