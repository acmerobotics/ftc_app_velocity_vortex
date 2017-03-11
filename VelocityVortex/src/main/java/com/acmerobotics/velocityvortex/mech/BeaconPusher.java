package com.acmerobotics.velocityvortex.mech;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.sensors.AverageDifferentiator;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan Brott
 */

public class BeaconPusher {

    public static final double MIN_POSITION = 0.25;
    public static final double MAX_POSITION = MIN_POSITION + 5;

    public static final double THRESHOLD = 0.001;
    public static final int INTERVAL = 100;
    public static final int TIMEOUT = 250;

    public static final DifferentialControlLoopCoefficients PID_COEFFICIENTS = new DifferentialControlLoopCoefficients(0.5, 0, 0);
    public static final int PUSH_MS = 300;

    private VoltageSensor voltageSensor;
    private DataFile logFile;
    private AverageDifferentiator speedMeasurer;
    private DcMotorSimple servo;
    private DistanceSensor sensor;
    private PIDController controller;
    private double targetPosition;

    public BeaconPusher(HardwareMap hardwareMap, DistanceSensor distanceSensor) {
        servo = hardwareMap.crservo.get("pusher");
//        servo.setDirection(DcMotorSimple.Direction.REVERSE);
        sensor = distanceSensor;

        voltageSensor = hardwareMap.voltageSensor.get("collector");

        speedMeasurer = new AverageDifferentiator(INTERVAL, TIMEOUT);
        if (sensor != null) {
            controller = new PIDController(PID_COEFFICIENTS);
        }
    }

    public BeaconPusher(HardwareMap hardwareMap) {
        this(hardwareMap, null);
    }

    public DcMotorSimple getServo() {
        return servo;
    }

    public PIDController getController() {
        return controller;
    }

    @Deprecated
    public boolean isSensorActive() {
        return sensor != null && getRawPosition() > 0;
    }

    public void setTargetPosition(double pos) {
        targetPosition = pos;
    }

    private double getRawPosition() {
        return sensor.getDistance(DistanceUnit.INCH);
    }

    public double getCurrentPosition() {
        return getRawPosition();
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

    public double getSpeed() {
        return speedMeasurer.getLastDerivative();
    }

    public void setLogFile(DataFile file) {
        if (file == null) {
            if (logFile != null) logFile.close();
            logFile = null;
        } else {
            logFile = file;
            logFile.write("diff: interval=" + speedMeasurer.getInterval() + "ms");
            logFile.write("time,pos,speed,voltage");
        }
    }

    public void extend() {
        setTargetPosition(MAX_POSITION);
    }

    public void retract() {
        setTargetPosition(MIN_POSITION);
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
            ElapsedTime timer = new ElapsedTime();
            while (opMode == null || opMode.opModeIsActive()) {
                update();
                if (getSpeed() < THRESHOLD) {
                    if (timer.milliseconds() > PUSH_MS) {
                        stop();
                        return;
                    }
                } else {
                    timer.reset();
                }
                Thread.yield();
            }
        } else {
            opMode.sleep(3000);
        }
    }

}
