package eu.codetopic.anty.lejos.car.hardware;

import eu.codetopic.anty.lejos.car.Constants;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.SteeringPilot;

import java.io.Closeable;
import java.io.IOException;

public class Wheels implements Closeable {

    public final EV3LargeRegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.B);
    public final EV3LargeRegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.C);
    public final Chassis chassis;
    public final MovePilot movePilot;

    public Wheels() {
        left.resetTachoCount();
        right.resetTachoCount();
        left.synchronizeWith(new EV3LargeRegulatedMotor[]{right});
        Wheel leftWheel = WheeledChassis.modelWheel(left, Constants.WHEELS_DIAMETER).offset(-Constants.WHEELS_OFFSET);// TODO: 21.9.16 test diameter and offset
        Wheel rightWheel = WheeledChassis.modelWheel(right, Constants.WHEELS_DIAMETER).offset(Constants.WHEELS_OFFSET);// TODO: 21.9.16 test diameter and offset
        chassis = new WheeledChassis(new Wheel[]{leftWheel, rightWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
        movePilot = new MovePilot(chassis);
        movePilot.setAngularAcceleration(20d);
        movePilot.setLinearAcceleration(20d);
    }

    public void forward() {
        left.forward();
        right.forward();
    }

    public void backward() {
        left.backward();
        right.backward();
    }

    public void stop() {
        left.stop(true);
        right.stop(true);
    }

    public void startSync() {
        left.startSynchronization();
    }

    public void endSync() {
        left.endSynchronization();
    }

    @Override
    public void close() throws IOException {
        stop();

        left.close();
        right.close();
    }
}
