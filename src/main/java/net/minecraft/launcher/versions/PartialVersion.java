package net.minecraft.launcher.versions;

import java.util.Date;

public class PartialVersion
  implements Version
{
  private String id;
  private Date time;
  private Date releaseTime;
  private ReleaseType type;

  public PartialVersion()
  {
  }

  public PartialVersion(String id, Date releaseTime, Date updateTime, ReleaseType type)
  {
    if ((id == null) || (id.length() == 0)) throw new IllegalArgumentException("ID cannot be null or empty");
    if (releaseTime == null) throw new IllegalArgumentException("Release time cannot be null");
    if (updateTime == null) throw new IllegalArgumentException("Update time cannot be null");
    if (type == null) throw new IllegalArgumentException("Release type cannot be null");
    this.id = id;
    this.releaseTime = releaseTime;
    time = updateTime;
    this.type = type;
  }

  public PartialVersion(Version version) {
    this(version.getId(), version.getReleaseTime(), version.getUpdatedTime(), version.getType());
  }

  public String getId()
  {
    return id;
  }

  public ReleaseType getType()
  {
    return type;
  }

  public Date getUpdatedTime()
  {
    return time;
  }

  public void setUpdatedTime(Date time)
  {
    if (time == null) throw new IllegalArgumentException("Time cannot be null");
    this.time = time;
  }

  public Date getReleaseTime()
  {
    return releaseTime;
  }

  public void setReleaseTime(Date time)
  {
    if (time == null) throw new IllegalArgumentException("Time cannot be null");
    releaseTime = time;
  }

  public void setType(ReleaseType type)
  {
    if (type == null) throw new IllegalArgumentException("Release type cannot be null");
    this.type = type;
  }

  public String toString()
  {
    return "PartialVersion{id='" + id + '\'' + ", updateTime=" + time + ", releaseTime=" + releaseTime + ", type=" + type + '}';
  }
}