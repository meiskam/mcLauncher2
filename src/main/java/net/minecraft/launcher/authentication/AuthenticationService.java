package net.minecraft.launcher.authentication;

import java.io.File;
import java.util.Map;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.events.AuthenticationChangedListener;

public abstract interface AuthenticationService
{
  public static final String STORAGE_KEY_PROFILE_NAME = "displayName";
  public static final String STORAGE_KEY_PROFILE_ID = "uuid";
  public static final String STORAGE_KEY_USERNAME = "username";
  public static final String STORAGE_KEY_REMEMBER_ME = "rememberMe";

  public abstract boolean canLogIn();

  public abstract void logIn()
    throws AuthenticationException;

  public abstract void logOut();

  public abstract boolean isLoggedIn();

  public abstract boolean canPlayOnline();

  public abstract GameProfile[] getAvailableProfiles();

  public abstract GameProfile getSelectedProfile();

  public abstract void selectGameProfile(GameProfile paramGameProfile)
    throws AuthenticationException;

  public abstract void loadFromStorage(Map<String, String> paramMap);

  public abstract Map<String, String> saveForStorage();

  public abstract String getSessionToken();

  public abstract String getUsername();

  public abstract void setUsername(String paramString);

  public abstract void setPassword(String paramString);

  public abstract void addAuthenticationChangedListener(AuthenticationChangedListener paramAuthenticationChangedListener);

  public abstract void removeAuthenticationChangedListener(AuthenticationChangedListener paramAuthenticationChangedListener);

  public abstract String guessPasswordFromSillyOldFormat(File paramFile);

  public abstract void setRememberMe(boolean paramBoolean);

  public abstract boolean shouldRememberMe();
}