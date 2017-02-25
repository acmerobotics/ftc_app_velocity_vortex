package com.acmerobotics.velocityvortex.mech;

import android.os.SystemClock;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.opmodes.Util;
import com.acmerobotics.velocityvortex.sensors.AverageDifferentiator;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImpl;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan Brott
 */

public class BeaconPusher {

    public static final double FULLY_RETRACTED = 0;
    public static final double FULLY_EXTENDED = 5;

    public static final DifferentialControlLoopCoefficients PID_COEFFICIENTS = new DifferentialControlLoopCoefficients(0.5, 0, 0);
    public static final int PUSH_MS = 400;

    private VoltageSensor voltageSensor;
    private DataFile logFile;
    private AverageDifferentiator speedMeasurer;
    private DcMotorSimple servo;
    private DistanceSensor sensor;
    private double initialPosition, targetPosition;
    private PIDController controller;

    public BeaconPusher(HardwareMap hardwareMap, DistanceSensor distanceSensor) {
        servo = hardwareMap.dcMotor.get("pusher");
        servo.setDirection(DcMotorSimple.Direction.REVERSE);
        sensor = distanceSensor;

        voltageSensor = hardwareMap.voltageSensor.get("collector");

        speedMeasurer = new AverageDifferentiator(500);
        if (sensor != null) {
            reset();
            controller = new PIDController(PID_COEFFICIENTS);
        }
    }

    public BeaconPusher(HardwareMap hardwareMap) {
        this(hardwareMap, null);
    }

    public PIDController getController() {
        return controller;
    }

    @Deprecated
    public boolean isSensorActive() {
        return sensor != null && getRawPosition() > 0;
    }

    public void reset() {
        initialPosition = getRawPosition();
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
        while ((opMode == null || opMode.opModeIsActive()) && Math.abs(getPositionError()) > error) {
            update();
            Thread.yield();
        }
        stop();
    }

    public void update() {
        if (isSensorActive()) {
            double error = getPositionError();
            double pos = getCurrentPosition();
            double speed = speedMeasurer.update(pos);
            servo.setPower(Range.clip(controller.update(error), -1, 1));

            if (logFile != null) {
                logFile.write(String.format("%d,%f,%f,%f", System.currentTimeMillis(), pos, speed, voltageSensor.getVoltage()));
            }
        }
    }

    public void setLogFile(DataFile file) {
        if (file == null) {
            if (logFile != null) logFile.close();
            logFile = null;
        } else {
            logFile = file;
            logFile.write("time,pos,speed,voltage");
        }
    }

    public void extend() {
        setTargetPosition(FULLY_EXTENDED);
    }

    public void retract() {
        setTargetPosition(FULLY_RETRACTED);
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
                if (Math.abs(pos - lastPos) < 0.1) {
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
            Util.sleep(3000);
        }
    }

}
