import javax.swing.*;

/**
 * 窗口管理器类用于管理文本编辑器中的窗口排列和定位
 */
public class WindowManager {
    // 文本编辑器实例
    private final TextEditor editor;
    // 桌面窗格，用于容纳内部框架
    private final JDesktopPane desktopPane;

    /**
     * 窗口排列方式枚举
     */
    public enum WindowArrangement {
        // 以瀑布方式排列窗口
        CASCADE,
        // 水平平铺窗口
        TILE_HORIZONTAL,
        // 垂直平铺窗口
        TILE_VERTICAL
    }

    // 当前的窗口排列方式，默认为CASCADE
    private WindowArrangement currentArrangement = WindowArrangement.CASCADE;

    /**
     * 构造函数，初始化窗口管理器
     *
     * @param editor      文本编辑器实例
     * @param desktopPane 桌面窗格实例
     */
    public WindowManager(TextEditor editor, JDesktopPane desktopPane) {
        this.editor = editor;
        this.desktopPane = desktopPane;
    }

    /**
     * 以瀑布方式排列窗口
     */
    public void cascadeWindows() {
        currentArrangement = WindowArrangement.CASCADE;
        int x = 0, y = 0;
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame.isIcon()) continue;
            try {
                frame.setMaximum(false);
                frame.reshape(x, y, 400, 300);
                x += 30;
                y += 30;
                if (x + 400 > desktopPane.getWidth()) x = 0;
                if (y + 300 > desktopPane.getHeight()) y = 0;
                frame.toFront();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 水平平铺窗口
     */
    public void tileWindowsHorizontally() {
        currentArrangement = WindowArrangement.TILE_HORIZONTAL;
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int count = frames.length;
        if (count == 0) return;

        int rows = (int) Math.sqrt(count);
        int cols = count / rows;
        if (count % rows != 0) cols++;

        int width = desktopPane.getWidth() / cols;
        int height = desktopPane.getHeight() / rows;
        int x = 0, y = 0;

        for (JInternalFrame frame : frames) {
            if (frame.isIcon()) continue;
            try {
                frame.setMaximum(false);
                frame.reshape(x, y, width, height);
                x += width;
                if (x >= desktopPane.getWidth()) {
                    x = 0;
                    y += height;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 垂直平铺窗口
     */
    public void tileWindowsVertically() {
        currentArrangement = WindowArrangement.TILE_VERTICAL;
        JInternalFrame[] frames = desktopPane.getAllFrames();
        int count = frames.length;
        if (count == 0) return;

        int height = desktopPane.getHeight() / count;
        int y = 0;

        for (JInternalFrame frame : frames) {
            if (frame.isIcon()) continue;
            try {
                frame.setMaximum(false);
                frame.reshape(0, y, desktopPane.getWidth(), height);
                y += height;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 为新窗口定位，根据当前的排列方式
     *
     * @param newFrame 新的内部框架
     */
    public void positionNewWindow(JInternalFrame newFrame) {
        if (currentArrangement != WindowArrangement.CASCADE) {
            applyCurrentArrangement();
        } else {
            int offset = 30;
            int x = 0, y = 0;
            boolean positionFound = false;

            while (!positionFound) {
                positionFound = true;
                for (JInternalFrame frame : desktopPane.getAllFrames()) {
                    if (frame != newFrame && !frame.isIcon() &&
                        frame.getX() == x && frame.getY() == y) {
                        x += offset;
                        y += offset;
                        if (x + newFrame.getWidth() > desktopPane.getWidth()) x = 0;
                        if (y + newFrame.getHeight() > desktopPane.getHeight()) y = 0;
                        positionFound = false;
                        break;
                    }
                }
            }

            newFrame.setLocation(x, y);
        }
    }

    /**
     * 应用当前的窗口排列方式
     */
    private void applyCurrentArrangement() {
        switch (currentArrangement) {
            case CASCADE:
                cascadeWindows();
                break;
            case TILE_HORIZONTAL:
                tileWindowsHorizontally();
                break;
            case TILE_VERTICAL:
                tileWindowsVertically();
                break;
        }
    }
}
