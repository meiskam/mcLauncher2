package net.minecraft.launcher.versions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.launcher.OperatingSystem;

public class Rule
{
  private Action action = Action.ALLOW;
  private OSRestriction os;

  public Action getAppliedAction()
  {
    if ((os != null) && (!os.isCurrentOperatingSystem())) return null;

    return action;
  }

  public String toString()
  {
    return "Rule{action=" + action + ", os=" + os + '}';
  }

  public static enum Action
  {
    ALLOW, 
    DISALLOW;
  }
  public class OSRestriction {
    private OperatingSystem name;
    private String version;

    public OSRestriction() {  } 
    public boolean isCurrentOperatingSystem() { if ((name != null) && (name != OperatingSystem.getCurrentPlatform())) return false;

      if (version != null)
        try {
          Pattern pattern = Pattern.compile(version);
          Matcher matcher = pattern.matcher(System.getProperty("os.version"));
          if (!matcher.matches()) return false;
        }
        catch (Throwable localThrowable)
        {
        }
      return true;
    }

    public String toString()
    {
      return "OSRestriction{name=" + name + ", version='" + version + '\'' + '}';
    }
  }
}