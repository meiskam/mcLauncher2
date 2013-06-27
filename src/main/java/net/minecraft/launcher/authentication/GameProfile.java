package net.minecraft.launcher.authentication;

import org.apache.commons.lang3.Validate;

public class GameProfile
{
  private final String id;
  private final String name;

  public GameProfile(String id, String name)
  {
    Validate.notBlank(id);
    Validate.notBlank(name);

    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) return false;

    GameProfile that = (GameProfile)o;

    if (!id.equals(that.id)) return false;
    if (!name.equals(that.name)) return false;

    return true;
  }

  public int hashCode()
  {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  public String toString()
  {
    return "GameProfile{id='" + id + '\'' + ", name='" + name + '\'' + '}';
  }
}