package eu.codetopic.anty.lejos.utils.draw.drawer;

public class CommonDrawerImpl implements CommonDrawer {

    protected final int height;
    protected final int width;

    protected final int screenMemWidth;
    protected final byte[] displayBuf;

    public CommonDrawerImpl(int height, int width) {
        this(height, width, generateBuffer(height, width));
    }

    public CommonDrawerImpl(int height, int width, byte[] dst) {
        this.height = height;
        this.width = width;

        screenMemWidth = (width + 7) / 8;
        displayBuf = dst;
    }

    public static byte[] generateBuffer(int height, int width) {
        int screenMemWidth = (width + 7) / 8;
        int lcdBufferLength = screenMemWidth * height;
        return new byte[lcdBufferLength];
    }

    @Override
    public void clear() {
        bitBlt(displayBuf, width, height, 0, 0, 0, 0, width, height, ROP_CLEAR);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public byte[] getContent() {
        return displayBuf;
    }

    @Override
    public void setPixel(int x, int y, int color) {
        bitBlt(displayBuf, width, height, 0, 0, displayBuf, width,
                height, x, y, 1, 1,(color == 1 ? ROP_SET : ROP_CLEAR));
    }

    @Override
    public int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= width) return 0;
        int bit = (x & 0x7);

        int index = (y)*screenMemWidth + x/8;
        return ((displayBuf[index] >> bit) & 1);
    }
    @Override
    public void bitBlt(byte[] src, int sw, int sh, int sx, int sy, int dx, int dy, int w, int h, int rop) {
        bitBlt(src, sw, sh, sx, sy, displayBuf, width, height, dx, dy, w, h, rop);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void bitBlt(byte[] src, int sw, int sh, int sx, int sy, byte dst[], int dw, int dh, int dx, int dy, int w, int h, int rop)
    {
        /* This is a partial implementation of the BitBlt algorithm. It provides a
         * complete set of raster operations and handles partial and fully aligned
         * images correctly. Overlapping source and destination images is also
         * supported. It does not performing mirroring. The code was converted
         * from an initial Java implementation and has not been optimized for C.
         * The general mechanism is to perform the block copy with Y as the inner
         * loop (because on the display the bits are packed y-wise into a byte). We
         * perform the various rop cases by reducing the operation to a series of
         * AND and XOR operations. Each step is controlled by a byte in the rop code.
         * This mechanism is based upon that used in the X Windows system server.
         */
        // Clip to source and destination
        int trim;
        if (dx < 0) {
            trim = -dx;
            dx = 0;
            sx += trim;
            w -= trim;
        }
        if (dy < 0) {
            trim = -dy;
            dy = 0;
            sy += trim;
            h -= trim;
        }
        if (sx < 0 || sy < 0) return;
        if (dx + w > dw) w = dw - dx;
        if (sx + w > sw) w = sw - sx;
        if (w <= 0) return;
        if (dy + h > dh) h = dh - dy;
        if (sy + h > sh) h = sh - sy;
        if (h <= 0) return;
        // Setup initial parameters and check for overlapping copy
        int xinc = 1;
        int yinc = 1;
        byte firstBit = 1;
        if (src == dst) {
            // If copy overlaps we use reverse direction
            if (dy > sy) {
                sy = sy + h - 1;
                dy = dy + h - 1;
                yinc = -1;
            }
            if (dx > sx) {
                firstBit = (byte)0x80;
                xinc = -1;
                sx = sx + w - 1;
                dx = dx + w - 1;
            }
        }
        if (src == null)
            src = dst;
        int swb = (sw+7)/8;
        int dwb = (dw+7)/8;
        //if (src == displayBuf)
        //swb = HW_MEM_WIDTH;
        //if (dst == displayBuf)
        //dwb = HW_MEM_WIDTH;
        int inStart = sy*swb;
        int outStart = dy*dwb;
        byte inStartBit = (byte)(1 << (sx & 0x7));
        byte outStartBit = (byte)(1 << (dx & 0x7));
        dwb *= yinc;
        swb *= yinc;
        // Extract rop sub-fields
        byte ca1 = (byte)(rop >> 24);
        byte cx1 = (byte)(rop >> 16);
        byte ca2 = (byte)(rop >> 8);
        byte cx2 = (byte) rop;
        boolean noDst = (ca1 == 0 && cx1 == 0);
        int ycnt;
        // Check for byte aligned case and optimise for it
        if (w >= 8 && inStartBit == firstBit && outStartBit == firstBit) {
            int ix = sx/8;
            int ox = dx/8;
            int byteCnt = w/8;
            ycnt = h;
            while (ycnt-- > 0) {
                int inIndex = inStart + ix;
                int outIndex = outStart + ox;
                int cnt = byteCnt;
                while(cnt-- > 0) {
                    if (noDst) dst[outIndex] = (byte)((src[inIndex] & ca2)^cx2);
                    else {
                        byte inVal = src[inIndex];
                        dst[outIndex] = (byte)((dst[outIndex] & ((inVal & ca1)^cx1)) ^ ((inVal & ca2)^cx2));
                    }
                    outIndex += xinc;
                    inIndex += xinc;
                }
                ix += swb;
                ox += dwb;
            }
            // Do we have a final non byte multiple to do?
            w &= 0x7;
            if (w == 0) {
                //if (dst == displayBuf)
                //update(displayBuf);
                return;
            }
            //inStart = sy*swb;
            //outStart = dy*dwb;
            sx += byteCnt*8;
            dx += byteCnt*8;
        }
        // General non byte aligned case
        int ix = sx/8;
        int ox = dx/8;
        ycnt = h;
        while(ycnt-- > 0) {
            int inIndex = inStart + ix;
            byte inBit = inStartBit;
            byte inVal = src[inIndex];
            byte inAnd = (byte)((inVal & ca1)^cx1);
            byte inXor = (byte)((inVal & ca2)^cx2);
            int outIndex = outStart + ox;
            byte outBit = outStartBit;
            byte outPixels = dst[outIndex];
            int cnt = w;
            while(true) {
                if (noDst) {
                    if ((inXor & inBit) != 0) outPixels |= outBit;
                    else outPixels &= ~outBit;
                } else {
                    byte resBit = (byte)((outPixels & ((inAnd & inBit) != 0 ? outBit : 0))^((inXor & inBit) != 0 ? outBit : 0));
                    outPixels = (byte)((outPixels & ~outBit) | resBit);
                }
                if (--cnt <= 0) break;
                if (xinc > 0) {
                    inBit <<= 1;
                    outBit <<= 1;
                } else {
                    inBit >>= 1;
                    outBit >>= 1;
                }
                if (inBit == 0) {
                    inBit = firstBit;
                    inIndex += xinc;
                    inVal = src[inIndex];
                    inAnd = (byte)((inVal & ca1)^cx1);
                    inXor = (byte)((inVal & ca2)^cx2);
                }
                if (outBit == 0) {
                    dst[outIndex] = outPixels;
                    outBit = firstBit;
                    outIndex += xinc;
                    outPixels = dst[outIndex];
                }
            }
            dst[outIndex] = outPixels;
            inStart += swb;
            outStart += dwb;
        }
        //if (dst == displayBuf)
        //update(displayBuf);
    }
}
