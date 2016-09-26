package eu.codetopic.anty.lejos.car;

public final class Constants {

    private Constants() {
    }

    public static final double DEG_TO_RAD_MUL = Math.PI / 180d;

    @Deprecated public static final int SCAN_SPEED_SIDES_FAST = /*1000*/1560;
    public static final int SCAN_SPEED_GRAPHICS_FAST = 150;
    public static final int SCAN_SPEED_GRAPHICS_SLOW = 15;
    public static final int SCAN_SPEED_FASTEST_ALLOWED = 360;
    public static final int SCAN_AROUND_ROTATION = /*360*/615;

    @Deprecated public static final int WHEELS_AROUND_ROTATION = 1350;

    public static final float MAX_VALUE_DISTANCE_EV3_IR = 50f;//1f;
    public static final float MAX_VALUE_DISTANCE_EV3_ULTRASONIC = 255f;
    public static final float MAX_VALUE_DISTANCE_NXT_ULTRASONIC = 255f;

    //public static final float STOP_DISTANCE = 0.25f;
    //public static final float SCAN_DISTANCE = 0.3f;
    //public static final float BACK_DISTANCE = 0.1f;

    public static final double WHEELS_DIAMETER = 4.3d;//1.85d
    public static final double WHEELS_OFFSET = 5.2d;//5.5d
}
