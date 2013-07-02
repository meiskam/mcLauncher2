package net.minecraft.launcher.authentication;

import java.io.File;
import java.util.Map;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.authentication.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.launcher.events.AuthenticationChangedListener;

public class LoadTestingAuthenticationService
  implements AuthenticationService
{
  private final AuthenticationService primary = new LegacyAuthenticationService();
  private final AuthenticationService secondary = new YggdrasilAuthenticationService();

  public void logIn() throws AuthenticationException
  {
    primary.logIn();
    try
    {
      secondary.logIn();
    } catch (AuthenticationException e) {
      Launcher.getInstance().println("Couldn't load-test new authentication service (method: logIn)", e);
    }
  }

  public boolean canLogIn()
  {
    return primary.canLogIn();
  }

  public void logOut()
  {
    primary.logOut();
    secondary.logOut();
  }

  public boolean isLoggedIn()
  {
    return primary.isLoggedIn();
  }

  public boolean canPlayOnline()
  {
    return primary.canPlayOnline();
  }

  public GameProfile[] getAvailableProfiles()
  {
    return primary.getAvailableProfiles();
  }

  public GameProfile getSelectedProfile()
  {
    return primary.getSelectedProfile();
  }

  public void selectGameProfile(GameProfile profile) throws AuthenticationException
  {
    primary.selectGameProfile(profile);
    try
    {
      secondary.selectGameProfile(profile);
    } catch (AuthenticationException e) {
      Launcher.getInstance().println("Couldn't load-test new authentication service (method: selectGameProfile)", e);
    }
  }

  public void loadFromStorage(Map<String, String> credentials)
  {
    primary.loadFromStorage(credentials);
    secondary.loadFromStorage(credentials);
  }

  public Map<String, String> saveForStorage()
  {
    return primary.saveForStorage();
  }

  public String getSessionToken()
  {
    return primary.getSessionToken();
  }

  public String getUsername()
  {
    return primary.getUsername();
  }

  public void setUsername(String username)
  {
    primary.setUsername(username);
    secondary.setUsername(username);
  }

  public void setPassword(String password)
  {
    primary.setPassword(password);
    secondary.setPassword(password);
  }

  public void addAuthenticationChangedListener(AuthenticationChangedListener listener)
  {
    primary.addAuthenticationChangedListener(listener);
  }

  public void removeAuthenticationChangedListener(AuthenticationChangedListener listener)
  {
    primary.removeAuthenticationChangedListener(listener);
  }

  public String guessPasswordFromSillyOldFormat(File lastlogin)
  {
    return primary.guessPasswordFromSillyOldFormat(lastlogin);
  }

  public void setRememberMe(boolean rememberMe)
  {
    primary.setRememberMe(rememberMe);
    secondary.setRememberMe(rememberMe);
  }

  public boolean shouldRememberMe()
  {
    return primary.shouldRememberMe();
  }
}