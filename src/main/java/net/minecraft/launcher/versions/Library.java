package net.minecraft.launcher.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.OperatingSystem;

public class Library
{
  private String name;
  private List<OperatingSystem> os;
  private Map<OperatingSystem, String> natives;
  private ExtractRules extract;
  private String url;

  public Library()
  {
  }

  public Library(String name)
  {
    if ((name == null) || (name.length() == 0)) throw new IllegalArgumentException("Library name cannot be null or empty");
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Library addRestriction(OperatingSystem[] operatingSystems) {
    if (os == null) os = new ArrayList<OperatingSystem>();
    if (operatingSystems != null) Collections.addAll(os, operatingSystems);
    return this;
  }

  public Library addNative(OperatingSystem operatingSystem, String name) {
    if ((operatingSystem == null) || (!operatingSystem.isSupported())) throw new IllegalArgumentException("Cannot add native for unsupported OS");
    if ((name == null) || (name.length() == 0)) throw new IllegalArgumentException("Cannot add native for null or empty name");
    if (natives == null) natives = new EnumMap<OperatingSystem, String>(OperatingSystem.class);
    natives.put(operatingSystem, name);
    return this;
  }

  public List<OperatingSystem> getRestrictedOperatingSystems() {
    return os;
  }

  public Map<OperatingSystem, String> getNatives() {
    return natives;
  }

  public ExtractRules getExtractRules() {
    return extract;
  }

  public Library setExtractRules(ExtractRules rules) {
    extract = rules;
    return this;
  }

  public String getArtifactBaseDir() {
    if (name == null) throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
    String[] parts = name.split(":", 3);
    return String.format("libraries/%s/%s/%s", new Object[] { parts[0].replaceAll("\\.", "/"), parts[1], parts[2] });
  }

  public String getArtifactPath() {
    if (name == null) throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
    return String.format("%s/%s", new Object[] { getArtifactBaseDir(), getArtifactFilename() });
  }

  public String getArtifactPath(String classifier) {
    if (name == null) throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
    String[] parts = name.split(":", 3);
    return String.format("%s/%s", new Object[] { getArtifactBaseDir(), getArtifactFilename(classifier) });
  }

  public String getArtifactFilename() {
    if (name == null) throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
    String[] parts = name.split(":", 3);
    return String.format("%s-%s.jar", new Object[] { parts[1], parts[2] });
  }

  public String getArtifactFilename(String classifier) {
    if (name == null) throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
    String[] parts = name.split(":", 3);
    return String.format("%s-%s-%s.jar", new Object[] { parts[1], parts[2], classifier });
  }

  public String toString()
  {
    return "Library{name='" + name + '\'' + ", os=" + os + ", natives=" + natives + ", extract=" + extract + '}';
  }

  public String getDownloadUrl()
  {
    if (url != null) return url;
    return LauncherConstants.URL_DOWNLOAD_BASE;
  }
}