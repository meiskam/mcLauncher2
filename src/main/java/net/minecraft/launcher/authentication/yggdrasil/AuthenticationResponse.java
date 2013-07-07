package net.minecraft.launcher.authentication.yggdrasil;

import net.minecraft.launcher.authentication.GameProfile;

public class AuthenticationResponse extends Response
{
  private String accessToken;
  private String clientToken;
  private GameProfile selectedProfile;
  private GameProfile[] availableProfiles;

  public String getAccessToken()
  {
    return accessToken;
  }

  public String getClientToken() {
    return clientToken;
  }

  public GameProfile[] getAvailableProfiles() {
    return availableProfiles;
  }

  public GameProfile getSelectedProfile() {
    return selectedProfile;
  }
}