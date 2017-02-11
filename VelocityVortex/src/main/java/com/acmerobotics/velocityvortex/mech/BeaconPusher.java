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
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan Brott
 */

public class BeaconPusher {

    private boolean extended, sensorActive;
    private CRServo servo;
    private DistanceSensor sensor;
    private double initialPosition, targetPosition;
    private PIDController controller;

    public BeaconPusher(HardwareMap hardwareMap, DistanceSensor distanceSensor) {
        ServoController servoController = hardwareMap.servoController.get("servo");
        servo = new CRServoImpl(servoController, 2);
        servo.setPower(-1);
        extended = false;
        sensor = distanceSensor;
        if (sensor != null) {
            initialPosition = sensor.getDistance(DistanceUnit.INCH);
            sensorActive = initialPosition != 0;
            controller = new PIDController(new DifferentialControlLoopCoefficients(3, 0, 0));
        }
    }

    public BeaconPusher(HardwareMap hardwareMap) {
        this(hardwareMap, null);
    }

    public void setTargetPosition(double pos) {
        targetPosition = pos;
    }

    public double getCurrentPosition() {
        return sensor.getDistance(DistanceUnit.INCH) - initialPosition;
    }

    public void update() {
        if (sensorActive) {
            double error = targetPosition - getCurrentPosition();
            servo.setPower(Range.clip(controller.update(error), -1, 1));
        }
    }

    public void extend() {
        if (!extended) {
            servo.setPower(1);
            extended = true;
        }
    }

    public void retract() {
        if (extended) {
            servo.setPower(-1);
            extended = false;
        }
    }

    public void push() {
        push(null);
    }

    public void push(LinearOpMode opMode) {
        extend();
        if (sensorActive) {
            double lastPos = getCurrentPosition();
            int repeats = 0;
            while (opMode == null || opMode.opModeIsActive()) {
                double pos = getCurrentPosition();
                if (lastPos == pos) {
                    repeats++;
                } else {
                    repeats = 0;
                    lastPos = pos;
                }
                if (repeats == 100) {
                    break;
                }
            }
        } else {
            SystemClock.sleep(3000);
        }
        retract();
        SystemClock.sleep(800);
    }

    public boolean isExtended() {
        return extended;
    }

}
