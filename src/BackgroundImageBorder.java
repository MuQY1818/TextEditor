import javax.swing.border.Border;
import javax.swing.*;
import java.awt.*;

public class BackgroundImageBorder implements Border {
    private final Image image;
    private final float alpha;
    private final double scaleFactor;

    public BackgroundImageBorder(String imagePath, float alpha, double scaleFactor) {
        this.image = new ImageIcon(imagePath).getImage();
        this.alpha = alpha;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int imageWidth = (int) (image.getWidth(null) * scaleFactor);
        int imageHeight = (int) (image.getHeight(null) * scaleFactor);
        int offsetX = (width - imageWidth) / 2;
        int offsetY = (height - imageHeight) / 2;
        g2d.drawImage(image, offsetX, offsetY, imageWidth, imageHeight, null);
        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}