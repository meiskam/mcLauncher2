package net.minecraft.launcher.ui.sidebar.login;

import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;

public class LoginContainerForm extends JPanel
{
  private static final String CARD_LOGGED_IN = "Logged In";
  private static final String CARD_NOT_LOGGED_IN = "Not Logged In";
  private final Launcher launcher;
  private final LoggedInForm loggedInForm;
  private final NotLoggedInForm notLoggedInForm;
  private final CardLayout layout = new CardLayout();

  public LoginContainerForm(Launcher launcher) {
    super(true);
    this.launcher = launcher;
    loggedInForm = new LoggedInForm(this);
    notLoggedInForm = new NotLoggedInForm(this);
    setMaximumSize(new Dimension(2147483647, 300));

    setLayout(layout);
    add(loggedInForm, CARD_LOGGED_IN);
    add(notLoggedInForm, CARD_NOT_LOGGED_IN);

    layout.show(this, CARD_NOT_LOGGED_IN);
  }

  public void checkLoginState() {
    AuthenticationService authentication = launcher.getProfileManager().getSelectedProfile().getAuthentication();

    notLoggedInForm.checkLoginState();
    loggedInForm.checkLoginState();

    if (authentication.isLoggedIn())
      layout.show(this, CARD_LOGGED_IN);
    else
      layout.show(this, CARD_NOT_LOGGED_IN);
  }

  public NotLoggedInForm getNotLoggedInForm()
  {
    return notLoggedInForm;
  }

  public Launcher getLauncher() {
    return launcher;
  }
}