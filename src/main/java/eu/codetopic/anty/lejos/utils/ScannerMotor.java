package eu.codetopic.anty.lejos.utils;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.port.TachoMotorPort;

public class ScannerMotor extends EV3MediumRegulatedMotor {

    private static int AROUND_ROTATION_ANGLE = -1;

    private RotationMode rotationMode = RotationMode.NORMAL;

    public ScannerMotor(TachoMotorPort port) {
        super(port);
        checkAroundRotationAngle();
    }

    public ScannerMotor(Port port) {
        super(port);
        checkAroundRotationAngle();
    }

    private static void checkAroundRotationAngle() {
        if (AROUND_ROTATION_ANGLE == -1) throw new IllegalStateException("Setup AROUND_ROTATION_ANGLE first");
    }

    public static void setAroundRotationAngle(int aroundRotationAngle) {
        AROUND_ROTATION_ANGLE = aroundRotationAngle;
    }

    public static int getAroundRotationAngle() {
        return AROUND_ROTATION_ANGLE;
    }

    public void invertRotationMode() {
        rotationMode = rotationMode.invert();
    }

    public void setRotationMode(RotationMode rotationMode) {
        this.rotationMode = rotationMode;
    }

    public RotationMode getRotationMode() {
        return rotationMode;
    }

    @Override
    public void rotateTo(int limitAngle) {
        limitAngle = rotationMode.modifyAngle(limitAngle);
        if (limitAngle != getTachoCount()) super.rotateTo(limitAngle);
    }

    @Override
    public void rotateTo(int limitAngle, boolean immediateReturn) {
        limitAngle = rotationMode.modifyAngle(limitAngle);
        if (limitAngle != getTachoCount()) super.rotateTo(limitAngle, immediateReturn);
    }

    public void rotateTo(Side side) {
        rotateTo(side.getAngle());
    }

    public void rotateTo(Side side, boolean immediateReturn) {
        rotateTo(side.getAngle(), immediateReturn);
    }

    public int getModifiedTachoCount() {
        return rotationMode.restoreAngle(getTachoCount());
    }

    public float getModifiedPosition() {
        return rotationMode.restoreAngle(getPosition());
    }

    public enum RotationMode {
        NORMAL, ROTATED;

        int getModification() {
            switch (this) {
                case ROTATED:
                    return AROUND_ROTATION_ANGLE;
                case NORMAL:
                default:
                    return 0;
            }
        }

        int modifyAngle(int angle) {
            return angle - getModification();
        }

        int restoreAngle(int angle) {
            return angle + getModification();
        }

        float restoreAngle(float angle) {
            return angle + getModification();
        }

        public RotationMode invert() {
            switch (this) {
                case ROTATED:
                    return NORMAL;
                case NORMAL:
                default:
                    return ROTATED;
            }
        }
    }

    public enum Side {
        CENTER, LEFT_80, RIGHT_80, LEFT_90, RIGHT_90, BACK;

        public int getAngle() {
            switch (this) {
                case BACK:
                case LEFT_80:
                case LEFT_90:
                    return -getAngleLength();
                case RIGHT_80:
                case RIGHT_90:
                    return getAngleLength();
                case CENTER:
                default:
                    return 0;
            }
        }

        public int getAngleLength() {
            switch (this) {
                case LEFT_80:
                case RIGHT_80:
                    return (int) (AROUND_ROTATION_ANGLE / 4.5f);
                case LEFT_90:
                case RIGHT_90:
                    return (int) (AROUND_ROTATION_ANGLE / 4f);
                case BACK:
                    return (int) (AROUND_ROTATION_ANGLE / 2f);
                case CENTER:
                default:
                    return 0;
            }
        }

        public Side invert() {
            switch (this) {
                case LEFT_80:
                    return RIGHT_80;
                case RIGHT_80:
                    return LEFT_80;
                case LEFT_90:
                    return RIGHT_90;
                case RIGHT_90:
                    return LEFT_90;
                case CENTER:
                    return BACK;
                case BACK:
                default:
                    return CENTER;
            }
        }
    }
}
