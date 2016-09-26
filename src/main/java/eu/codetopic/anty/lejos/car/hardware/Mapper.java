package eu.codetopic.anty.lejos.car.hardware;

import eu.codetopic.anty.lejos.car.Constants;
import eu.codetopic.anty.lejos.car.hardware.scan.FastRangeScanner;
import eu.codetopic.anty.lejos.utils.draw.MapDrawer;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import lejos.robotics.RangeReading;
import lejos.robotics.RangeReadings;
import lejos.robotics.geometry.Point;
import lejos.robotics.geometry.Rectangle;
import lejos.robotics.localization.MCLPoseProvider;
import lejos.robotics.mapping.OccupancyGridMap;
import lejos.robotics.mapping.RangeMap;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.Pose;

import static eu.codetopic.anty.lejos.car.Constants.DEG_TO_RAD_MUL;

public final class Mapper {

    private static final int MAP_SIZE = 100;

    private Mapper() {
    }

    /*public static void start(Canvas canvas, Hardware hardware) {
        Wheels wheels = hardware.getWheels();
        MCLPoseProvider poseProvider = new MCLPoseProvider(wheels.movePilot,
                new RangeScannerImpl(hardware, Constants.SCAN_SPEED_FASTEST_ALLOWED), new RangeMap() {
            Rectangle rectangle = new Rectangle(-100f, -100f, 200f, 200f);

            @Override
            public float range(Pose pose) {
                return Float.MAX_VALUE;
            }

            @Override
            public boolean inside(Point p) {
                return rectangle.contains(p);
            }

            @Override
            public Rectangle getBoundingRect() {
                return rectangle;
            }
        }, 100, 15);
        poseProvider.setPose(new Pose(0f, 0f, 0f));
        poseProvider.update();

        PoseDrawer poseDrawer = new PoseDrawer(canvas, poseProvider, true);
        poseDrawer.start();

        while (!Thread.interrupted()) {
            Delay.msDelay(1000);
            poseProvider.update();
            poseDrawer.notifyPoseChanged();
        }
        Utils.stopThread(poseDrawer);
    }*/

    public static void start(Canvas canvas, Hardware hardware) {
        MovePilot pilot = hardware.getWheels().movePilot;
        OccupancyGridMap map = new OccupancyGridMap(MAP_SIZE, MAP_SIZE, -1d, 1d, 100d);
        MapDrawer mapDrawer = new MapDrawer(canvas, null, map, true);

        MCLPoseProvider poseProvider = new MCLPoseProvider(pilot, null, new RangeMap() {
            Rectangle rectangle = new Rectangle(0, 0, MAP_SIZE, MAP_SIZE);

            @Override
            public float range(Pose pose) {
                return Float.POSITIVE_INFINITY;// TODO: 25.9.16 make it work better
            }

            @Override
            public boolean inside(Point p) {
                return rectangle.contains(p);
            }

            @Override
            public Rectangle getBoundingRect() {
                return rectangle;
            }
        }, 500, 15);
        poseProvider.setPose(new Pose(MAP_SIZE / 2f, MAP_SIZE / 2f, 0f));

        while (!Thread.interrupted()) {
            RangeReadings readings = FastRangeScanner.aroundScan(hardware, Constants.SCAN_SPEED_FASTEST_ALLOWED);

            RangeReadings filteredReadings = new RangeReadings(0);
            //noinspection Convert2streamapi
            for (RangeReading reading : readings)
                if (reading.getRange() != Float.POSITIVE_INFINITY) filteredReadings.add(reading);
            boolean success = poseProvider.update(filteredReadings);

            System.out.println("MCLPoseProvider update success: " + success);

            Pose pose = poseProvider.getPose();

            int maxX = map.getWidth() - 1, maxY = map.getHeight() - 1;
            for (RangeReading reading : readings) {
                if (reading.getRange() == Float.POSITIVE_INFINITY) continue;

                double angle = reading.getAngle();
                double proximity = reading.getRange();
                int x = (int) (pose.getX() + (Math.cos(angle * DEG_TO_RAD_MUL) * proximity));
                int y = (int) (pose.getY() + (Math.sin(angle * DEG_TO_RAD_MUL) * proximity));

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int mX = x + i, mY = y + j;
                        if (mX < 0 || mX > maxX || mY < 0 || mY > maxY) continue;
                        map.setOccupied(mX, mY, 2);// TODO: 25.9.16 set all visible places as scanned
                        //System.out.println("Writing occupied on: x=" + (x + i) + ", y=" + (y + j));
                    }
                }
            }
            mapDrawer.drawMap(pose);

            pilot.travel(30d);
            pilot.rotate(180d);
            // TODO: 24.9.16 find nearest not scanned space (occupied=0)
            // TODO: 24.9.16 find path to found space
            // TODO: 24.9.16 move using found path

            mapDrawer.drawMap(poseProvider.getPose());

            Thread.yield();
        }

        //debug info
        System.out.println("Final pose: " + poseProvider.getPose());
        StringBuilder sb = new StringBuilder("Final map:");
        for (int i = 0, wLen = map.getWidth(); i < wLen; i++) {
            sb.append("\n");
            for (int j = 0, hLen = map.getHeight(); j < hLen; j++) {
                sb.append(map.getOccupied(i, j)).append(":");
            }
        }
        System.out.println(sb);
    }
}
