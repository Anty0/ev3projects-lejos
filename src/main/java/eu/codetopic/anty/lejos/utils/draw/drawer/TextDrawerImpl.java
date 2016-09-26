package eu.codetopic.anty.lejos.utils.draw.drawer;

import lejos.hardware.lcd.Font;

public class TextDrawerImpl extends CommonDrawerImpl implements TextDrawer {

    private Font font = Font.getDefaultFont();

    public TextDrawerImpl(int height, int width) {
        super(height, width);
    }

    public TextDrawerImpl(int height, int width, byte[] dst) {
        super(height, width, dst);
    }

    public void drawChar(char c, int x, int y) {
        bitBlt(font.glyphs, font.width * font.glyphCount, font.height, font.width * (c - 32), 0, x * font.glyphWidth, y * font.height, font.width, font.height, ROP_COPY);
    }

    public void drawString(String str, int x, int y) {
        char[] strData = str.toCharArray();
        // Draw the background rect
        bitBlt(null, width, height, 0, 0, x * font.glyphWidth, y * font.height, strData.length * font.glyphWidth, font.height, ROP_CLEAR);
        // and the characters
        for (int i = 0; (i < strData.length); i++)
            bitBlt(font.glyphs, font.width * font.glyphCount, font.height, font.width * (strData[i] - 32), 0, (x + i) * font.glyphWidth, y * font.height, font.width, font.height, ROP_COPY);
    }

    public void drawInt(int i, int x, int y) {
        drawString(Integer.toString(i), x, y);
    }

    public void drawInt(int i, int places, int x, int y) {
        drawString(String.format("%" + places + "d", i), x, y);
    }

    public void drawString(String str, int x, int y, boolean inverted) {
        if (inverted) {
            char[] strData = str.toCharArray();
            // Draw the background rect
            bitBlt(null, getWidth(), getHeight(), 0, 0, x * font.glyphWidth, y * font.height, strData.length * font.glyphWidth, font.height, ROP_SET);
            // and the characters
            for (int i = 0; (i < strData.length); i++)
                bitBlt(font.glyphs, font.width * font.glyphCount, font.height, font.width * (strData[i] - 32), 0, (x + i) * font.glyphWidth, y * font.height, font.width, font.height, ROP_COPYINVERTED);
        } else drawString(str, x, y);
    }

    public void scroll() {
        bitBlt(displayBuf, width, height, 0, font.height,
                0, 0, width, height - font.height, ROP_COPY);
        bitBlt(null, width, height, 0, 0, 0, height - font.height,
                width, font.height, ROP_CLEAR);
    }

    public void clear(int x, int y, int n) {
        bitBlt(null, width, height, 0, 0, x * font.glyphWidth, y * font.height,
                n * font.glyphWidth, font.height, ROP_CLEAR);
    }

    public void clear(int y) {
        bitBlt(null, width, height, 0, 0, 0, y * font.height,
                getWidth(), font.height, ROP_CLEAR);
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public int getTextWidth() {
        return width / font.width;
    }

    @Override
    public int getTextHeight() {
        return height / font.height;
    }
}
