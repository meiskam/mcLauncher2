package net.minecraft.launcher.ui.sidebar.login;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.minecraft.launcher.GameLauncher;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.authentication.OldAuthentication;
import net.minecraft.launcher.authentication.OldAuthentication.Response;
import net.minecraft.launcher.authentication.OldAuthentication.StoredDetails;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.popups.ErrorMessagePopup;
import net.minecraft.launcher.updater.VersionManager;

public class NotLoggedInForm extends BaseLogInForm
{
  private final JTextField usernameField = new JTextField(20);
  private final JPasswordField passwordField = new JPasswordField(20);
  private final JButton playButton = new JButton("Play");
  private final JButton registerButton = new JButton("Register");
  private final JCheckBox rememberMeCheckbox = new JCheckBox("Log me in automatically");

  public NotLoggedInForm(LoginContainerForm container) {
    super(container, "Log In");
    setMaximumSize(new Dimension(2147483647, 300));
    createInterface();
  }

  protected void populateGrid(GridBagConstraints constraints) {
    constraints.fill = 2;

    JLabel usernameLabel = new JLabel("Username:", 2);
    usernameLabel.setLabelFor(usernameField);
    add(usernameLabel, constraints, 0, 0, 0, 1);
    add(usernameField, constraints, 1, 0, 1, 0);

    JLabel passwordLabel = new JLabel("Password:", 2);
    passwordLabel.setLabelFor(passwordField);
    add(passwordLabel, constraints, 0, 1, 0, 1);
    add(passwordField, constraints, 1, 1, 1, 0);

    ((JCheckBox)add(rememberMeCheckbox, constraints, 0, 2, 0, 2)).setEnabled(false);

    playButton.addActionListener(this);
    usernameField.addActionListener(this);
    passwordField.addActionListener(this);
    registerButton.addActionListener(this);
    registerButton.setDefaultCapable(false);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 2));
    buttonPanel.add(registerButton);
    buttonPanel.add(playButton);

    playButton.setFont(new Font(playButton.getFont().getName(), 1, playButton.getFont().getSize()));

    add(buttonPanel, constraints, 0, 3, 0, 0);
  }

  public JCheckBox getRememberMeCheckbox() {
    return rememberMeCheckbox;
  }

  public JTextField getUsernameField() {
    return usernameField;
  }

  public JPasswordField getPasswordField() {
    return passwordField;
  }

  public JButton getPlayButton() {
    return playButton;
  }

  public JButton getRegisterButton() {
    return registerButton;
  }

  public void checkLoginState()
  {
    boolean canLogIn = true;

    if (getLauncher().getGameLauncher().isWorking()) canLogIn = false;
    if (getLauncher().getVersionManager().getVersions().size() <= 0) canLogIn = false;
    if (getLauncher().getAuthentication().isAuthenticating()) canLogIn = false;

    playButton.setEnabled(canLogIn);
  }

  public void actionPerformed(ActionEvent e)
  {
    OldAuthentication authentication = getLauncher().getAuthentication();

    if ((e.getSource() == playButton) || (e.getSource() == usernameField) || (e.getSource() == passwordField)) {
      Response response = authentication.getLastSuccessfulResponse();

      if (response != null)
        getLauncher().getGameLauncher().playGame();
      else
        tryLogIn(true, true);
    }
    else {
      try {
        OperatingSystem.openLink(new URI(LauncherConstants.URL_REGISTER));
      }
      catch (URISyntaxException localURISyntaxException) {
      }
    }
  }

  public void onProfilesRefreshed(ProfileManager manager) {
    Profile profile = manager.getSelectedProfile();

    if (profile.getAuthentication() != null) {
      String username = profile.getAuthentication().getUsername();

      if ((username != null) && (username.length() > 0)) {
        getUsernameField().setText(username);
        String password = getLauncher().getAuthentication().guessPasswordFromSillyOldFormat(username);

        if ((password != null) && (password.length() > 0)) {
          getLauncher().println("Going to log in with legacy stored username & password...");

          getPasswordField().setText(password);
          tryLogIn(false, false);
        }
      }
    } else {
      getUsernameField().setText("");
      getPasswordField().setText("");
    }
  }

  public void tryLogIn(final boolean launchOnSuccess, final boolean verbose) {
    final OldAuthentication authentication = getLauncher().getAuthentication();
    final Profile profile = getLauncher().getProfileManager().getSelectedProfile();

    authentication.setAuthenticating(true);
    getLoginContainer().checkLoginState();

    getLauncher().getVersionManager().getExecutorService().submit(new Runnable()
    {
      public void run() {
        String username = usernameField.getText();
        try
        {
          Launcher.getInstance().println("Trying to log in...");
          Response response = authentication.login(username, new String(passwordField.getPassword()));

          if (!getLauncher().getProfileManager().getSelectedProfile().equals(profile)) {
            getLauncher().println("Profile changed during authentication, ignoring response.");
            getLauncher().getAuthentication().setAuthenticating(false);
            getLauncher().getAuthentication().clearLastSuccessfulResponse();
            getLoginContainer().checkLoginState();
            return;
          }

          if (response.getErrorMessage() != null) {
            NotLoggedInForm.this.loginFailed(response.getErrorMessage(), verbose);
          } else if (response.getSessionId() == null) {
            NotLoggedInForm.this.loginFailed("Could not log in: SessionID was null?", verbose);
          } else if (response.getPlayerName() == null) {
            NotLoggedInForm.this.loginFailed("Could not log in: Name was null?", verbose);
          } else if (response.getUUID() == null) {
            NotLoggedInForm.this.loginFailed("Could not log in: UUID was null?", verbose);
          } else {
            getLauncher().println("Logged in successfully");
            getLauncher().getAuthentication().setAuthenticating(false);

            NotLoggedInForm.this.saveAuthenticationDetails(profile);

            if (launchOnSuccess)
              getLauncher().getGameLauncher().playGame();
            else
              getLoginContainer().checkLoginState();
          }
        }
        catch (Throwable ex) {
          if (!getLauncher().getProfileManager().getSelectedProfile().equals(profile)) {
            getLauncher().println("Profile changed during authentication, ignoring response (which was an error anyway).");
            getLauncher().getAuthentication().setAuthenticating(false);
            getLoginContainer().checkLoginState();
            return;
          }

          OldAuthentication.StoredDetails details = getLauncher().getProfileManager().getSelectedProfile().getAuthentication();

          if ((details != null) && (details.getUsername().equals(username)) && (details.getDisplayName() != null) && (details.getUUID() != null)) {
            getLauncher().println("Couldn't log in", ex);
            Launcher.getInstance().println("Going to play offline as '" + details.getDisplayName() + "'...");
            OldAuthentication.Response response = new OldAuthentication.Response(details.getUsername(), null, null, details.getDisplayName(), details.getUUID());
            getLauncher().getAuthentication().setLastSuccessfulResponse(response);

            getLauncher().getAuthentication().setAuthenticating(false);
            getLoginContainer().checkLoginState();
          } else {
            NotLoggedInForm.this.loginFailed("Could not log in: " + ex.getMessage(), verbose);
          }
        }
      }
    });
  }

  private void saveAuthenticationDetails(Profile profile) {
    OldAuthentication.Response response = getLauncher().getAuthentication().getLastSuccessfulResponse();
    if ((response == null) || (response.getUsername() == null)) return;

    profile.setAuthentication(new OldAuthentication.StoredDetails(response.getUsername(), null, response.getPlayerName(), response.getUUID()));
    try
    {
      getLauncher().getProfileManager().saveProfiles();
    } catch (IOException e) {
      getLauncher().println("Couldn't save authentication details to profile", e);
    }
  }

  private void loginFailed(String error, boolean verbose) {
    Launcher.getInstance().println(error);
    if (verbose) ErrorMessagePopup.show(getLauncher().getFrame(), error);

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run() {
        getLauncher().getAuthentication().setAuthenticating(false);
        getLoginContainer().checkLoginState();
      }
    });
  }
}