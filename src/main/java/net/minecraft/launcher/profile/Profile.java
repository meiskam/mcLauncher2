package net.minecraft.launcher.profile;

import java.io.File;
import net.minecraft.launcher.authentication.OldAuthentication.StoredDetails;

public class Profile
{
  public static final String DEFAULT_JRE_ARGUMENTS = "-Xmx1G";
  private String name;
  private File gameDir;
  private StoredDetails authentication;
  private String lastVersionId;
  private String javaDir;
  private String javaArgs;

  public Profile()
  {
  }

  public Profile(Profile copy)
  {
    name = copy.name;
    gameDir = copy.gameDir;
    authentication = copy.authentication;
    lastVersionId = copy.lastVersionId;
    javaDir = copy.javaDir;
    javaArgs = copy.javaArgs;
  }

  public Profile(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getGameDir() {
    return gameDir;
  }

  public void setGameDir(File gameDir) {
    this.gameDir = gameDir;
  }

  public StoredDetails getAuthentication() {
    return authentication;
  }

  public void setAuthentication(StoredDetails authentication) {
    this.authentication = authentication;
  }

  public void setLastVersionId(String lastVersionId) {
    this.lastVersionId = lastVersionId;
  }

  public void setJavaDir(String javaDir) {
    this.javaDir = javaDir;
  }

  public void setJavaArgs(String javaArgs) {
    this.javaArgs = javaArgs;
  }

  public String getLastVersionId() {
    return lastVersionId;
  }

  public String getJavaArgs() {
    return javaArgs;
  }

  public String getJavaPath() {
    return javaDir;
  }
}