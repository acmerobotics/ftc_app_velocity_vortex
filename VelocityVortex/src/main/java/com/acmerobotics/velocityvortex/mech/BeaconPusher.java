package com.acmerobotics.velocityvortex.mech;

import android.os.SystemClock;

import com.acmerobotics.velocityvortex.drive.PIDController;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImpl;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan Brott
 */

public class BeaconPusher {

    public static final DifferentialControlLoopCoefficients PID_COEFFICIENTS = new DifferentialControlLoopCoefficients(3, 0, 0);
    public static final int PUSH_MS = 250;

    private boolean sensorActive;
    private CRServo servo;
    private DistanceSensor sensor;
    private double initialPosition, targetPosition;
    private PIDController controller;

    public BeaconPusher(HardwareMap hardwareMap, DistanceSensor distanceSensor) {
        servo = hardwareMap.crservo.get("pusher");
        sensor = distanceSensor;
        if (sensor != null) {
            reset();
            controller = new PIDController(PID_COEFFICIENTS);
        }
    }

    public BeaconPusher(HardwareMap hardwareMap) {
        this(hardwareMap, null);
    }

    public boolean isSensorActive() {
        return sensorActive;
    }

    public void reset() {
        initialPosition = getRawPosition();
        sensorActive = initialPosition > 0;
    }

    public void setTargetPosition(double pos) {
        targetPosition = pos;
    }

    private double getRawPosition() {
        return sensor.getDistance(DistanceUnit.INCH);
    }

    public double getCurrentPosition() {
        return getRawPosition() - initialPosition;
    }

    private double getPositionError() {
        return targetPosition - getCurrentPosition();
    }

    public void moveToPosition(double pos, double error, LinearOpMode opMode) {
        setTargetPosition(pos);
        while ((opMode == null || opMode.opModeIsActive()) && Math.abs(getPositionError()) < error) {
            update();
            Thread.yield();
        }
        stop();
    }

    public void update() {
        if (isSensorActive()) {
            double error = getPositionError();
            servo.setPower(Range.clip(controller.update(error), -1, 1));
        }
    }

    public void extend() {
        servo.setPower(1);
    }

    public void retract() {
        servo.setPower(-1);
    }

    public void stop() {
        servo.setPower(0);
    }

    public void push() {
        push(null);
    }

    public void push(LinearOpMode opMode) {
        extend();

        if (isSensorActive()) {
            ElapsedTime time = new ElapsedTime();
            double lastPos = getCurrentPosition();
            while (opMode == null || opMode.opModeIsActive()) {
                double pos = getCurrentPosition();
                opMode.telemetry.addData("pos", pos);
                opMode.telemetry.addData("time", time.milliseconds());
                opMode.telemetry.update();
                if (Math.abs(pos - lastPos) < 0.1) { //0.025) {
                    if (time.milliseconds() >= PUSH_MS) {
                        break;
                    }
                } else {
                    lastPos = pos;
                    time.reset();
                }
                Thread.yield();
            }
        } else {
            SystemClock.sleep(3000);
        }
    }

}
