package net.minecraft.launcher.authentication;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;

public class OldAuthentication
{
  private final Proxy proxy;
  private final File lastLoginFile;
  private Response lastSuccessfulResponse;
  private boolean isAuthenticating = false;

  public OldAuthentication(Launcher launcher, Proxy proxy) {
    lastLoginFile = new File(launcher.getWorkingDirectory(), "lastlogin");
    this.proxy = proxy;
  }

  public Response login(String username, String password) throws IOException {
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("user", username);
    args.put("password", password);
    args.put("version", Integer.valueOf(14));
    String response = Http.performPost(new URL(LauncherConstants.URL_AUTHENTICATION_OLD), args, proxy).trim();
    String[] split = response.split(":");

    if (split.length == 5) {
      lastSuccessfulResponse = new Response(username, null, split[3], split[2], split[4]);
      return lastSuccessfulResponse;
    }
    return new Response(username, response, null, null, null);
  }

  public StoredDetails getStoredDetails()
  {
    if (!lastLoginFile.isFile()) return null;
    try
    {
      Cipher cipher = getCipher(2, "passwordfile");
      DataInputStream dis;
      if (cipher != null)
        dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLoginFile), cipher));
      else {
        dis = new DataInputStream(new FileInputStream(lastLoginFile));
      }

      String username = dis.readUTF();
      String password = dis.readUTF();
      String displayName = username.length() > 0 ? username.split("@")[0] : "";
      StoredDetails result = new StoredDetails(username, password, displayName, null);
      dis.close();
      return result;
    } catch (Exception e) {
      Launcher.getInstance().println("Couldn't load old lastlogin file", e);
    }return null;
  }

  public String guessPasswordFromSillyOldFormat(String username)
  {
    StoredDetails details = getStoredDetails();

    if ((details != null) && 
      (details.getUsername().equals(username))) {
      return details.getPassword();
    }

    return null;
  }

  private Cipher getCipher(int mode, String password) throws Exception {
    Random random = new Random(43287234L);
    byte[] salt = new byte[8];
    random.nextBytes(salt);
    PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

    SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
    Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
    cipher.init(mode, pbeKey, pbeParamSpec);
    return cipher;
  }

  public Response getLastSuccessfulResponse() {
    return lastSuccessfulResponse;
  }

  public void setLastSuccessfulResponse(Response lastSuccessfulResponse) {
    this.lastSuccessfulResponse = lastSuccessfulResponse;
  }

  public void clearLastSuccessfulResponse() {
    lastSuccessfulResponse = null;
  }

  public synchronized boolean isAuthenticating() {
    return isAuthenticating;
  }

  public synchronized void setAuthenticating(boolean authenticating) {
    isAuthenticating = authenticating;
  }

  public static class Response
  {
    private final String username;
    private final String errorMessage;
    private final String sessionId;
    private final String playerName;
    private final String uuid;

    public Response(String username, String errorMessage, String sessionId, String playerName, String uuid)
    {
      this.username = username;
      this.errorMessage = errorMessage;
      this.sessionId = sessionId;
      this.playerName = playerName;
      this.uuid = uuid;
    }

    public String getUsername() {
      return username;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public String getSessionId() {
      return sessionId;
    }

    public String getPlayerName() {
      return playerName;
    }

    public String getUUID() {
      return uuid;
    }

    public boolean isOnline() {
      return sessionId != null;
    }
  }

  public static class StoredDetails
  {
    private final String username;
    private final String password;
    private final String displayName;
    private final String uuid;

    public StoredDetails(String username, String password, String displayName, String uuid)
    {
      this.username = username;
      this.password = password;
      this.displayName = displayName;
      this.uuid = uuid;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

    public String getDisplayName() {
      return displayName;
    }

    public String getUUID() {
      return uuid;
    }

    public boolean equals(Object o)
    {
      if (this == o) return true;
      if ((o == null) || (getClass() != o.getClass())) return false;

      StoredDetails that = (StoredDetails)o;

      if (username != null ? !username.equals(that.username) : that.username != null) return false;
      if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

      return true;
    }

    public int hashCode()
    {
      int result = username != null ? username.hashCode() : 0;
      result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
      return result;
    }
  }
}