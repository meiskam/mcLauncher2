package net.minecraft.launcher.authentication.yggdrasil;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.authentication.BaseAuthenticationService;
import net.minecraft.launcher.authentication.GameProfile;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.authentication.exceptions.InvalidCredentialsException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class YggdrasilAuthenticationService extends BaseAuthenticationService
{
  private static final String BASE_URL = "https://authserver.mojang.com/";
  private static final URL ROUTE_AUTHENTICATE = LauncherConstants.constantURL(BASE_URL + "authenticate");
  private static final URL ROUTE_REFRESH = LauncherConstants.constantURL(BASE_URL + "refresh");
  private static final URL ROUTE_VALIDATE = LauncherConstants.constantURL(BASE_URL + "validate");
  private static final URL ROUTE_INVALIDATE = LauncherConstants.constantURL(BASE_URL + "invalidate");
  private static final URL ROUTE_SIGNOUT = LauncherConstants.constantURL(BASE_URL + "signout");
  private static final String STORAGE_KEY_ACCESS_TOKEN = "accessToken";
  private final Gson gson = new Gson();
  private final Agent agent = Agent.MINECRAFT;
  private GameProfile[] profiles = null;
  private String accessToken = null;
  private boolean isOnline = false;

  public boolean canLogIn()
  {
    return (!canPlayOnline()) && (StringUtils.isNotBlank(getUsername())) && ((StringUtils.isNotBlank(getPassword())) || (StringUtils.isNotBlank(getAccessToken())));
  }

  public void logIn() throws AuthenticationException
  {
    if (StringUtils.isBlank(getUsername())) {
      throw new InvalidCredentialsException("Invalid username");
    }

    if (StringUtils.isNotBlank(getAccessToken()))
      logInWithToken();
    else if (StringUtils.isNotBlank(getPassword()))
      logInWithPassword();
    else
      throw new InvalidCredentialsException("Invalid password");
  }

  protected void logInWithPassword() throws AuthenticationException
  {
    if (StringUtils.isBlank(getUsername())) {
      throw new InvalidCredentialsException("Invalid username");
    }
    if (StringUtils.isBlank(getPassword())) {
      throw new InvalidCredentialsException("Invalid password");
    }

    Launcher.getInstance().println("Logging in with username & password");

    AuthenticationRequest request = new AuthenticationRequest(this, getPassword());
    AuthenticationResponse response = (AuthenticationResponse)makeRequest(ROUTE_AUTHENTICATE, request, AuthenticationResponse.class);

    if (!response.getClientToken().equals(getClientToken())) {
      throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
    }

    accessToken = response.getAccessToken();
    profiles = response.getAvailableProfiles();
    setSelectedProfile(response.getSelectedProfile());

    if ((getSelectedProfile() == null) && (ArrayUtils.isNotEmpty(profiles))) {
      selectGameProfile(profiles[0]);
    }

    fireAuthenticationChangedEvent();
  }

  protected void logInWithToken() throws AuthenticationException {
    if (StringUtils.isBlank(getUsername())) {
      throw new InvalidCredentialsException("Invalid username");
    }
    if (StringUtils.isBlank(getAccessToken())) {
      throw new InvalidCredentialsException("Invalid access token");
    }

    Launcher.getInstance().println("Logging in with access token");

    RefreshRequest request = new RefreshRequest(this);
    RefreshResponse response = (RefreshResponse)makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);

    if (!response.getClientToken().equals(getClientToken())) {
      throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
    }

    accessToken = response.getAccessToken();
    profiles = response.getAvailableProfiles();
    setSelectedProfile(response.getSelectedProfile());

    if ((getSelectedProfile() == null) && (ArrayUtils.isNotEmpty(profiles))) {
      selectGameProfile(profiles[0]);
    }

    fireAuthenticationChangedEvent();
  }

  protected <T extends Response> T makeRequest(URL url, Object input, Class<T> classOfT) throws AuthenticationException {
    try {
      String jsonResult = Http.performPost(url, gson.toJson(input), Launcher.getInstance().getProxy(), "application/json", true);
      T result = (T)gson.fromJson(jsonResult, classOfT);

      if (result == null) return null;

      if (StringUtils.isNotBlank(result.getError())) {
        if (result.getError().equals("ForbiddenOperationException")) {
          throw new InvalidCredentialsException(result.getErrorMessage());
        }
        throw new AuthenticationException(result.getErrorMessage());
      }

      isOnline = true;

      return result;
    } catch (IOException e) {
      throw new AuthenticationException("Cannot contact authentication server", e);
    }
  }

  public void logOut()
  {
    super.logOut();

    if ((StringUtils.isNotBlank(getClientToken())) && (StringUtils.isNotBlank(getAccessToken()))) {
      Launcher.getInstance().println("Invalidating accessToken with server...");
      try
      {
        makeRequest(ROUTE_INVALIDATE, new InvalidateRequest(this), Response.class);
      } catch (AuthenticationException e) {
        Launcher.getInstance().println("Couldn't invalidate token on server", e);
      }
    }

    accessToken = null;
    profiles = null;
    isOnline = false;
  }

  public GameProfile[] getAvailableProfiles()
  {
    return profiles;
  }

  public boolean isLoggedIn()
  {
    return StringUtils.isNotBlank(accessToken);
  }

  public boolean canPlayOnline()
  {
    return (isLoggedIn()) && (getSelectedProfile() != null) && (isOnline);
  }

  public void selectGameProfile(GameProfile profile) throws AuthenticationException
  {
    if (!isLoggedIn()) {
      throw new AuthenticationException("Cannot change game profile whilst not logged in");
    }
    if (getSelectedProfile() != null) {
      throw new AuthenticationException("Cannot change game profile. You must log out and back in.");
    }
    if ((profile == null) || (!ArrayUtils.contains(profiles, profile))) {
      throw new IllegalArgumentException("Invalid profile '" + profile + "'");
    }

    RefreshRequest request = new RefreshRequest(this, profile);
    RefreshResponse response = (RefreshResponse)makeRequest(ROUTE_REFRESH, request, RefreshResponse.class);

    if (!response.getClientToken().equals(getClientToken())) {
      throw new AuthenticationException("Server requested we change our client token. Don't know how to handle this!");
    }

    accessToken = response.getAccessToken();
    setSelectedProfile(response.getSelectedProfile());

    fireAuthenticationChangedEvent();
  }

  public void loadFromStorage(Map<String, String> credentials)
  {
    super.loadFromStorage(credentials);

    accessToken = ((String)credentials.get(STORAGE_KEY_ACCESS_TOKEN));
  }

  public Map<String, String> saveForStorage()
  {
    Map<String, String> result = super.saveForStorage();
    if (!shouldRememberMe()) return result;

    if (StringUtils.isNotBlank(getAccessToken())) {
      result.put(STORAGE_KEY_ACCESS_TOKEN, getAccessToken());
    }

    return result;
  }

  public String getSessionToken()
  {
    if ((isLoggedIn()) && (getSelectedProfile() != null) && (canPlayOnline())) {
      return String.format("token:%s:%s", new Object[] { getAccessToken(), getSelectedProfile().getId() });
    }
    return null;
  }

  public String getAccessToken()
  {
    return accessToken;
  }

  public String getClientToken() {
    return Launcher.getInstance().getClientToken().toString();
  }

  public Agent getAgent() {
    return agent;
  }

  public String toString()
  {
    return "YggdrasilAuthenticationService{agent=" + agent + ", profiles=" + Arrays.toString(profiles) + ", selectedProfile=" + getSelectedProfile() + ", sessionToken='" + getSessionToken() + '\'' + ", username='" + getUsername() + '\'' + ", isLoggedIn=" + isLoggedIn() + ", canPlayOnline=" + canPlayOnline() + ", accessToken='" + accessToken + '\'' + ", clientToken='" + getClientToken() + '\'' + '}';
  }
}