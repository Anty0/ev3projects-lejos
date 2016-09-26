package eu.codetopic.anty.lejos.car.hardware;

import eu.codetopic.anty.lejos.car.hardware.scan.Scan;
import eu.codetopic.anty.lejos.utils.SimpleKeyListener;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.LED;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.internal.ev3.EV3LED;
import lejos.robotics.TouchAdapter;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.objectdetection.TouchFeatureDetector;

import java.io.Closeable;
import java.io.IOException;

public class Hardware implements Closeable {

    private final EV3LED led;
    private Scan scan;
    private Wheels wheels;
    private EV3TouchSensor touchSensor;
    private TouchAdapter touch;
    private TouchFeatureDetector touchDetector;

    public Hardware() {
        this((EV3LED) BrickFinder.getDefault().getLED());
    }

    public Hardware(EV3LED led) {
        this.led = led;
        Thread mainThread = Thread.currentThread();
        Button.ESCAPE.addKeyListener(new SimpleKeyListener() {
            @Override
            public void keyPressed(Key k) {
                mainThread.interrupt();
            }
        });
    }

    public void initialize() {
        scan = new Scan();
        wheels = new Wheels();
        touchSensor = new EV3TouchSensor(SensorPort.S1);
        touch = new TouchAdapter(touchSensor);
        touchDetector = new TouchFeatureDetector(touch, 0d, 3.5d);
    }

    public EV3LED getLed() {
        return led;
    }

    public Scan getScan() {
        return scan;
    }

    public Wheels getWheels() {
        return wheels;
    }

    public TouchAdapter getTouch() {
        return touch;
    }

    public TouchFeatureDetector getTouchDetector() {
        return touchDetector;
    }

    @Override
    public void close() throws IOException {
        touchSensor.close();
        wheels.close();
        scan.close();
    }
}
