package net.minecraft.launcher.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D.Float;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import net.minecraft.launcher.Launcher;

public abstract class TexturedPanel extends JPanel
{
  private static final long serialVersionUID = 1L;
  private Image image;
  private Image bgImage;

  public TexturedPanel(String filename)
  {
    setOpaque(true);
    try
    {
      bgImage = ImageIO.read(TexturedPanel.class.getResource(filename)).getScaledInstance(32, 32, 16);
    } catch (IOException e) {
      Launcher.getInstance().println("Unexpected exception initializing textured panel", e);
    }
  }

  public void update(Graphics g)
  {
    paint(g);
  }

  public void paintComponent(Graphics graphics)
  {
    int width = getWidth() / 2 + 1;
    int height = getHeight() / 2 + 1;

    if ((image == null) || (image.getWidth(null) != width) || (image.getHeight(null) != height)) {
      image = createImage(width, height);
      copyImage(width, height);
    }

    graphics.drawImage(image, 0, 0, width * 2, height * 2, null);
  }

  protected void copyImage(int width, int height) {
    Graphics imageGraphics = image.getGraphics();

    for (int x = 0; x <= width / 32; x++) {
      for (int y = 0; y <= height / 32; y++) {
        imageGraphics.drawImage(bgImage, x * 32, y * 32, null);
      }
    }

    if ((imageGraphics instanceof Graphics2D)) {
      overlayGradient(width, height, (Graphics2D)imageGraphics);
    }

    imageGraphics.dispose();
  }

  protected void overlayGradient(int width, int height, Graphics2D graphics) {
    int gh = 1;
    graphics.setPaint(new GradientPaint(new Float(0.0F, 0.0F), new Color(553648127, true), new Float(0.0F, gh), new Color(0, true)));
    graphics.fillRect(0, 0, width, gh);

    gh = height;
    graphics.setPaint(new GradientPaint(new Float(0.0F, 0.0F), new Color(0, true), new Float(0.0F, gh), new Color(1610612736, true)));
    graphics.fillRect(0, 0, width, gh);
  }
}