package eu.codetopic.anty.lejos.utils.draw.canvas;

import eu.codetopic.anty.lejos.utils.Rectangle2DInt;
import eu.codetopic.anty.lejos.utils.draw.drawer.*;

import java.util.ArrayList;
import java.util.List;

abstract class CanvasAbs implements Canvas {

    private final List<Canvas> children = new ArrayList<>();

    protected final Rectangle2DInt drawingArea;
    protected GraphicsDrawer graphicsDrawer = null;
    protected TextDrawer textDrawer = null;
    protected final byte[] displayBuf;

    public CanvasAbs(Rectangle2DInt drawingArea) {
        this.drawingArea = drawingArea;
        displayBuf = CommonDrawerImpl.generateBuffer(drawingArea
                .getHeight(), drawingArea.getWidth());
    }

    @Override
    public Canvas[] getChildren() {
        synchronized (children) {
            return children.toArray(new Canvas[children.size()]);
        }
    }

    @Override
    public Canvas createRestrictedCanvas(Rectangle2DInt restrictedArea) {
        if (!drawingArea.contains(restrictedArea)) throw new IllegalArgumentException("restricted area bust be in canvas drawing area");
        Canvas child = new CanvasChild(this, restrictedArea);
        synchronized (children) {
            children.add(child);
        }
        return child;
    }

    @Override
    public void remove(Canvas canvas) {
        synchronized (children) {
            children.remove(canvas);
        }
    }

    @Override
    public void bump(Canvas canvas) {
        synchronized (children) {
            if (children.remove(canvas)) {
                children.add(canvas);
            }
        }
    }

    @Override
    public Rectangle2DInt getParentPosition() {
        return drawingArea;
    }

    @Override
    public byte[] getContent() {
        return displayBuf;
    }

    @Override
    public GraphicsDrawer getGraphicsDrawer() {
        if (graphicsDrawer == null) graphicsDrawer = new GraphicsDrawerImpl(
                drawingArea.getHeight(), drawingArea.getWidth(), displayBuf);
        return graphicsDrawer;
    }

    @Override
    public TextDrawer getTextDrawer() {
        if (textDrawer == null) textDrawer = new TextDrawerImpl(
                drawingArea.getHeight(), drawingArea.getWidth(), displayBuf);
        return textDrawer;
    }

    @Override
    public CommonDrawer getDrawer() {
        if (textDrawer != null) return textDrawer;
        return getGraphicsDrawer();
    }
}
