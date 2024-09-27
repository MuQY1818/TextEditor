import javax.swing.*;
import java.awt.*;

public class WindowManager {
    private TextEditor editor;
    private JDesktopPane desktopPane;

    public enum WindowArrangement {
        CASCADE,
        TILE_HORIZONTAL,
        TILE_VERTICAL
    }

    private WindowArrangement currentArrangement = WindowArrangement.CASCADE;

    public WindowManager(TextEditor editor, JDesktopPane desktopPane) {
        this.editor = editor;
        this.desktopPane = desktopPane;
    }

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