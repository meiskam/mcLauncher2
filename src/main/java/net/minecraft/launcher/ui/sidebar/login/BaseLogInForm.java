package net.minecraft.launcher.ui.sidebar.login;

import java.awt.event.ActionListener;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.ui.sidebar.SidebarGridForm;
import net.minecraft.launcher.updater.VersionManager;

public abstract class BaseLogInForm extends SidebarGridForm
  implements ActionListener, RefreshedVersionsListener
{
  private final LoginContainerForm container;
  private final Launcher launcher;

  public BaseLogInForm(LoginContainerForm container, String name)
  {
    super(name);
    this.container = container;
    launcher = container.getLauncher();
    launcher.getVersionManager().addRefreshedVersionsListener(this);
  }

  public abstract void checkLoginState();

  public void onVersionsRefreshed(VersionManager manager)
  {
    checkLoginState();
  }

  public boolean shouldReceiveEventsInUIThread()
  {
    return true;
  }

  public LoginContainerForm getLoginContainer() {
    return container;
  }

  public Launcher getLauncher() {
    return launcher;
  }
}