package net.minecraft.launcher.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.SwingUtilities;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.AuthenticationSerializer;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.LegacyAuthenticationService;
import net.minecraft.launcher.events.RefreshedProfilesListener;
import net.minecraft.launcher.updater.DateTypeAdapter;
import net.minecraft.launcher.updater.FileTypeAdapter;
import net.minecraft.launcher.updater.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.io.FileUtils;

public class ProfileManager
{
  public static final String DEFAULT_PROFILE_NAME = "(Default)";
  private final Launcher launcher;
  private final Gson gson;
  private final Map<String, Profile> profiles = new HashMap<String, Profile>();
  private final File profileFile;
  private final List<RefreshedProfilesListener> refreshedProfilesListeners = Collections.synchronizedList(new ArrayList<RefreshedProfilesListener>());
  private String selectedProfile;

  public ProfileManager(Launcher launcher)
  {
    this.launcher = launcher;
    profileFile = new File(launcher.getWorkingDirectory(), "launcher_profiles.json");

    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
    builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    builder.registerTypeAdapter(File.class, new FileTypeAdapter());
    builder.registerTypeAdapter(AuthenticationService.class, new AuthenticationSerializer());
    builder.registerTypeAdapter(LegacyAuthenticationService.class, new AuthenticationSerializer());
    builder.enableComplexMapKeySerialization();
    builder.setPrettyPrinting();
    gson = builder.create();
  }

  public void saveProfiles() throws IOException {
    RawProfileList rawProfileList = new RawProfileList();
    rawProfileList.profiles = profiles;
    rawProfileList.selectedProfile = getSelectedProfile().getName();
    rawProfileList.clientToken = launcher.getClientToken();

    FileUtils.writeStringToFile(profileFile, gson.toJson(rawProfileList));
  }

  public boolean loadProfiles() throws IOException {
    profiles.clear();
    selectedProfile = null;

    if (profileFile.isFile()) {
      RawProfileList rawProfileList = (RawProfileList)gson.fromJson(FileUtils.readFileToString(profileFile), RawProfileList.class);

      profiles.putAll(rawProfileList.profiles);
      selectedProfile = rawProfileList.selectedProfile;
      launcher.setClientToken(rawProfileList.clientToken);

      fireRefreshEvent();
      return true;
    }
    fireRefreshEvent();
    return false;
  }

  public void fireRefreshEvent()
  {
    final List<RefreshedProfilesListener> listeners = new ArrayList<RefreshedProfilesListener>(refreshedProfilesListeners);
    for (Iterator<RefreshedProfilesListener> iterator = listeners.iterator(); iterator.hasNext(); ) {
      RefreshedProfilesListener listener = (RefreshedProfilesListener)iterator.next();

      if (!listener.shouldReceiveEventsInUIThread()) {
        listener.onProfilesRefreshed(this);
        iterator.remove();
      }
    }

    if (!listeners.isEmpty())
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run() {
          for (RefreshedProfilesListener listener : listeners)
            listener.onProfilesRefreshed(ProfileManager.this);
        }
      });
  }

  public Profile getSelectedProfile()
  {
    if ((selectedProfile == null) || (!profiles.containsKey(selectedProfile))) {
      if (profiles.get(DEFAULT_PROFILE_NAME) != null) {
        selectedProfile = DEFAULT_PROFILE_NAME;
      } else if (profiles.size() > 0) {
        selectedProfile = ((Profile)profiles.values().iterator().next()).getName();
      } else {
        selectedProfile = DEFAULT_PROFILE_NAME;
        profiles.put(DEFAULT_PROFILE_NAME, new Profile(selectedProfile));
      }
    }

    return (Profile)profiles.get(selectedProfile);
  }

  public Map<String, Profile> getProfiles() {
    return profiles;
  }

  public Launcher getLauncher() {
    return launcher;
  }

  public void addRefreshedProfilesListener(RefreshedProfilesListener listener) {
    refreshedProfilesListeners.add(listener);
  }

  public void setSelectedProfile(String selectedProfile) {
    boolean update = !this.selectedProfile.equals(selectedProfile);
    this.selectedProfile = selectedProfile;

    if (update)
      fireRefreshEvent();
  }

  private static class RawProfileList
  {
    public Map<String, Profile> profiles = new HashMap<String, Profile>();
    public String selectedProfile;
    public UUID clientToken = UUID.randomUUID();
  }
}