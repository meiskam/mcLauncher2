package net.minecraft.launcher.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.updater.DateTypeAdapter;
import net.minecraft.launcher.updater.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.io.FileUtils;

public class ProfileManager
{
  public static final String DEFAULT_PROFILE_NAME = "(Default)";
  private final Launcher launcher;
  private final Gson gson;
  private final Map<String, Profile> profiles = new HashMap<String, Profile>();
  private final File profileFile;
  private Profile selectedProfile;

  public ProfileManager(Launcher launcher)
  {
    this.launcher = launcher;
    profileFile = new File(launcher.getWorkingDirectory(), "launcher_profiles.json");

    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
    builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    builder.enableComplexMapKeySerialization();
    builder.setPrettyPrinting();
    gson = builder.create();
  }

  public void saveProfiles() throws IOException {
    RawProfileList rawProfileList = new RawProfileList();
    rawProfileList.profiles = profiles;
    rawProfileList.selectedProfile = getSelectedProfile().getName();

    FileUtils.writeStringToFile(profileFile, gson.toJson(rawProfileList));
  }

  public boolean loadProfiles() throws IOException {
    profiles.clear();
    selectedProfile = null;

    if (profileFile.isFile()) {
      RawProfileList rawProfileList = (RawProfileList)gson.fromJson(FileUtils.readFileToString(profileFile), RawProfileList.class);

      profiles.putAll(rawProfileList.profiles);
      selectedProfile = ((Profile)profiles.get(rawProfileList.selectedProfile));

      return true;
    }
    return false;
  }

  public Profile getSelectedProfile()
  {
    if (selectedProfile == null) {
      if (profiles.get(DEFAULT_PROFILE_NAME) != null) {
        selectedProfile = ((Profile)profiles.get(DEFAULT_PROFILE_NAME));
      } else if (profiles.size() > 0) {
        selectedProfile = ((Profile)profiles.values().iterator().next());
      } else {
        selectedProfile = new Profile(DEFAULT_PROFILE_NAME);
        profiles.put(DEFAULT_PROFILE_NAME, selectedProfile);
      }
    }

    return selectedProfile;
  }

  public Map<String, Profile> getProfiles() {
    return profiles;
  }

  public Launcher getLauncher() {
    return launcher;
  }

  private static class RawProfileList {
    public Map<String, Profile> profiles = new HashMap<String, Profile>();
    public String selectedProfile;
  }
}