package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.Vector;

import static com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive.sanitizeHeading;

/**
 * @author Ryan
 */

public class MockHolonomicDrive implements HolonomicDrive {

    private double targetHeading, heading;

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public double getTargetHeading() {
        return targetHeading;
    }

    @Override
    public void setTargetHeading(double targetHeading) {
        this.targetHeading = sanitizeHeading(targetHeading);
        this.heading = sanitizeHeading(targetHeading);
    }

    @Override
    public void setVelocity(Vector2d velocity) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void turn(double angle) {
        targetHeading = sanitizeHeading(targetHeading + angle);
        heading = sanitizeHeading(heading + angle);
        System.out.format("Turning to %f heading\n", heading);
    }

    @Override
    public void turnSync(double angle, LinearOpMode opMode) {
        turn(angle);
    }

    @Override
    public void move(double inches, double speed, LinearOpMode opMode) {
        stop();
        System.out.format("Moving %f inches with speed %f\n", inches, speed);
    }
}
