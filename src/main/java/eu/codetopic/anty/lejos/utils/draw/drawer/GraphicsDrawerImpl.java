package eu.codetopic.anty.lejos.utils.draw.drawer;

import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.Image;

public class GraphicsDrawerImpl extends CommonDrawerImpl implements GraphicsDrawer {

    /** drawArc and fillArc accuracy parameter */
    private static final int ARC_ACC = 5;
    private int rgbColor = BLACK;
    private int textRop = ROP_OR;
    private int pixelRop = ROP_SET;
    private int strokeStyle = SOLID;
    private Font font = Font.getDefaultFont();

    public GraphicsDrawerImpl(int height, int width) {
        super(height, width);
    }

    public GraphicsDrawerImpl(int height, int width, byte[] dst) {
        super(height, width, dst);
    }

    /**
     * Adjust the x co-ordinate to use the translation and anchor values.
     * @param x Original value
     * @param w width of the item.
     * @param anchor anchor parameter
     * @return updated x value.
     */
    @SuppressWarnings("Duplicates")
    private int adjustX(int x, int w, int anchor) {
        switch (anchor & (LEFT | RIGHT | HCENTER)) {
            case LEFT:
                break;
            case RIGHT:
                x -= w;
                break;
            case HCENTER:
                x -= (w >> 1);
                break;
        }
        return x;
    }

    /**
     * Adjust the y co-ordinate to use the translation and anchor values.
     * @param y Original value
     * @param h height of the item.
     * @param anchor anchor parameter
     * @return updated y value.
     */
    @SuppressWarnings("Duplicates")
    private int adjustY(int y, int h, int anchor) {
        switch (anchor & (TOP | BOTTOM | VCENTER)) {
            case TOP:
                break;
            case BOTTOM:
                y -= h;
                break;
            case VCENTER:
                y -= (h >> 1);
                break;
        }
        return y;
    }

    /**
     * Draws the specified String using the current font and color. x and y
     * give the location of the anchor point. Additional method to allow for
     * the easy use of inverted text. In this case the area below the string
     * is drawn in the current color, before drawing the text in the "inverted"
     * color.
     * <br><b>Note</b>: This is a non standard method.
     * @param str the String to be drawn
     * @param x the x coordinate of the anchor point
     * @param y the y coordinate of the anchor point
     * @param anchor the anchor point for positioning the text
     * @param inverted true to invert the text display.
     */
    @Override
    @SuppressWarnings("Duplicates")
    public void drawString(String str, int x, int y, int anchor, boolean inverted) {
        if (anchor == 0)
            anchor = TOP | LEFT;
        if ((anchor & LEFT) == 0) {
            int strWidth = font.stringWidth(str);
            if ((anchor & RIGHT) != 0)
                x -= strWidth;
            else if ((anchor & HCENTER) != 0)
                x -= (strWidth / 2);
        }

        if ((anchor & TOP) == 0) {
            if ((anchor & BASELINE) != 0)
                y -= font.getBaselinePosition();
            else if ((anchor & BOTTOM) != 0)
                y -= font.getHeight();
        }
        int gw = font.glyphWidth;
        int gh = font.height;
        int rop = textRop;
        int cellWidth = font.width;
        byte[] glyphs = font.glyphs;
        int span = gw * font.glyphCount;
        int first = font.firstChar;
        char[] strData = str.toCharArray();
        if (inverted) {
            // draw background and use inverted rop...
            bitBlt(null, width, height, 0, 0, displayBuf, width, height, x, y, cellWidth * strData.length, gh, pixelRop);
            rop = (rgbColor == WHITE ? ROP_OR : ROP_ANDINVERTED);
        }
        for (int i = 0; i < strData.length; i++)
            bitBlt(glyphs, span, gh, gw * (strData[i] - first), 0, displayBuf, width, height, x + i * cellWidth, y, gw, gh, rop);
    }

    /**
     * Draw the specified image to the graphics surface, using the supplied rop.
     * <br><b>Note</b>: This is a non standard method.
     * Added because without it, it is very
     * hard to invert/manipulate an image, or screen region
     * @param src image to draw (may be null for ops that do not require input.
     * @param sx x offset in the source
     * @param sy y offset in the source
     * @param w width of area to draw
     * @param h height of area to draw.
     * @param x destination
     * @param y destination
     * @param anchor location of the anchor point
     * @param rop drawing operation.
     * @see Image
     */
    @Override
    public void drawRegionRop(Image src, int sx, int sy, int w, int h, int x, int y, int anchor, int rop) {
        x = adjustX(x, w, anchor);
        y = adjustY(y, h, anchor);
        if (src == null) bitBlt(displayBuf, width, height, sx, sy, displayBuf, width, height, x, y, w, h, rop);
        else bitBlt(src.getData(), src.getWidth(), src.getHeight(), sx, sy, displayBuf, width, height, x, y, w, h, rop);
    }

    /**
     * Draw the specified region of the source image to the graphics surface
     * after applying the requested transformation, use the supplied rop.
     * <br>NOTE: When calculating the anchor point this method assumes that
     * a transformed version of the source width/height should be used.
     * @param src The source image
     * @param sx x coordinate of the region
     * @param sy y coordinate of the region
     * @param w width of the region
     * @param h height of the region
     * @param transform the required transform
     * @param x x coordinate of the anchor point
     * @param y y coordinate of the anchor point
     * @param anchor type of anchor
     * @param rop raster operation used to draw the output.
     */
    @Override
    @SuppressWarnings("Duplicates")
    public void drawRegionRop(Image src, int sx, int sy, int w, int h, int transform, int x, int y, int anchor, int rop) {
        // Check for common optimized case...
        if (transform == 0) {
            drawRegionRop(src, sx, sy, w, h, x, y, anchor, rop);
            return;
        }

        byte[] inData = src.getData();
        int inWidth = src.getWidth();
        int inHeight = src.getHeight();

        // Transform matrix
        int x1 = 1;
        int y1 = 0;
        int x2 = 0;
        int y2 = 1;
        // Transformed version of width/height.
        int ow = w;
        int oh = h;
        switch (transform) {
            case TRANS_MIRROR:
                x1 = -1;
                break;
            case TRANS_MIRROR_ROT180:
                y2 = -1;
                break;
            case TRANS_MIRROR_ROT270:
                x1 = 0;
                y1 = 1;
                x2 = 1;
                y2 = 0;
                ow = h;
                oh = w;
                break;
            case TRANS_MIRROR_ROT90:
                x1 = 0;
                y1 = -1;
                x2 = -1;
                y2 = 0;
                ow = h;
                oh = w;
                break;
            case TRANS_ROT180:
                x1 = -1;
                y2 = -1;
                break;
            case TRANS_ROT270:
                x1 = 0;
                y1 = 1;
                x2 = -1;
                y2 = 0;
                ow = h;
                oh = w;
                break;
            case TRANS_ROT90:
                x1 = 0;
                y1 = -1;
                x2 = 1;
                y2 = 0;
                ow = h;
                oh = w;
                break;
        }
        // Sort out the anchor point.
        x = adjustX(x, ow, anchor);
        y = adjustY(y, oh, anchor);

        // perform the transformation.
        // We rotate around the centre point
        int cx = (w + 1) / 2;
        int cy = (h + 1) / 2;
        // Setup the input centre point
        int sxbase = sx + cx;
        int sybase = sy + cy;
        // Setup the output centre point. Note that we use transformed rounding.
        // If we don't do this the for even widths/heights we end up with a
        // on symetric rotation.
        int xbase = x + (ow + x1 + y1) / 2;
        int ybase = y + (oh + x2 + y2) / 2;
        // Now loop through the input region...
        int iy = -cy;
        int iye = iy + h;
        while (iy < iye) {
            int iyy1 = iy * y1;
            int iyy2 = iy * y2;
            int ix = -cx;
            int ixe = ix + w;
            while (ix < ixe) {
                int ox = ix * x1 + iyy1 + xbase;
                int oy = ix * x2 + iyy2 + ybase;
                bitBlt(inData, inWidth, inHeight, sxbase + ix, sybase + iy, displayBuf, width, height, ox, oy, 1, 1, rop);
                ix++;
            }
            iy++;
        }

    }

    /**
     * Return the currently selected font object.
     * @return Current font.
     */
    @Override
    public Font getFont()
    {
        return font;
    }

    @Override
    public void setFont(Font f)
    {
        font = f;
    }

    /**
     * Set the current drawing color. The value is in the format 0x00RRGGBB.
     * NOTE. Currently only black and white is supported. any non black color
     * is treated as white!
     * @param rgb new color.
     */
    @Override
    public void setColor(int rgb) {
        rgbColor = rgb;
        if (rgbColor == BLACK) {
            pixelRop = ROP_SET;
            textRop = ROP_OR;
        } else {
            pixelRop = ROP_CLEAR;
            textRop = ROP_ANDINVERTED;
        }
    }

    /**
     * Sets the current color to the specified RGB values.
     * @param red the red component
     * @param green the green component
     * @param blue the blue
     * @throws IllegalArgumentException if any of the color components
     * are outside of range <code>0-255</code>
     * @see #getColor
     */
    @Override
    public void setColor(int red, int green, int blue) {
        if ((red < 0) || (red > 255)
                || (green < 0) || (green > 255)
                || (blue < 0) || (blue > 255)) {
            throw new IllegalArgumentException("bad color value");
        }
        setColor((red << 16) | (green << 8) | blue);
    }

    /**
     * Return the current rgb color.
     * @return current color.
     */
    public int getColor() {// TODO: 19.9.16 add support
        return rgbColor;
    }

    /**
     * Draws the specified String using the current font and color. x and y
     * give the location of the anchor point.
     * @param str the String to be drawn
     * @param x the x coordinate of the anchor point
     * @param y the y coordinate of the anchor point
     * @param anchor the anchor point for positioning the text
     */
    @Override
    public void drawString(String str, int x, int y, int anchor) {
        drawString(str, x, y, anchor, false);
    }

    /**
     * Draw a substring to the graphics surface using the current color.
     * @param str the base string
     * @param offset the start of the sub string
     * @param len the length of the sub string
     * @param x the x coordinate of the anchor point
     * @param y the x coordinate of the anchor point
     * @param anchor the anchor point used to position the text.
     */
    @Override
    public void drawSubstring(String str, int offset, int len,
                              int x, int y, int anchor) {
        // will throw NullPointerException
        int strLen = str.length();
        if ((offset < 0) || (offset > strLen)
                || (len < 0) || (len > strLen)
                || ((offset + len) < 0) || ((offset + len) > strLen))
        {
            throw new StringIndexOutOfBoundsException();
        }

        drawString(str.substring(offset, offset + len), x, y, anchor);
    }

    /**
     * Draw a single character to the graphics surface using the current color.
     * @param character the character to draw
     * @param x the x coordinate of the anchor point
     * @param y the x coordinate of the anchor point
     * @param anchor the anchor point used to position the text.
     */
    @Override
    public void drawChar(char character, int x, int y, int anchor) {
        drawString(new String(new char[]{character}), x, y, anchor);
    }

    /**
     * Draw a series of characters to the graphics surface using the current color.
     * @param data the characters
     * @param offset the start of the characters to be drawn
     * @param length the length of the character string to draw
     * @param x the x coordinate of the anchor point
     * @param y the x coordinate of the anchor point
     * @param anchor the anchor point used to position the text.
     */
    @Override
    public void drawChars(char[] data, int offset, int length,
                          int x, int y, int anchor) {
        // this will throw NullPointerException if data == null
        int chLen = data.length;

        if ((offset < 0) || (offset > chLen)
                || (length < 0) || (length > chLen)
                || ((offset + length) < 0) || ((offset + length) > chLen))
        {
            throw new ArrayIndexOutOfBoundsException();
        }

        drawString(new String(data, offset, length), x, y, anchor);

    }

    /**
     * Draw the specified region of the supplied image to the graphics surface.
     * NOTE: Transforms are not currently supported.
     * @param src image to draw (may be null for ops that do not require input.
     * @param sx x offset to the region
     * @param sy y offset to the region
     * @param w width of the region
     * @param h height of the region
     * @param transform
     * @param x destination
     * @param y destination
     * @param anchor location of the anchor point
     * @see Image
     */
    @Override
    public void drawRegion(Image src,
                           int sx, int sy,
                           int w, int h,
                           int transform,
                           int x, int y,
                           int anchor) {
        drawRegionRop(src, sx, sy, w, h, transform, x, y, anchor, ROP_COPY);
    }

    /**
     * Draw the specified image to the graphics surface, using the supplied rop.
     * @param src image to draw (may be null for ops that do not require input.
     * @param x destination
     * @param y destination
     * @param anchor location of the anchor point
     * @see Image
     */
    @Override
    public void drawImage(Image src, int x, int y, int anchor) {
        drawRegionRop(src, 0, 0, src.getWidth(), src.getHeight(), x, y, anchor, ROP_COPY);
    }

    /**
     * Method to set a pixel to screen.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    private void setPixel(int x, int y) {
        bitBlt(displayBuf, width, height, 0, 0, displayBuf, width, height, x, y, 1, 1, pixelRop);
    }

    /**
     * Draw a line between the specified points, using the current color and style.
     * @param x0 x start point
     * @param y0 y start point
     * @param x1 x end point
     * @param y1 y end point
     */
    @Override
    public void drawLine(int x0, int y0, int x1, int y1) {
        drawLine(x0, y0, x1, y1, strokeStyle);
    }

    @SuppressWarnings("Duplicates")
    private void drawLine(int x0, int y0, int x1, int y1, int style) {
        // Uses Bresenham's line algorithm
        int dy = y1 - y0;
        int dx = x1 - x0;
        int stepx, stepy;
        boolean skip = false;
        if (style == SOLID && (dx == 0 || dy == 0)) {
            // Special case horizontal and vertical lines
            if (dx <= 0) {
                x0 = x1;
                dx = -dx;
            }
            if (dy <= 0) {
                y0 = y1;
                dy = -dy;
            }

            bitBlt(displayBuf, width, height, x0, y0, displayBuf, width, height, x0, y0,
                    dx + 1, dy + 1, (rgbColor == BLACK ? ROP_SET : ROP_CLEAR));
            return;
        }
        if (dy < 0) {
            dy = -dy;
            stepy = -1;
        } else {
            stepy = 1;
        }
        if (dx < 0) {
            dx = -dx;
            stepx = -1;
        } else {
            stepx = 1;
        }
        dy <<= 1; // dy is now 2*dy
        dx <<= 1; // dx is now 2*dx

        setPixel(x0, y0);
        if (dx > dy) {
            int fraction = dy - (dx >> 1);  // same as 2*dy - dx
            while (x0 != x1) {
                if (fraction >= 0) {
                    y0 += stepy;
                    fraction -= dx; // same as fraction -= 2*dx
                }
                x0 += stepx;
                fraction += dy; // same as fraction -= 2*dy
                if ((style == SOLID) || !skip)
                    setPixel(x0, y0);
                skip = !skip;
            }
        } else {
            int fraction = dx - (dy >> 1);
            while (y0 != y1) {
                if (fraction >= 0) {
                    x0 += stepx;
                    fraction -= dy;
                }
                y0 += stepy;
                fraction += dx;
                if ((style == SOLID) || !skip)
                    setPixel(x0, y0);
                skip = !skip;
            }
        }
    }

    /**
     * Draw an arc, using the current color and style.
     * @param x
     * @param y
     * @param width
     * @param height
     * @param startAngle
     * @param arcAngle
     */
    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        drawArc(x, y, width, height, startAngle, arcAngle, strokeStyle, false);
    }

    /**
     * Draw a filled arc, using the current color.
     * @param x
     * @param y
     * @param width
     * @param height
     * @param startAngle
     * @param arcAngle
     */
    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        // drawArc is for now only SOLID
        drawArc(x, y, width, height, startAngle, arcAngle, SOLID, true);
    }

    @SuppressWarnings("Duplicates")
    private void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle,
                         int style, boolean fill) {
        // Scale up width and height to create more accurate ellipse form
        int xscale = (width < height) ? ARC_ACC : ((ARC_ACC * width + (width >> 1)) / height);
        int yscale = (width < height) ? ((ARC_ACC * height + (height >> 1)) / width) : ARC_ACC;
        // Calculate x, y center and radius from upper left corner
        int x0 = x + (width >> 1);
        int y0 = y + (height >> 1);
        int radius = (width < height) ? (width >> 1) : (height >> 1);
        if (arcAngle >= 360 || arcAngle <= -360) {
            drawTheCircle(radius, style, fill, xscale, yscale, x0, y0);
            return;
        }

        while (startAngle < 0)
            startAngle += 360;
        while (startAngle > 360)
            startAngle -= 360;
        while (arcAngle > 360)
            arcAngle -= 360;
        while (arcAngle < -360)
            arcAngle += 360;  // negative arc angle is OK
        // Check and set start and end angle
        int endAngle = startAngle + arcAngle;
        if (arcAngle >= 0) {
            if (endAngle > 360) { // need 2 segments
                drawTheArc(radius, style, fill, xscale, yscale, startAngle, 360, x0, y0);
                drawTheArc(radius, style, fill, xscale, yscale, 0, endAngle - 360, x0, y0);
            } else drawTheArc(radius, style, fill, xscale, yscale, startAngle, endAngle, x0, y0);
        } /* else draw arc from end to start */ else if (endAngle < 0) { // need 2 segments
            drawTheArc(radius, style, fill, xscale, yscale, endAngle + 360, 360, x0, y0);
            drawTheArc(radius, style, fill, xscale, yscale, 0, startAngle, x0, y0);
        } else drawTheArc(radius, style, fill, xscale, yscale, endAngle, startAngle, x0, y0);

    }

    @SuppressWarnings("Duplicates")
    private void drawTheCircle(int radius, int style, boolean fill, int xscale,
                               int yscale, int x0, int y0) {
        // Initialize scaled up Bresenham's circle algorithm
        int f = (1 - ARC_ACC * radius);
        int ddF_x = 0;
        int ddF_y = -2 * ARC_ACC * radius;
        int xc = 0;
        int yc = ARC_ACC * radius;
        int dotskip = 0;
        while (xc < yc) {
            if (f >= 0) {
                yc--;
                ddF_y += 2;
                f += ddF_y;
            }

            xc++;
            ddF_x += 2;
            f += ddF_x + 1;

            // Skip points for dotted version
            dotskip = (dotskip + 1) % (2 * ARC_ACC);
            if ((style == DOTTED) && !fill && (dotskip < ((2 * ARC_ACC) - 1)))
                continue;

            // Scale down again
            int xxp = (xc * xscale + (xscale >> 1)) / (ARC_ACC * ARC_ACC);
            int xyp = (xc * yscale + (yscale >> 1)) / (ARC_ACC * ARC_ACC);
            int yyp = (yc * yscale + (yscale >> 1)) / (ARC_ACC * ARC_ACC);
            int yxp = (yc * xscale + (xscale >> 1)) / (ARC_ACC * ARC_ACC);

            if (fill) {
                /* TDO: Optimize more by drawing horizontal lines */
                drawLine(x0, y0, x0 + yxp, y0 - xyp, style); // 0   - 45 degrees
                drawLine(x0, y0, x0 + xxp, y0 - yyp, style); // 45  - 90 degrees
                drawLine(x0, y0, x0 - xxp, y0 - yyp, style); // 90  - 135 degrees
                drawLine(x0, y0, x0 - yxp, y0 - xyp, style); // 135 - 180 degrees
                drawLine(x0, y0, x0 - yxp, y0 + xyp, style); // 180 - 225 degrees
                drawLine(x0, y0, x0 - xxp, y0 + yyp, style); // 225 - 270 degrees
                drawLine(x0, y0, x0 + xxp, y0 + yyp, style); // 270 - 315 degrees
                drawLine(x0, y0, x0 + yxp, y0 + xyp, style); // 315 - 360 degrees
            } else {
                setPixel(x0 + yxp, y0 - xyp); // 0   - 45 degrees
                setPixel(x0 + xxp, y0 - yyp); // 45  - 90 degrees
                setPixel(x0 - xxp, y0 - yyp); // 90  - 135 degrees
                setPixel(x0 - yxp, y0 - xyp); // 135 - 180 degrees
                setPixel(x0 - yxp, y0 + xyp); // 180 - 225 degrees
                setPixel(x0 - xxp, y0 + yyp); // 225 - 270 degrees
                setPixel(x0 + xxp, y0 + yyp); // 270 - 315 degrees
                setPixel(x0 + yxp, y0 + xyp); // 315 - 360 degrees
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void drawTheArc(int radius, int style, boolean fill, int xscale,
                            int yscale, int startAngle, int endAngle, int x0, int y0) {
        // Initialize scaled up Bresenham's circle algorithm
        int f = (1 - ARC_ACC * radius);
        int ddF_x = 0;
        int ddF_y = -2 * ARC_ACC * radius;
        int xc = 0;
        int yc = ARC_ACC * radius;
        int dotskip = 0;
        while (xc < yc) {
            if (f >= 0) {
                yc--;
                ddF_y += 2;
                f += ddF_y;
            }

            xc++;
            ddF_x += 2;
            f += ddF_x + 1;

            // Skip points for dotted version
            dotskip = (dotskip + 1) % (2 * ARC_ACC);
            if ((style == DOTTED) && !fill && (dotskip < ((2 * ARC_ACC) - 1)))
                continue;

            // Scale down again
            int xxp = (xc * xscale + (xscale >> 1)) / (ARC_ACC * ARC_ACC);
            int xyp = (xc * yscale + (yscale >> 1)) / (ARC_ACC * ARC_ACC);
            int yyp = (yc * yscale + (yscale >> 1)) / (ARC_ACC * ARC_ACC);
            int yxp = (yc * xscale + (xscale >> 1)) / (ARC_ACC * ARC_ACC);

            // Calculate angle for partly circles / ellipses
            // NOTE: Below, (float) should not be needed. Not sure why Math.round() only accepts float.
            int tp = (int) Math.round(Math.toDegrees(Math.atan2(yc, xc)));
            if (fill) {
                /* TDO: Optimize more by drawing horizontal lines */
                if (((90 - tp) >= startAngle) && ((90 - tp) <= endAngle))
                    drawLine(x0, y0, x0 + yxp, y0 - xyp, style); // 0   - 45 degrees
                if ((tp >= startAngle) && (tp <= endAngle))
                    drawLine(x0, y0, x0 + xxp, y0 - yyp, style); // 45  - 90 degrees
                if (((180 - tp) >= startAngle) && ((180 - tp) <= endAngle))
                    drawLine(x0, y0, x0 - xxp, y0 - yyp, style); // 90  - 135 degrees
                if (((180 - (90 - tp)) >= startAngle) && ((180 - (90 - tp)) <= endAngle))
                    drawLine(x0, y0, x0 - yxp, y0 - xyp, style); // 135 - 180 degrees
                if (((270 - tp) >= startAngle) && ((270 - tp) <= endAngle))
                    drawLine(x0, y0, x0 - yxp, y0 + xyp, style); // 180 - 225 degrees
                if (((270 - (90 - tp)) >= startAngle) && ((270 - (90 - tp)) <= endAngle))
                    drawLine(x0, y0, x0 - xxp, y0 + yyp, style); // 225 - 270 degrees
                if (((360 - tp) >= startAngle) && ((360 - tp) <= endAngle))
                    drawLine(x0, y0, x0 + xxp, y0 + yyp, style); // 270 - 315 degrees
                if (((360 - (90 - tp)) >= startAngle) && ((360 - (90 - tp)) <= endAngle))
                    drawLine(x0, y0, x0 + yxp, y0 + xyp, style); // 315 - 360 degrees
            } else {
                if (((90 - tp) >= startAngle) && ((90 - tp) <= endAngle))
                    setPixel(x0 + yxp, y0 - xyp); // 0   - 45 degrees
                if ((tp >= startAngle) && (tp <= endAngle))
                    setPixel(x0 + xxp, y0 - yyp); // 45  - 90 degrees
                if (((180 - tp) >= startAngle) && ((180 - tp) <= endAngle))
                    setPixel(x0 - xxp, y0 - yyp); // 90  - 135 degrees
                if (((180 - (90 - tp)) >= startAngle) && ((180 - (90 - tp)) <= endAngle))
                    setPixel(x0 - yxp, y0 - xyp); // 135 - 180 degrees
                if (((270 - tp) >= startAngle) && ((270 - tp) <= endAngle))
                    setPixel(x0 - yxp, y0 + xyp); // 180 - 225 degrees
                if (((270 - (90 - tp)) >= startAngle) && ((270 - (90 - tp)) <= endAngle))
                    setPixel(x0 - xxp, y0 + yyp); // 225 - 270 degrees
                if (((360 - tp) >= startAngle) && ((360 - tp) <= endAngle))
                    setPixel(x0 + xxp, y0 + yyp); // 270 - 315 degrees
                if (((360 - (90 - tp)) >= startAngle) && ((360 - (90 - tp)) <= endAngle))
                    setPixel(x0 + yxp, y0 + xyp); // 315 - 360 degrees
            }
        }
    }

    /**
     * Draw a rounded rectangle.
     * @param x
     * @param y
     * @param width
     * @param height
     * @param arcWidth
     * @param arcHeight
     */
    @Override
    @SuppressWarnings("Duplicates")
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        int xc = x + (width / 2);
        int yc = y + (height / 2);
        int a = arcWidth / 2;
        int b = arcHeight / 2;

        int translateX = (width / 2) - (arcWidth / 2);
        int translateY = (height / 2) - (arcHeight / 2);

        // Draw 4 sides:
        int xDiff = arcWidth / 2;
        int yDiff = arcHeight / 2;
        drawLine(x, y + yDiff, x, y + height - yDiff);
        drawLine(x + width, y + yDiff, x + width, y + height - yDiff);
        drawLine(x + xDiff, y, x + width - xDiff, y);
        drawLine(x + xDiff, y + height, x + width - xDiff, y + height);


        /* e(x,y) = b^2*x^2 + a^2*y^2 - a^2*b^2 */
        int xxx = 0, yyy = b;
        int a2 = a * a, b2 = b * b;
        int crit1 = -(a2 / 4 + a % 2 + b2);
        int crit2 = -(b2 / 4 + b % 2 + a2);
        int crit3 = -(b2 / 4 + b % 2);
        int t = -a2 * yyy; /* e(xxx+1/2,y-1/2) - (a^2+b^2)/4 */
        int dxt = 2 * b2 * xxx, dyt = -2 * a2 * yyy;
        int d2xt = 2 * b2, d2yt = 2 * a2;

        while (yyy >= 0 && xxx <= a) {
            setPixel(xc + xxx + translateX, yc + yyy + translateY); // Q4
            if (xxx != 0 || yyy != 0)
                setPixel(xc - xxx - translateX, yc - yyy - translateY); // Q2
            if (xxx != 0 && yyy != 0) {
                setPixel(xc + xxx + translateX, yc - yyy - translateY); // Q1
                setPixel(xc - xxx - translateX, yc + yyy + translateY); // Q3
            }
            if (t + b2 * xxx <= crit1
                    || /* e(xxx+1,y-1/2) <= 0 */ t + a2 * yyy <= crit3)      /* e(xxx+1/2,y) <= 0 */

            {
                xxx++;
                dxt += d2xt;
                t += dxt;
            } // incx()
            else if (t - a2 * yyy > crit2) /* e(xxx+1/2,y-1) > 0 */

            {
                yyy--;
                dyt += d2yt;
                t += dyt;
            } else {
                {
                    xxx++;
                    dxt += d2xt;
                    t += dxt;
                } // incx()
                {
                    yyy--;
                    dyt += d2yt;
                    t += dyt;
                }
            }
        }
    }

    /**
     * Draw a rectangle using the current color and style.
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @Override
    @SuppressWarnings("Duplicates")
    public void drawRect(int x, int y, int width, int height) {
        if ((width < 0) || (height < 0))
            return;

        if (height == 0 || width == 0) {
            drawLine(x, y, x + width, y + height);
        } else {
            drawLine(x, y, x + width - 1, y);
            drawLine(x + width, y, x + width, y + height - 1);
            drawLine(x + width, y + height, x + 1, y + height);
            drawLine(x, y + height, x, y + 1);
        }
    }

    /**
     * Draw a filled rectangle using the current color.
     * @param x
     * @param y
     * @param w
     * @param h
     */
    @Override
    public void fillRect(int x, int y, int w, int h) {
        if ((w < 0) || (h < 0)) return;
        bitBlt(displayBuf, width, height, x, y, displayBuf, width, height, x, y, w, h, (rgbColor == BLACK ? ROP_SET : ROP_CLEAR));
    }

    /**
     * Return the current stroke style.
     * @return current style.
     */
    @Override
    public int getStrokeStyle() {
        return strokeStyle;
    }

    /**
     * Set the stroke style to be used for drawing operations.
     * @param style new style.
     */
    @Override
    public void setStrokeStyle(int style) {
        if (style != SOLID && style != DOTTED) {
            throw new IllegalArgumentException();
        }
        strokeStyle = style;
    }

    /**
     * Copy one rectangular area of the drawing surface to another.
     * @param sx Source x
     * @param sy Source y
     * @param w Source width
     * @param h Source height
     * @param x Destination x
     * @param y Destination y
     * @param anchor location of the anchor point of the destination.
     */
    @Override
    public void copyArea(int sx, int sy,
                         int w, int h,
                         int x, int y, int anchor) {
        x = adjustX(x, w, anchor);
        y = adjustY(y, h, anchor);
        bitBlt(displayBuf, width, height, sx, sy, displayBuf, width, height, x, y, w, h, ROP_COPY);
    }
}
