package eu.codetopic.anty.lejos.car.hardware.scan;

import eu.codetopic.anty.lejos.car.hardware.Hardware;
import eu.codetopic.anty.lejos.utils.Range;
import eu.codetopic.anty.lejos.utils.ScannerMotor;
import eu.codetopic.anty.lejos.utils.ScannerMotor.Side;
import eu.codetopic.anty.lejos.utils.Utils;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import eu.codetopic.anty.lejos.utils.draw.drawer.GraphicsDrawer;
import lejos.robotics.RangeFinder;
import lejos.robotics.RangeReading;
import lejos.robotics.RangeReadings;
import lejos.robotics.RangeScanner;

import static eu.codetopic.anty.lejos.car.Constants.*;

public final class FastRangeScanner {

    private static final float MULTIPLE_DIFF = 360f / (float) SCAN_AROUND_ROTATION;

    private FastRangeScanner() {
    }

    public static void graphicsAroundScan(Canvas canvas, Hardware hardware, float motorSpeed, boolean lines) {
        GraphicsDrawer drawer = canvas.getGraphicsDrawer();
        drawer.clear();
        drawer.drawString("Scanning...", drawer.getWidth() / 2, drawer.getHeight() / 2,
                GraphicsDrawer.HCENTER | GraphicsDrawer.VCENTER);
        canvas.apply();
        RangeReadings results = aroundScan(hardware, motorSpeed);

        drawer.clear();
        drawer.drawString("Drawing...", drawer.getWidth() / 2, drawer.getHeight() / 2,
                GraphicsDrawer.HCENTER | GraphicsDrawer.VCENTER);
        canvas.apply();
        drawScan(canvas, results, hardware.getScan().getSensorType().getDistanceMaxValue(), lines);
        while (!Thread.interrupted()) Utils.sleep(150);
    }

    public static RangeReadings aroundScan(Hardware hardware, float speed) {
        ScannerMotor top = hardware.getScan().getScannerMotor();
        top.setSpeed(SCAN_SPEED_FASTEST_ALLOWED);
        top.rotateTo(Side.CENTER);
        top.invertRotationMode();
        return scan(hardware, speed, new Range(top.getModifiedTachoCount(), Side.CENTER.getAngle()));
    }

    public static RangeReadings scan(Hardware hardware, float speed, Range scanRange) {
        Scan scan = hardware.getScan();
        ScannerMotor top = scan.getScannerMotor();
        RangeReadings results = new RangeReadings(0);

        top.setSpeed(SCAN_SPEED_FASTEST_ALLOWED);
        top.rotateTo((int) scanRange.getLower());
        top.setSpeed(speed);
        top.rotateTo((int) scanRange.getHigher(), true);

        while (top.isMoving() && !Thread.currentThread().isInterrupted()) {
            results.add(new RangeReading(MULTIPLE_DIFF * top.getModifiedPosition(), scan.fetchDistance(false)));
        }

        return results;
    }
    
    public static void drawScan(Canvas canvas, RangeReadings results, double maxRange, boolean lines) {
        GraphicsDrawer drawer = canvas.getGraphicsDrawer();
        int width = drawer.getWidth(), height = drawer.getHeight();
        int xAdd, yAdd;
        final double r;
        if (width < height) {
            r = width / 2d;
            xAdd = 0;
            yAdd = (height - width) / 2;
        } else {
            r = height / 2d;
            xAdd = (width - height) / 2;
            yAdd = 0;
        }

        drawer.clear();
        drawer.setStrokeStyle(GraphicsDrawer.DOTTED);
        drawer.drawRect((int) (r + xAdd - 5), (int) (r + yAdd - 5), 10, 10);
        drawer.setStrokeStyle(GraphicsDrawer.SOLID);

        if (!results.isEmpty()) {
            if (lines) {
                RangeReading firstCapture = results.get(0);
                Point first = firstCapture.getRange() == Float.POSITIVE_INFINITY
                        ? null : getPosition(firstCapture, maxRange, r);
                Point last = null;
                for (int i = 1, size = results.size(); i < size; i++) {
                    if (Thread.currentThread().isInterrupted()) break;
                    RangeReading capture = results.get(i);
                    if (capture.getRange() == Float.POSITIVE_INFINITY) {
                        last = null;
                        continue;
                    }
                    Point pos = getPosition(capture, maxRange, r);
                    if (last != null)
                        drawer.drawLine(last.getX() + xAdd, last.getY() + yAdd, pos.getX() + xAdd, pos.getY() + yAdd);
                    last = pos;
                }
                if (last != null && first != null)
                    drawer.drawLine(last.getX() + xAdd, last.getY() + yAdd, first.getX() + xAdd, first.getY() + yAdd);
            } else {
                for (RangeReading capture : results) {
                    if (Thread.currentThread().isInterrupted()) break;
                    Point pos = getPosition(capture, maxRange, r);
                    drawer.drawRect(pos.getX() + xAdd - 1, pos.getY() + yAdd - 1, 2, 2);
                }
            }
        }
        canvas.apply();
    }

    private static Point getPosition(RangeReading capture, double maxRange, double r) {
        double angle = capture.getAngle();
        double range = capture.getRange();

        return new Point((int) (Math.cos((angle - 90) * DEG_TO_RAD_MUL) * (r * (range / maxRange)) + r),
                (int) (Math.sin((angle - 90) * DEG_TO_RAD_MUL) * (r * (range / maxRange)) + r));
    }

    private static final class Point {

        private final int x;
        private final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public static class RangeScannerImpl implements RangeScanner {

        private final Hardware hardware;
        private final float speed;

        public RangeScannerImpl(Hardware hardware, float speed) {
            this.hardware = hardware;
            this.speed = speed;
        }

        @Override
        public RangeReadings getRangeValues() {
            return aroundScan(hardware, speed);
        }

        @Override
        public void setAngles(float[] angles) {
            throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public RangeFinder getRangeFinder() {
            return null;
        }
    }
}
