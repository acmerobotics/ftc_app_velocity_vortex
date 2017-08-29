package com.acmerobotics.library.drive;

import com.acmerobotics.library.pathing.RobotPath;
import com.acmerobotics.library.pathing.RobotPose;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

/**
 *
 * Created by kelly on 8/25/2017.
 */

public class PathDriveController {

    private final double kV_VELOCITY = .1;
    private final double kA_VELOCITY = .1;
    private final double kA_HEADING = .1;
    private final double kV_HEADING = .1;

    private Drive drive;
    private RobotPath path;
    private PIDController headingController, velocityController;
    private boolean started = false;
    private long startTime;
    private double prevOmega, prevVelocity;
    private long lastTime;


    public PathDriveController (Drive drive, RobotPath path) {
        this.drive = drive;
        this.path = path;
        setHeadingCoefs(new DifferentialControlLoopCoefficients(0.1, 0, 0));
        setVelocityCoefs(new DifferentialControlLoopCoefficients(0.1, 0, 0));
        prevOmega = 0;
        prevVelocity = 0;
    }

    /**
     * update the controller
     * @return false if the controller has finished executing the path
     */
    public boolean update() {
        if(path.length() < 1) return false;
        if (!started) {
            started = true;
            startTime = System.currentTimeMillis();
            lastTime = 0;
        }

        long time = System.currentTimeMillis() - startTime;
        long dt = time - lastTime;
        double currentVelocity = drive.getVelocity();
        double currentHeading = drive.getHeading();

        RobotPose pose = path.takeFirst();
        while (path.length() > 0 && pose.getTimestamp() < time) pose = path.takeFirst();
        RobotPose nextPose = path.getPose(0);

        double omega = nextPose.getAngularVelocity(pose);
        double vel = nextPose.getVelocity(pose);
        double dOmega = (omega - prevOmega) / dt * 1000;
        double accel = (vel - prevOmega) / dt * 1000;

        double velError = currentVelocity - vel;
        double headingError = currentHeading - pose.getHeading();

        double headingForward = omega * kV_HEADING + dOmega * kA_HEADING;
        double velocityForward = vel * kV_VELOCITY + accel * kA_VELOCITY;

        double headingCorrection = headingController.update(headingError);
        double velCorrection = velocityController.update(velError);

        drive.move(velocityForward + velCorrection, headingForward + headingCorrection);

        lastTime = time;
        prevOmega = omega;
        prevVelocity = vel;

        return true;
    }

    public void setHeadingCoefs (DifferentialControlLoopCoefficients coef) {
        headingController = new PIDController(coef);
    }

    public void setVelocityCoefs (DifferentialControlLoopCoefficients coef) {
        velocityController = new PIDController(coef);
    }
}
