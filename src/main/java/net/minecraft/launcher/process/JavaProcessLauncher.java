package net.minecraft.launcher.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaProcessLauncher
{
  private static final String PATH_SEPARATOR = System.getProperty("file.separator");
  private static final String JAVA_PATH = System.getProperty("java.home") + PATH_SEPARATOR + "bin" + PATH_SEPARATOR;
  private List<String> commands;
  private File directory;

  public JavaProcessLauncher(String[] commands)
  {
    this.commands = new ArrayList<String>(commands.length);
    addCommands(commands);
  }

  public JavaProcess start() throws IOException {
    List<String> full = getFullCommands();
    return new JavaProcess(full, new ProcessBuilder(full).directory(directory).redirectErrorStream(true).start());
  }

  public List<String> getFullCommands() {
    List<String> result = new ArrayList<String>(commands);
    result.add(0, getJavaPath());
    return result;
  }

  public List<String> getCommands() {
    return commands;
  }

  public void addCommands(String[] commands) {
    this.commands.addAll(Arrays.asList(commands));
  }

  public JavaProcessLauncher directory(File directory) {
    this.directory = directory;

    return this;
  }

  public File getDirectory() {
    return directory;
  }

  public static String buildCommands(List<String> commands) {
    StringBuilder builder = new StringBuilder(80);

    for (int i = 0; i < commands.size(); i++) {
      if (i > 0) {
        builder.append(' ');
      }

      String part = (String)commands.get(i);

      if ((part.indexOf(' ') >= 0) || (part.indexOf('\t') >= 0) || (part.indexOf("*") > 0)) {
        if (part.charAt(0) != '"') {
          builder.append('"');
          builder.append(part);
          if (part.endsWith("\\")) {
            builder.append("\\");
          }
          builder.append('"');
        } else if (part.endsWith("\"")) {
          builder.append(part);
        } else {
          throw new IllegalArgumentException("Illegal unmatched quote in commands");
        }
      }
      else builder.append(part);

    }

    return builder.toString();
  }

  protected String getJavaPath() {
    return JAVA_PATH + "java";
  }

  public String toString()
  {
    return "JavaProcessLauncher[commands=" + commands + ", java=" + JAVA_PATH + "]";
  }
}