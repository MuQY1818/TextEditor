import javax.swing.border.Border;
import javax.swing.*;
import java.awt.*;

/**
 * 实现Border接口的类，用于在组件边界上绘制背景图像
 */
public class BackgroundImageBorder implements Border {
    // 图像对象，用于边界上绘制的背景图像
    private final Image image;
    // alpha透明度，用于设置图像的透明度
    private final float alpha;
    // 缩放因子，用于调整图像的显示大小
    private final double scaleFactor;

    /**
     * 构造函数，初始化BackgroundImageBorder对象
     *
     * @param imagePath 图像文件的路径
     * @param alpha 图像的透明度，取值范围为0.0到1.0
     * @param scaleFactor 图像的缩放因子，用于调整图像大小
     */
    public BackgroundImageBorder(String imagePath, float alpha, double scaleFactor) {
        this.image = new ImageIcon(imagePath).getImage();
        this.alpha = alpha;
        this.scaleFactor = scaleFactor;
    }

    /**
     * 绘制边界上的背景图像
     *
     * @param c 要绘制边界的组件
     * @param g 图形上下文
     * @param x 组件的X坐标
     * @param y 组件的Y坐标
     * @param width 组件的宽度
     * @param height 组件的高度
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        // 设置图形的透明度
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        // 计算图像的缩放后的宽度和高度
        int imageWidth = (int) (image.getWidth(null) * scaleFactor);
        int imageHeight = (int) (image.getHeight(null) * scaleFactor);
        // 计算图像在组件中的居中偏移量
        int offsetX = (width - imageWidth) / 2;
        int offsetY = (height - imageHeight) / 2;
        // 绘制图像
        g2d.drawImage(image, offsetX, offsetY, imageWidth, imageHeight, null);
        g2d.dispose();
    }

    /**
     * 获取边界的内边距
     *
     * @param c 与边界相关的组件
     * @return Insets对象，表示边界的内边距
     *
     * 由于图像仅作为背景绘制，并不影响组件的实际内边距，因此返回零内边距
     */
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }

    /**
     * 检查边界是否不透明
     *
     * @return boolean值，如果边界是不透明的，则为true；否则为false
     *
     * 由于图像可能具有透明度，所以边界被认为是透明的
     */
    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
