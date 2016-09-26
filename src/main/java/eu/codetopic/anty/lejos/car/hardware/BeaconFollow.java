package eu.codetopic.anty.lejos.car.hardware;

import eu.codetopic.anty.lejos.car.hardware.scan.Scan;
import eu.codetopic.anty.lejos.car.hardware.scan.Scan.SeekResult;
import eu.codetopic.anty.lejos.utils.Range;
import eu.codetopic.anty.lejos.utils.ScannerMotor;
import eu.codetopic.anty.lejos.utils.ScannerMotor.Side;
import eu.codetopic.anty.lejos.utils.draw.canvas.Canvas;

@Deprecated
@SuppressWarnings("deprecation")
public final class BeaconFollow {

    private BeaconFollow() {
    }

    public static void follow(Canvas canvas/*TODO: use to draw beacon position*/, Hardware hardware) {
        Scan scan = hardware.getScan();
        Wheels wheels = hardware.getWheels();
        //TouchDetector touch = hardware.getTouch();// TODO: 21.9.16 add detection and mapping

        ScannerMotor scanMotor = scan.getScannerMotor();
        scanMotor.setSpeed(scanMotor.getMaxSpeed());
        scanMotor.rotateTo(Side.CENTER);
        Range allowedScanRange = new Range(Side.LEFT_90.getAngle(), Side.RIGHT_90.getAngle());

        while (!Thread.interrupted()) {
            SeekResult result = scan.fetchBeaconSeek();

            if (result == null || result.getDistance() == Float.POSITIVE_INFINITY) {
                wheels.startSync();
                wheels.stop();
                wheels.endSync();
                Thread.yield();
                continue;
            }

            //float targetAngle = result.getBeaconPosAngle() / 360f * Constants.SCAN_AROUND_ROTATION;
            /*if (!allowedScanRange.contains(scannerTacho + targetAngle)) {
                if (targetAngle > 0) {
                    targetAngle = (float) (allowedScanRange.getHigher() - scannerTacho);
                } else {
                    targetAngle = (float) (allowedScanRange.getLower() - scannerTacho);
                }
            }*/
            int scannerTacho = scanMotor.getModifiedTachoCount();
            float pos = result.getBeaconPosAngle();
            if (pos > 1f && allowedScanRange.getHigher() > scannerTacho) {
                scanMotor.forward();
            } else if (pos < -1f && allowedScanRange.getLower() < scannerTacho) {
                scanMotor.backward();
            } else {
                scanMotor.stop(true);
            }

            if (result.getDistance() < 15f) {
                wheels.startSync();
                wheels.stop();
                wheels.endSync();
                Thread.yield();
                continue;
            }

            pos += scannerTacho;
            if (Math.abs(pos) > allowedScanRange.getHigher()) pos = (float) allowedScanRange.getHigher();

            wheels.startSync();
            float slowerMul = (float) (Math.abs(allowedScanRange.getHigher() - pos) / allowedScanRange.getHigher());
            wheels.left.setSpeed((pos >= 0 ? wheels.left.getMaxSpeed() : slowerMul * wheels.left.getMaxSpeed()) / 5f);
            wheels.right.setSpeed((pos <= 0 ? wheels.right.getMaxSpeed() : slowerMul * wheels.right.getMaxSpeed()) / 5f);
            wheels.forward();
            wheels.endSync();

            Thread.yield();
        }

        wheels.startSync();
        wheels.stop();
        wheels.endSync();
        scanMotor.rotateTo(Side.CENTER);
    }
}
