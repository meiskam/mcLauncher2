package net.minecraft.launcher.process;

import net.minecraft.launcher.authentication.OldAuthentication.Response;

public enum MinecraftProcessArguments
{
  LEGACY("%s %s", false, false), 
  USERNAME_SESSION("--username %s --session %s", false, false), 
  USERNAME_SESSION_VERSION("--username %s --session %s --version %s", false, true), 
  USERNAME_SESSION_UUID_VERSION("--username %s --session %s --uuid %s --version %s", true, true);

  private final String format;
  private final boolean useUuid;
  private final boolean useVersion;

  private MinecraftProcessArguments(String format, boolean useUuid, boolean useVersion) { this.format = format;
    this.useUuid = useUuid;
    this.useVersion = useVersion; }

  public String formatAuthResponse(Response response, String version)
  {
    if (useUuid) {
      if (useVersion) {
        return String.format(format, new Object[] { response.getPlayerName(), response.getSessionId(), response.getUUID(), version });
      }
      return String.format(format, new Object[] { response.getPlayerName(), response.getSessionId(), response.getUUID() });
    }

    if (useVersion) {
      return String.format(format, new Object[] { response.getPlayerName(), response.getSessionId(), version });
    }
    return String.format(format, new Object[] { response.getPlayerName(), response.getSessionId() });
  }
}