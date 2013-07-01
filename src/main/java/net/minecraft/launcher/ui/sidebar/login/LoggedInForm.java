package net.minecraft.launcher.ui.sidebar.login;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.minecraft.launcher.GameLauncher;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.GameProfile;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.updater.VersionManager;

public class LoggedInForm extends BaseLogInForm
{
  private final JButton playButton = new JButton("Play");
  private final JButton logOutButton = new JButton("Log Out");
  private final JLabel welcomeText = new JLabel("<html>OH NO PANIC! :(</html>");
  private AuthenticationService previousAuthentication = null;

  public LoggedInForm(LoginContainerForm container) {
    super(container, "Play Game");
    setMaximumSize(new Dimension(2147483647, 300));
    createInterface();
  }

  protected void populateGrid(GridBagConstraints constraints) {
    constraints.fill = 2;

    add(welcomeText, constraints, 0, 0, 0, 0);

    playButton.addActionListener(this);
    logOutButton.addActionListener(this);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 2));
    buttonPanel.add(logOutButton);
    buttonPanel.add(playButton);

    playButton.setFont(new Font(playButton.getFont().getName(), 1, playButton.getFont().getSize()));

    add(buttonPanel, constraints, 0, 3, 0, 0);
  }

  public void checkLoginState() {
    boolean canPlay = true;
    boolean canLogOut = true;
    Profile profile = getLauncher().getProfileManager().getSelectedProfile();
    AuthenticationService authentication = profile.getAuthentication();

    if (getLauncher().getGameLauncher().isWorking()) {
      canPlay = false;
      canLogOut = false;
    }

    if (getLauncher().getVersionManager().getVersions(profile.getVersionFilter()).size() <= 0) {
      canPlay = false;
    }

    welcomeText.setText("<html>Welcome, guest!</html>");

    if (authentication.isLoggedIn()) {
      if (authentication.getSelectedProfile() == null) {
        playButton.setText("Play Demo");
      } else {
        welcomeText.setText("<html>Welcome, <b>" + authentication.getSelectedProfile().getName() + "</b>!</html>");

        if (authentication.canPlayOnline())
          playButton.setText("Play");
        else {
          playButton.setText("Play Offline");
        }
      }
    }

    logOutButton.setEnabled(canLogOut);
    playButton.setEnabled(canPlay);

    previousAuthentication = authentication;
  }

  public void onProfilesRefreshed(ProfileManager manager)
  {
    Profile profile = manager.getSelectedProfile();

    if (profile.getAuthentication() != previousAuthentication)
      getLoginContainer().checkLoginState();
  }

  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == playButton) {
      getLauncher().getVersionManager().getExecutorService().submit(new Runnable()
      {
        public void run() {
          getLauncher().getGameLauncher().playGame();
        } } );
    }
    else if (e.getSource() == logOutButton) {
      getLauncher().getProfileManager().getSelectedProfile().getAuthentication().logOut();
      getLoginContainer().checkLoginState();
    }
  }
}