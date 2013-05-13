package net.minecraft.launcher.updater;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.launcher.versions.ReleaseType;

public class VersionFilter
{
  private final Set<ReleaseType> types = new HashSet<ReleaseType>();
  private int maxCount = 5;

  public VersionFilter() {
    Collections.addAll(types, ReleaseType.values());
  }

  public Set<ReleaseType> getTypes() {
    return types;
  }

  public VersionFilter onlyForTypes(ReleaseType[] types) {
    this.types.clear();
    includeTypes(types);
    return this;
  }

  public VersionFilter includeTypes(ReleaseType[] types) {
    if (types != null) Collections.addAll(this.types, types);
    return this;
  }

  public VersionFilter excludeTypes(ReleaseType[] types) {
    if (types != null) {
      for (ReleaseType type : types) {
        this.types.remove(type);
      }
    }
    return this;
  }

  public int getMaxCount() {
    return maxCount;
  }

  public VersionFilter setMaxCount(int maxCount) {
    this.maxCount = maxCount;
    return this;
  }
}