package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * @author Ryan
 */

public interface HolonomicDrive {
    double getHeading();
    double getTargetHeading();
    void setTargetHeading(double targetHeading);
    void setVelocity(Vector2d velocity);
    void stop();
    void turn(double angle);
    void turnSync(double angle, LinearOpMode opMode);
    void move(double inches, double speed, LinearOpMode opMode);
}
