package eu.codetopic.anty.lejos.car.hardware.scan;

import eu.codetopic.anty.lejos.utils.ScannerMotor;
import eu.codetopic.anty.lejos.utils.ScannerMotor.RotationMode;
import lejos.hardware.Sound;
import lejos.hardware.device.DeviceIdentifier;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

import java.io.Closeable;
import java.io.IOException;

import static eu.codetopic.anty.lejos.car.Constants.*;

public class Scan implements Closeable {

    private final DistanceSensorType sensorType;
    private final BaseSensor scanSensor;
    private final SampleProvider distanceMode;
    private final SampleProvider beaconSeekMode;
    private final ScannerMotor scannerMotor;

    public Scan() {
        this(DistanceSensorType.detectType());
    }

    public Scan(DistanceSensorType sensorType) {
        this.sensorType = sensorType;
        switch (sensorType) {
            case EV3_IR:
                scanSensor = new EV3IRSensor(SensorPort.S4);
                distanceMode = ((EV3IRSensor) scanSensor).getDistanceMode();
                beaconSeekMode = ((EV3IRSensor) scanSensor).getSeekMode();
                break;
            case EV3_ULTRASONIC:
                scanSensor = new EV3UltrasonicSensor(SensorPort.S4);
                distanceMode = ((EV3UltrasonicSensor) scanSensor).getDistanceMode();
                beaconSeekMode = null;
                break;
            case NXT_ULTRASONIC:
                scanSensor = new NXTUltrasonicSensor(SensorPort.S4);
                distanceMode = ((NXTUltrasonicSensor) scanSensor).getDistanceMode();
                beaconSeekMode = null;
                break;
            case UNKNOWN:
            default:
                scanSensor = null;
                distanceMode = null;
                beaconSeekMode = null;
                Sound.buzz();
                break;
        }
        ScannerMotor.setAroundRotationAngle(SCAN_AROUND_ROTATION);
        scannerMotor = new ScannerMotor(MotorPort.A);
    }

    public DistanceSensorType getSensorType() {
        return sensorType;
    }

    public boolean hasDistanceMode() {
        return distanceMode != null;
    }

    public float fetchDistance(boolean cutToMaxValue) {
        float[] sample = new float[distanceMode.sampleSize()];
        distanceMode.fetchSample(sample, 0);
        sample[0] = sensorType.convertToCm(sample[0]);
        return cutToMaxValue ? (sample[0] > sensorType.getDistanceMaxValue()
                ? sensorType.getDistanceMaxValue() : sample[0]) : sample[0];
    }

    public boolean hasSeekMode() {
        return beaconSeekMode != null;
    }

    public SeekResult fetchBeaconSeek() {
        if (!hasSeekMode()) return null;
        float[] sample = new float[beaconSeekMode.sampleSize()];
        beaconSeekMode.fetchSample(sample, 0);

        int distanceIndex = 1;
        while (distanceIndex < sample.length && sample[distanceIndex] == Float.POSITIVE_INFINITY) {
            distanceIndex += 2;
        }

        return distanceIndex >= sample.length ? null : new SeekResult((distanceIndex - 1) / 2,
                sample[distanceIndex - 1], sample[distanceIndex]);
    }

    public ScannerMotor getScannerMotor() {
        return scannerMotor;
    }

    @Override
    public void close() throws IOException {
        scannerMotor.stop();
        scannerMotor.setRotationMode(RotationMode.NORMAL);
        scannerMotor.setSpeed((int) scannerMotor.getMaxSpeed());
        scannerMotor.rotateTo(0);

        scanSensor.close();
        scannerMotor.close();
    }

    public static final class SeekResult {

        private final int channel;
        private final float beaconPosAngle;
        private final float distance;

        private SeekResult(int channel, float beaconPosAngle, float distance) {
            this.channel = channel;
            this.beaconPosAngle = beaconPosAngle;
            this.distance = distance;
        }

        public int getChannel() {
            return channel;
        }

        public float getBeaconPosAngle() {
            return beaconPosAngle;
        }

        public float getDistance() {
            return distance;
        }

        @Override
        public String toString() {
            return "SeekResult{" +
                    "distance=" + distance +
                    ", beaconPosAngle=" + beaconPosAngle +
                    ", channel=" + channel +
                    '}';
        }
    }

    public enum DistanceSensorType {
        EV3_IR(MAX_VALUE_DISTANCE_EV3_IR, 1f), EV3_ULTRASONIC(MAX_VALUE_DISTANCE_EV3_ULTRASONIC, 100f),
        NXT_ULTRASONIC(MAX_VALUE_DISTANCE_NXT_ULTRASONIC, 100f), UNKNOWN(0f, 0f);

        private final float distanceMaxValue, convertMul;

        DistanceSensorType(float distanceMaxValue, float convertMul) {
            this.distanceMaxValue = distanceMaxValue;
            this.convertMul = convertMul;
        }

        public float getDistanceMaxValue() {
            return distanceMaxValue;
        }

        private float convertToCm(float distance) {
            return distance * convertMul;
        }

        public static DistanceSensorType detectType() {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(SensorPort.S4);
            String signature = deviceIdentifier.getDeviceSignature(false);
            deviceIdentifier.close();

            if (signature.contains("IR-PROX")) return EV3_IR;// TODO: 25.9.16 better signatures
            if (signature.contains("SONIC") && signature.contains("EV3")) return EV3_ULTRASONIC;
            if (signature.contains("SONIC") && signature.contains("NXT")) return NXT_ULTRASONIC;

            System.out.println("Can't detect signature: " + signature);
            return UNKNOWN;
        }
    }
}
