package net.minecraft.launcher.authentication.yggdrasil;

public class Agent
{
  public static final Agent MINECRAFT = new Agent("Minecraft", 1);
  private final String name;
  private final int version;

  public Agent(String name, int version)
  {
    this.name = name;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public String toString()
  {
    return "Agent{name='" + name + '\'' + ", version=" + version + '}';
  }
}