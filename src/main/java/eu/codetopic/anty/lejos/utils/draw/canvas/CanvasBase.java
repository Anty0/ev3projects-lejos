package eu.codetopic.anty.lejos.utils.draw.canvas;

import eu.codetopic.anty.lejos.utils.Rectangle2DInt;
import eu.codetopic.anty.lejos.utils.draw.drawer.CommonDrawer;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.CommonLCD;
import lejos.hardware.lcd.GraphicsLCD;

class CanvasBase extends CanvasAbs {

    private final boolean drawRectangleAroundChildren;
    private final GraphicsLCD lcd;
    private boolean closed = false;

    CanvasBase(boolean drawRectangleAroundChildren) {
        this(BrickFinder.getDefault().getGraphicsLCD(), drawRectangleAroundChildren);
    }

    CanvasBase(GraphicsLCD lcd, boolean drawRectangleAroundChildren) {
        super(new Rectangle2DInt(0, 0, lcd.getWidth(), lcd.getHeight()));
        this.drawRectangleAroundChildren = drawRectangleAroundChildren;
        lcd.setAutoRefresh(false);
        this.lcd = lcd;
    }

    @Override
    public Canvas getParent() {
        return null;
    }

    @Override
    public void removeSelf() {
        if (closed) throw new NullPointerException();
        lcd.clear();
        lcd.refresh();
        closed = true;
    }

    @Override
    public void bumpSelf() {
        if (closed) throw new NullPointerException();
        // nothing to do (CanvasBase is always on top)
    }

    @Override
    public void apply() {
        if (closed) throw new NullPointerException();
        drawCanvas(this, lcd, 0, 0);
        lcd.refresh();
    }

    private void drawCanvas(Canvas canvas, GraphicsLCD drawer, int transX, int transY) {
        Rectangle2DInt area = canvas.getParentPosition();
        int x = transX + area.getX();
        int y = transY + area.getY();
        int width = area.getWidth();
        int height = area.getHeight();

        if (drawRectangleAroundChildren) drawer.bitBlt(null, 0, 0, 0, 0,
                x - 2, y - 2, width + 4, height + 4, CommonDrawer.ROP_CLEAR);
        drawer.bitBlt(canvas.getContent(), width, height, 0, 0, x, y, width, height, CommonDrawer.ROP_COPY);
        if (drawRectangleAroundChildren) drawer.drawRect(x - 2, y - 2, width + 3, height + 3);

        for (Canvas child : canvas.getChildren()) {
            drawCanvas(child, drawer, x, y);
        }
    }
}
