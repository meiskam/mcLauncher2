package net.minecraft.launcher.process;

import net.minecraft.launcher.authentication.OldAuthentication.Response;

public enum MinecraftProcessArguments
{
  LEGACY("%s %s", false), 
  USERNAME_SESSION("--username %s --session %s", false), 
  USERNAME_SESSION_UUID("--username %s --session %s --uuid %s", true);

  private final String format;
  private final boolean useUuid;

  private MinecraftProcessArguments(String format, boolean useUuid) { this.format = format;
    this.useUuid = useUuid; }

  public String formatAuthResponse(Response response)
  {
    if (useUuid) {
      return String.format(format, new Object[] { response.getPlayerName(), response.getSessionId(), response.getUUID() });
    }
    return String.format(format, new Object[] { response.getPlayerName(), response.getSessionId() });
  }
}