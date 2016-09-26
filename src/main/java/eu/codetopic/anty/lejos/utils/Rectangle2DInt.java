package eu.codetopic.anty.lejos.utils;

public class Rectangle2DInt {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /**
     * Create an empty rectangle at (0,0)
     */
    public Rectangle2DInt() {
        x = y = width = height = 0;
    }

    /**
     * Create a rectangle with int coordinates
     *
     * @param x the x coordinate of the top left corner
     * @param y the y coordinate of the top left corner
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public Rectangle2DInt(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isEmpty() {
        return (width <= 0) || (height <= 0);
    }

    public boolean contains(Rectangle2DInt rect) {
        return contains(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public boolean contains(int x, int y, int w, int h) {
        if (isEmpty() || w < 0 || h < 0) return false;
        int x0 = getX();
        int y0 = getY();
        return (x >= x0 && y >= y0 &&
                (x + w) <= x0 + getWidth() &&
                (y + h) <= y0 + getHeight());
    }

    public boolean contains(int x, int y) {
        int x0 = getX();
        int y0 = getY();
        return (x >= x0 && y >= y0 &&
                x < x0 + getWidth() && y < y0 + getHeight());
    }

    /**
     * Test if this Rectangle2D intersects a rectangle defined by int coordinates
     */
    public boolean intersects(int x, int y, int w, int h) {
        if (isEmpty() || w <= 0 || h <= 0) return false;
        int x0 = getX();
        int y0 = getY();
        return (x + w > x0 && y + h > y0 &&
                x < x0 + getWidth() && y < y0 + getHeight());
    }

    /**
     * Test if the rectangle is equal to a given object
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Rectangle2DInt
                && getX() == ((Rectangle2DInt) obj).getX()
                && getY() == ((Rectangle2DInt) obj).getY()
                && getWidth() == ((Rectangle2DInt) obj).getWidth()
                && getHeight() == ((Rectangle2DInt) obj).getHeight();
    }
}
