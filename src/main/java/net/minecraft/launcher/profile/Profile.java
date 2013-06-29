package net.minecraft.launcher.profile;

import java.io.File;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.yggdrasil.YggdrasilAuthenticationService;

public class Profile
{
  public static final String DEFAULT_JRE_ARGUMENTS = "-Xmx1G";
  public static final Resolution DEFAULT_RESOLUTION = new Resolution(854, 480);

  private AuthenticationService authentication = new YggdrasilAuthenticationService();
  private String name;
  private File gameDir;
  private String lastVersionId;
  private String javaDir;
  private String javaArgs;
  private Resolution resolution;

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
    resolution = copy.resolution;
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

  public Resolution getResolution() {
    return resolution;
  }

  public void setResolution(Resolution resolution) {
    this.resolution = resolution;
  }

  public AuthenticationService getAuthentication() {
    return authentication;
  }
  public static class Resolution {
    private int width;
    private int height;

    public Resolution() {
    }

    public Resolution(int width, int height) {
      this.width = width;
      this.height = height;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }
  }
}