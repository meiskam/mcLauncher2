package net.minecraft.launcher;

import java.net.MalformedURLException;
import java.net.URL;

public class LauncherConstants
{
  public static final String VERSION_NAME = "1.0.7";
  public static final int VERSION_NUMERIC = 3;
  public static final String URL_REGISTER = "https://account.mojang.com/register";
  public static final String URL_DOWNLOAD_BASE = "https://s3.amazonaws.com/Minecraft.Download/";
  public static final String URL_RESOURCE_BASE = "https://s3.amazonaws.com/Minecraft.Resources/";
  public static final String URL_BLOG = "http://mcupdate.tumblr.com";
  public static final String URL_SUPPORT = "http://help.mojang.com";
  public static final String URL_STATUS_CHECKER = "http://status.mojang.com/check";
  public static final int UNVERSIONED_BOOTSTRAP_VERSION = 0;
  public static final int MINIMUM_BOOTSTRAP_SUPPORTED = 4;
  public static final String URL_BOOTSTRAP_DOWNLOAD = "https://mojang.com/2013/06/minecraft-1-6-pre-release/";
  public static final String[] BOOTSTRAP_OUT_OF_DATE_BUTTONS = { "Go to URL", "Close" };

  public static final String[] CONFIRM_PROFILE_DELETION_OPTIONS = { "Delete profile", "Cancel" };
  public static final String URL_FORGOT_PASSWORD_MOJANG = "https://account.mojang.com/resetpassword/request";
  public static final String URL_FORGOT_PASSWORD_MINECRAFT = "https://minecraft.net/resetpassword";
  public static final String URL_FORGOT_MIGRATED_EMAIL = "http://help.mojang.com/customer/portal/articles/1205055-minecraft-launcher-error---migrated-account";
  public static final int MAX_NATIVES_LIFE_IN_SECONDS = 3600;

  public static URL constantURL(String input)
  {
    try
    {
      return new URL(input);
    } catch (MalformedURLException e) {
      throw new Error(e);
    }
  }
}