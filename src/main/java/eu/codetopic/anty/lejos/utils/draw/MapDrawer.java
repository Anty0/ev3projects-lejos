package eu.codetopic.anty.lejos.utils.draw;

import eu.codetopic.anty.lejos.utils.Utils;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import eu.codetopic.anty.lejos.utils.draw.drawer.GraphicsDrawer;
import lejos.hardware.lcd.Font;
import lejos.robotics.Color;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.mapping.OccupancyGridMap;
import lejos.robotics.navigation.Pose;

import static eu.codetopic.anty.lejos.car.Constants.DEG_TO_RAD_MUL;

public class MapDrawer extends Thread {

    private final Canvas canvas;
    private final PoseProvider poseProvider;
    private final OccupancyGridMap map;
    private final boolean waitOnNotify;
    private volatile boolean notified = true;

    private final GraphicsDrawer drawer;
    private final int width, height, centerX, centerY;

    public MapDrawer(Canvas canvas, PoseProvider poseProvider, OccupancyGridMap map, boolean waitOnNotify) {
        this.canvas = canvas;
        this.poseProvider = poseProvider;
        this.map = map;
        this.waitOnNotify = waitOnNotify;

        this.drawer = this.canvas.getGraphicsDrawer();
        this.width = drawer.getWidth();
        this.height = drawer.getHeight();
        this.centerX = width / 2;
        this.centerY = height / 2;

        setDaemon(true);
        setPriority(3);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (waitOnNotify) {
                Utils.waitWhile(() -> !notified);
                notified = false;
            }
            drawMap();
            Thread.yield();
        }
    }

    public void drawMap() {
        drawMap(poseProvider.getPose());
    }

    public void drawMap(Pose pose) {
        drawer.clear();

        drawer.drawArc(centerX - 12, centerY - 12, 24, 24, 0, 360);

        drawer.setFont(Font.getSmallFont());
        int fontH = Font.getSmallFont().getHeight();
        String[] poseStr = pose.toString().split(" ");
        for (int i = poseStr.length - 1; i >= 0; i--) {
            drawer.drawString(poseStr[i], centerX - 12, centerY - 12 - (fontH * i),
                    GraphicsDrawer.RIGHT | GraphicsDrawer.BOTTOM);
        }
        drawer.setFont(Font.getDefaultFont());

        float angle = pose.getHeading();
        drawer.drawLine(centerX, centerY,
                centerX + (int) (Math.cos((angle - 90) * DEG_TO_RAD_MUL) * 15d),
                centerY + (int) (Math.sin((angle - 90) * DEG_TO_RAD_MUL) * 15d));


        int wStart = (int) pose.getX() - centerX;
        int wOffset = wStart;
        int wEnd = wStart + width;
        if (wStart < 0) wStart = 0;
        if (wEnd > map.getWidth()) wEnd = map.getWidth();

        int hStart = (int) pose.getY() - centerY;
        int hOffset = hStart;
        int hEnd = hStart + height;
        if (hStart < 0) hStart = 0;
        if (hEnd > map.getHeight()) hEnd = map.getHeight();

        for (int w = wStart; w < wEnd; w++) {
            for (int h = hStart; h < hEnd; h++) {
                if (map.isOccupied(w, h)) {
                    drawer.setPixel(w - wOffset, h - hOffset, GraphicsDrawer.BLACK);
                    //System.out.println("Drawing occupied on: x=" + (w - wOffset) + ", y=" + (h - hOffset));
                }
            }
        }

        canvas.apply();
    }

    public void notifyPoseChanged() {
        notified = true;
    }
}