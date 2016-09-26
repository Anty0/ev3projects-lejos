package eu.codetopic.anty.lejos.car.hardware.scan;

import eu.codetopic.anty.lejos.car.Constants;
import eu.codetopic.anty.lejos.car.hardware.Hardware;
import eu.codetopic.anty.lejos.utils.Range;
import eu.codetopic.anty.lejos.utils.ScannerMotor;
import eu.codetopic.anty.lejos.utils.ScannerMotor.Side;

import java.util.ArrayList;
import java.util.List;

import static eu.codetopic.anty.lejos.utils.ScannerMotor.Side.CENTER;
import static eu.codetopic.anty.lejos.utils.ScannerMotor.Side.LEFT_90;

@Deprecated
@SuppressWarnings("deprecation")
public class SidesScanner {

    private SidesScanner() {
    }

    public static void start(Hardware hardware, ResultsCallback resultsCallback) {
        double sideSize = (double) (Side.LEFT_80.getAngleLength() * 2) / 3d;
        double rightDivide = sideSize / 2d;
        double leftDivide = -rightDivide;

        Range centerRange = new Range(leftDivide, rightDivide);
        Range leftRange = new Range(leftDivide - sideSize, leftDivide);
        Range rightRange = new Range(rightDivide, rightDivide + sideSize);

        int angleWaitLen = Side.LEFT_90.getAngleLength() - Side.LEFT_80.getAngleLength();

        List<Float> results = new ArrayList<>();

        Scan scan = hardware.getScan();
        ScannerMotor motor = scan.getScannerMotor();

        motor.setSpeed(Constants.SCAN_SPEED_SIDES_FAST);
        Side lastSide = LEFT_90;
        motor.rotateTo(lastSide);
        while (!Thread.interrupted()) {
            motor.stop();

            int lastTarget = lastSide.getAngle();
            Range waitRange = new Range(lastTarget - angleWaitLen, lastTarget + angleWaitLen);
            lastSide = lastSide.invert();
            int newTarget = lastSide.getAngle();
            Range finishRange = new Range(newTarget - angleWaitLen, newTarget + angleWaitLen);
            motor.rotateTo(lastSide, true);

            int position = motor.getModifiedTachoCount();
            while (!Thread.currentThread().isInterrupted() && waitRange.contains(position) && motor.isMoving()) {
                Thread.yield();
                position = motor.getModifiedTachoCount();
            }

            while (!Thread.currentThread().isInterrupted() && !finishRange.contains(position) && motor.isMoving()) {
                position = motor.getModifiedTachoCount();

                if (centerRange.contains(position)) {
                    while (!Thread.currentThread().isInterrupted() && centerRange.contains(position) && motor.isMoving()) {
                        position = motor.getModifiedTachoCount();
                        results.add(scan.fetchDistance(true));
                    }
                    resultsCallback.onCenterResult(results);
                    results.clear();
                } else if (leftRange.contains(position)) {
                    while (!Thread.currentThread().isInterrupted() && leftRange.contains(position) && motor.isMoving()) {
                        position = motor.getModifiedTachoCount();
                        results.add(scan.fetchDistance(true));
                    }
                    resultsCallback.onLeftResult(results);
                    results.clear();
                } else if (rightRange.contains(position)) {
                    while (!Thread.currentThread().isInterrupted() && rightRange.contains(position) && motor.isMoving()) {
                        position = motor.getModifiedTachoCount();
                        results.add(scan.fetchDistance(true));
                    }
                    resultsCallback.onRightResult(results);
                    results.clear();
                }
            }
        }
        lastSide = CENTER;
        motor.rotateTo(lastSide);
    }

    public interface ResultsCallback {

        void onCenterResult(List<Float> result);

        void onLeftResult(List<Float> result);

        void onRightResult(List<Float> result);
    }
}
