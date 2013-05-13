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
  private Response lastSuccessfulResponse;
  private boolean isAuthenticating = false;

  public OldAuthentication(Proxy proxy) {
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
      lastSuccessfulResponse = new Response(null, split[3], split[2], split[4]);
      return lastSuccessfulResponse;
    }
    return new Response(response, null, null, null);
  }

  public StoredDetails getStoredDetails(File file)
  {
    if (!file.isFile()) return null;
    try
    {
      Cipher cipher = getCipher(2, "passwordfile");
      DataInputStream dis;
      if (cipher != null)
        dis = new DataInputStream(new CipherInputStream(new FileInputStream(file), cipher));
      else {
        dis = new DataInputStream(new FileInputStream(file));
      }

      StoredDetails result = new StoredDetails(dis.readUTF(), dis.readUTF());
      dis.close();
      return result;
    } catch (Exception e) {
      Launcher.getInstance().println("Couldn't load old lastlogin file", e);
    }return null;
  }

  private Cipher getCipher(int mode, String password) throws Exception
  {
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
    private final String errorMessage;
    private final String sessionId;
    private final String name;
    private final String uuid;

    public Response(String errorMessage, String sessionId, String name, String uuid)
    {
      this.errorMessage = errorMessage;
      this.sessionId = sessionId;
      this.name = name;
      this.uuid = uuid;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public String getSessionId() {
      return sessionId;
    }

    public String getName() {
      return name;
    }

    public String getUUID() {
      return uuid;
    }
  }

  public static class StoredDetails
  {
    private final String username;
    private final String password;

    public StoredDetails(String username, String password)
    {
      this.username = username;
      this.password = password;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }
}