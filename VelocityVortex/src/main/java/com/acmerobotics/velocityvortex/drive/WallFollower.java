package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan
 */

public class WallFollower {

    public static final double DISTANCE_SMOOTHER_EXP = 1;
    public static final double DEFAULT_STRAFE_P = 0.15;

    protected EnhancedMecanumDrive drive;
    private DistanceSensor sensor;
    private double sensorOffset;
    private ExponentialSmoother smoother;

    private double targetDistance, distanceSpread, forwardSpeed;

    public WallFollower(EnhancedMecanumDrive drive, DistanceSensor sensor, RobotProperties properties) {
        this.drive = drive;
        this.sensor = sensor;
        this.sensorOffset = properties.getDistanceSensorOffset();
        this.smoother = new ExponentialSmoother(DISTANCE_SMOOTHER_EXP);
    }

    public double getDistance() {
        double headingError = Math.toRadians(drive.getHeadingError());
        return sensor.getDistance(DistanceUnit.INCH) * Math.cos(headingError) - sensorOffset * Math.sin(headingError);
    }

    public double getSmoothedDistance() {
        return smoother.update(getDistance());
    }

    public void setTargetDistance(double distance, double spread) {
        targetDistance = distance;
        distanceSpread = spread;
    }

    public void setForwardSpeed(double forwardSpeed) {
        this.forwardSpeed = forwardSpeed;
    }

    public void moveToDistance(double distance, double spread, LinearOpMode opMode) {
        setTargetDistance(distance, spread);
        setForwardSpeed(0);
        while ((opMode == null || opMode.opModeIsActive()) && !update()) {
            Thread.yield();
        }
        drive.stop();
    }

    public boolean update() {
        double distanceError = targetDistance - getSmoothedDistance();
        if (Math.abs(distanceError) > distanceSpread) {
            double lateralSpeed = DEFAULT_STRAFE_P * distanceError;
            drive.setVelocity(new Vector2D(lateralSpeed, forwardSpeed));
            drive.update();
            return false;
        } else {
            drive.setVelocity(new Vector2D(0, forwardSpeed));
            drive.update();
            return true;
        }
    }

}