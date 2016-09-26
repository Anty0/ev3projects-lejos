package eu.codetopic.anty.lejos.car.hardware;

import eu.codetopic.anty.lejos.car.Constants;
import eu.codetopic.anty.lejos.car.hardware.scan.SidesScanner;
import eu.codetopic.anty.lejos.utils.Utils;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;
import eu.codetopic.anty.lejos.utils.draw.drawer.GraphicsDrawer;
import lejos.robotics.TouchAdapter;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.Pose;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.robotics.objectdetection.TouchFeatureDetector;

import java.util.ArrayList;
import java.util.List;

import static eu.codetopic.anty.lejos.car.Constants.*;

@Deprecated
@SuppressWarnings("deprecation")
public final class Driver {

    private Driver() {
    }

    public static void drive(Canvas canvas, Hardware hardware) {
        Wheels wheels = hardware.getWheels();
        TouchAdapter touchAdapter = hardware.getTouch();
        TouchFeatureDetector touchDetector = hardware.getTouchDetector();

        double maxScanDistance = hardware.getScan().getSensorType().getDistanceMaxValue();

        Thread current = Thread.currentThread();
        FeatureListener listener = (feature, detector) -> current.interrupt();
        touchDetector.addListener(listener);

        GraphicsDrawer drawer = canvas.getGraphicsDrawer();
        PoseProvider poseProv = wheels.chassis.getPoseProvider();
        poseProv.setPose(new Pose(0f, 0f, 0f));
        Thread poseDrawer = new Thread(() -> {
            drawer.clear();
            while (!Thread.interrupted()) {
                Pose pose = poseProv.getPose();
                drawer.drawRect((int) (pose.getX() / 10f - 1f) + (drawer.getWidth() / 2),
                        (int) (pose.getY() / 10f - 1f) + (drawer.getHeight() / 2), 2, 2);
                canvas.apply();
                Thread.yield();
            }
        });
        poseDrawer.setDaemon(true);
        poseDrawer.setPriority(3);
        poseDrawer.start();

        do {
            Thread.yield();
            if (touchAdapter.isPressed()) {
                // TODO: 21.9.16 write blockade position to map
                wheels.startSync();
                wheels.left.setSpeed(500);
                wheels.right.setSpeed(500);
                wheels.backward();
                wheels.endSync();
                Utils.waitWhile(touchAdapter::isPressed);
                wheels.startSync();
                wheels.stop();
                wheels.left.rotate(-Constants.WHEELS_AROUND_ROTATION / 2, true);
                wheels.right.rotate(Constants.WHEELS_AROUND_ROTATION / 2, true);
                wheels.endSync();
                wheels.left.waitComplete();
                wheels.right.waitComplete();
                Thread.yield();
            }

            SidesScanner.start(hardware, new SidesScanner.ResultsCallback() {

                double leftDistance = -1d;
                double rightDistance = -1d;
                double centerDistance = -1d;

                @Override
                public void onCenterResult(List<Float> result) {
                    int center = 0;
                    for (Float distance : result) center += distance;
                    centerDistance = center / result.size();
                    updateDrive();
                }

                @Override
                public void onLeftResult(List<Float> result) {
                    int left = 0;
                    for (Float distance : result) left += distance;
                    leftDistance = left / result.size();
                    updateDrive();
                }

                @Override
                public void onRightResult(List<Float> result) {
                    int right = 0;
                    for (Float distance : result) right += distance;
                    rightDistance = right / result.size();
                    updateDrive();
                }

                private void updateDrive() {
                    if (leftDistance < 0 || rightDistance < 0 || centerDistance < 0) {
                        wheels.startSync();
                        wheels.stop();
                        wheels.endSync();
                        return;
                    }
                    Driver.updateDrive(wheels, maxScanDistance, leftDistance, rightDistance, centerDistance);
                }
            });
            wheels.stop();
        } while (touchAdapter.isPressed());// TODO: 21.9.16 better exit detection
        Utils.tryIt(() -> ((ArrayList<?>) touchDetector.getClass()
                .getDeclaredField("listeners").get(touchDetector)).remove(listener));
        Utils.stopThread(poseDrawer);
    }

    private static void updateDrive(Wheels wheels, double maxDistance, double leftDistance, double rightDistance, double centerDistance) {// TODO: 18.9.16 maybe improve
        double leftS, rightS;

        double speedMul = centerDistance * 3f - maxDistance;
        if (speedMul < maxDistance) {
            speedMul /= maxDistance;
            leftS = (rightDistance / leftDistance) * (speedMul * wheels.left.getMaxSpeed());
            rightS = (leftDistance / rightDistance) * (speedMul * wheels.right.getMaxSpeed());
        } else {
            leftS = (rightDistance / leftDistance) * wheels.left.getMaxSpeed();
            rightS = (leftDistance / rightDistance) * wheels.right.getMaxSpeed();
        }

        wheels.startSync();
        if (leftDistance > rightDistance) {
            wheels.left.setSpeed((float) leftS);
            wheels.right.setSpeed((float) rightS * 2f);
        } else {
            wheels.left.setSpeed((float) leftS * 2f);
            wheels.right.setSpeed((float) rightS);
        }
        wheels.forward();
        wheels.endSync();
    }
}
