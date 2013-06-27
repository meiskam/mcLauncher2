package net.minecraft.launcher.authentication;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.SwingUtilities;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.events.AuthenticationChangedListener;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseAuthenticationService
  implements AuthenticationService
{
  private static final String LEGACY_LASTLOGIN_PASSWORD = "passwordfile";
  private static final int LEGACY_LASTLOGIN_SEED = 43287234;
  private final List<AuthenticationChangedListener> listeners = new ArrayList<AuthenticationChangedListener>();
  private String username = null;
  private String password = null;
  private GameProfile selectedProfile = null;

  public boolean canLogIn()
  {
    return (!canPlayOnline()) && (StringUtils.isNotBlank(getUsername())) && (StringUtils.isNotBlank(getPassword()));
  }

  public void logOut()
  {
    password = null;
    setSelectedProfile(null);
  }

  public boolean isLoggedIn()
  {
    return getSelectedProfile() != null;
  }

  public boolean canPlayOnline()
  {
    return (isLoggedIn()) && (getSelectedProfile() != null) && (getSessionToken() != null);
  }

  public void addAuthenticationChangedListener(AuthenticationChangedListener listener)
  {
    listeners.add(listener);
  }

  public void removeAuthenticationChangedListener(AuthenticationChangedListener listener)
  {
    listeners.remove(listener);
  }

  protected void fireAuthenticationChangedEvent() {
    final List<AuthenticationChangedListener> listeners = new ArrayList<AuthenticationChangedListener>(this.listeners);

    for (Iterator<AuthenticationChangedListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
      AuthenticationChangedListener listener = (AuthenticationChangedListener)iterator.next();

      if (!listener.shouldReceiveEventsInUIThread()) {
        listener.onAuthenticationChanged(this);
        iterator.remove();
      }
    }

    if (!listeners.isEmpty())
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run() {
          for (AuthenticationChangedListener listener : listeners)
            listener.onAuthenticationChanged(BaseAuthenticationService.this);
        }
      });
  }

  public void setUsername(String username)
  {
    if ((isLoggedIn()) && (canPlayOnline())) {
      throw new IllegalStateException("Cannot change username whilst logged in & online");
    }

    this.username = username;
  }

  public void setPassword(String password) {
    if ((isLoggedIn()) && (canPlayOnline()) && (StringUtils.isNotBlank(password))) {
      throw new IllegalStateException("Cannot set password whilst logged in & online");
    }

    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  protected String getPassword() {
    return password;
  }

  public void loadFromStorage(Map<String, String> credentials)
  {
    logOut();

    setUsername((String)credentials.get(STORAGE_KEY_USERNAME));

    if ((credentials.containsKey(STORAGE_KEY_PROFILE_NAME)) && (credentials.containsKey(STORAGE_KEY_PROFILE_ID)))
      setSelectedProfile(new GameProfile((String)credentials.get(STORAGE_KEY_PROFILE_ID), (String)credentials.get(STORAGE_KEY_PROFILE_NAME)));
  }

  public Map<String, String> saveForStorage()
  {
    Map<String, String> result = new HashMap<String, String>();

    if (getUsername() != null) {
      result.put(STORAGE_KEY_USERNAME, getUsername());
    }

    if (getSelectedProfile() != null) {
      result.put(STORAGE_KEY_PROFILE_NAME, getSelectedProfile().getName());
      result.put(STORAGE_KEY_PROFILE_ID, getSelectedProfile().getId());
    }

    return result;
  }

  protected void setSelectedProfile(GameProfile selectedProfile) {
    this.selectedProfile = selectedProfile;
  }

  public GameProfile getSelectedProfile() {
    return selectedProfile;
  }

  public String toString()
  {
    StringBuilder result = new StringBuilder();

    result.append(getClass().getSimpleName());
    result.append("{");

    if (isLoggedIn()) {
      result.append("Logged in as ");
      result.append(getUsername());

      if (getSelectedProfile() != null) {
        result.append(" / ");
        result.append(getSelectedProfile());
        result.append(" - ");

        if (canPlayOnline()) {
          result.append("Online with session token '");
          result.append(getSessionToken());
          result.append("'");
        } else {
          result.append("Offline");
        }
      }
    } else {
      result.append("Not logged in");
    }

    result.append("}");

    return result.toString();
  }

  public String guessPasswordFromSillyOldFormat(File file) {
    String[] details = getStoredDetails(file);

    if ((details != null) && 
      (details[0].equals(getUsername()))) {
      return details[1];
    }

    return null;
  }

  public static String[] getStoredDetails(File lastLoginFile) {
    if (!lastLoginFile.isFile()) return null;
    try
    {
      Cipher cipher = getCipher(2, LEGACY_LASTLOGIN_PASSWORD);
      DataInputStream dis;
      if (cipher != null)
        dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLoginFile), cipher));
      else {
        dis = new DataInputStream(new FileInputStream(lastLoginFile));
      }

      String username = dis.readUTF();
      String password = dis.readUTF();
      dis.close();
      return new String[] { username, password };
    } catch (Exception e) {
      Launcher.getInstance().println("Couldn't load old lastlogin file", e);
    }return null;
  }

  private static Cipher getCipher(int mode, String password) throws Exception
  {
    Random random = new Random(LEGACY_LASTLOGIN_SEED);
    byte[] salt = new byte[8];
    random.nextBytes(salt);
    PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

    SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
    Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
    cipher.init(mode, pbeKey, pbeParamSpec);
    return cipher;
  }
}