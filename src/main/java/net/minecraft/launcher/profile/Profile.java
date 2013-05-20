package net.minecraft.launcher.profile;

import java.io.File;
import java.util.List;
import net.minecraft.launcher.authentication.OldAuthentication.StoredDetails;

public class Profile
{
  private String name;
  private File gameDir;
  private StoredDetails authentication;
  private String lastVersionId;
  private String javaDir;
  private List<String> jvmArgs;

  public Profile()
  {
  }

  public Profile(String name)
  {
    this.name = name;
  }

  public String getName() {
    return name;
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

  public void setJvmArgs(List<String> jvmArgs) {
    this.jvmArgs = jvmArgs;
  }

  public String getLastVersionId() {
    return lastVersionId;
  }

  public List<String> getJvmArgs() {
    return jvmArgs;
  }

  public String getJavaPath() {
    return javaDir;
  }
}