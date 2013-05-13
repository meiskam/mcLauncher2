package net.minecraft.launcher.ui.sidebar;

import javax.swing.JLabel;

public class BetaNoticeForm extends SidebarForm
{
  public BetaNoticeForm()
  {
    super("NOTICE");

    createInterface();
  }

  protected void createInterface()
  {
    JLabel label = new JLabel();

    label.setText("<html><div width='175px'><p style='color: red; font-weight: bold'>This launcher is in early development.</p><p>It is nowhere near complete, may be very broken, and will be changed a lot over the next few weeks.</p><p>Please take any bugs, missing features or oddities you see with a grain of redstone.</p></div></html>");

    add(label);
  }
}