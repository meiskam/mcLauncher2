package net.minecraft.launcher.events;

import net.minecraft.launcher.profile.ProfileManager;

public abstract interface ProfileChangedListener
{
  public abstract void onProfileChanged(ProfileManager paramProfileManager);

  public abstract boolean shouldReceiveEventsInUIThread();
}