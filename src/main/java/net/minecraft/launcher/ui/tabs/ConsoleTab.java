package net.minecraft.launcher.ui.tabs;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.minecraft.launcher.Launcher;

public class ConsoleTab extends JScrollPane
{
  private static final Font MONOSPACED = new Font("Monospaced", 0, 12);

  private final JTextPane console = new JTextPane();
  private final Launcher launcher;

  public ConsoleTab(Launcher launcher)
  {
    this.launcher = launcher;

    console.setFont(MONOSPACED);
    console.setEditable(false);
    console.setMargin(null);

    setViewportView(console);
  }

  public Launcher getLauncher() {
    return launcher;
  }

  public void print(final String line) {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run() {
          print(line);
        }
      });
      return;
    }

    Document document = console.getDocument();
    JScrollBar scrollBar = getVerticalScrollBar();
    boolean shouldScroll = false;

    if (getViewport().getView() == console) {
      shouldScroll = scrollBar.getValue() + scrollBar.getSize().getHeight() + MONOSPACED.getSize() * 4 > scrollBar.getMaximum();
    }
    try
    {
      document.insertString(document.getLength(), line, null);
    } catch (BadLocationException localBadLocationException) {
    }
    if (shouldScroll)
      scrollBar.setValue(2147483647);
  }
}