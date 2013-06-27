package net.minecraft.launcher.authentication.yggdrasil;

public class InvalidateRequest
{
  private String accessToken;
  private String clientToken;

  public InvalidateRequest(YggdrasilAuthenticationService authenticationService)
  {
    accessToken = authenticationService.getAccessToken();
    clientToken = authenticationService.getClientToken();
  }
}