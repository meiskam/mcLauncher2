package net.minecraft.launcher.process;

import java.util.UUID;
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
    String playerName = response.getPlayerName();
    String sessionId = response.getSessionId() == null ? "-" : response.getSessionId();
    String uuid = response.getUUID() == null ? new UUID(0L, 0L).toString() : response.getUUID();

    if (useUuid) {
      if (useVersion) {
        return String.format(format, new Object[] { playerName, sessionId, uuid, version });
      }
      return String.format(format, new Object[] { playerName, sessionId, uuid });
    }

    if (useVersion) {
      return String.format(format, new Object[] { playerName, sessionId, version });
    }
    return String.format(format, new Object[] { playerName, sessionId });
  }
}