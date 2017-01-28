package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;
import com.qualcomm.robotcore.util.Range;

/**
 * Orientation-preserving drive interface based on an existing mecanum drive
 */
public class EnhancedMecanumDrive {

    public static final double MAX_TURN_SPEED = 1;
    public static final double DEFAULT_TURN_ERROR = 1;

    private PIDController controller;
    private MecanumDrive drive;
    private BNO055IMU imu;
    private Vector2D velocity;
    private double targetHeading;
    private double initialHeading;

    public EnhancedMecanumDrive(MecanumDrive drive, BNO055IMU imu, DifferentialControlLoopCoefficients pid) {
        this.drive = drive;
        this.imu = imu;
        controller = new PIDController(pid);
        velocity = new Vector2D(0, 0);
        initialHeading = 0;
        resetHeading();
    }

    public void setInitialHeading (double heading) {
        initialHeading = heading;
    }

    /**
     * Get the robot's heading. This value is the size in degrees of the angle from the fixed axis
     * in a clockwise direction.
     * @see BNO055IMU#getAngularOrientation()
     * @return the heading
     */
    public double getHeading() {
        return -imu.getAngularOrientation().firstAngle - initialHeading;
    }

    public double getTargetHeading() {
        return targetHeading;
    }

    /**
     * Move the robot forward a specific distance. Please use the lower-level command
     * {@link MecanumDrive#move(double, double)} instead.
     * @deprecated
     * @param ticks encoder ticks to move forward
     */
    @Deprecated
    public void moveForward(int ticks) {
        resetHeading();
        drive.resetEncoders();
        int error = ticks;
        while (error > 0) {
            error = ticks - drive.getMeanPosition();
            setVelocity(new Vector2D(0, 0.0003 * error + 0.1));
            update();
            Thread.yield();
        }
        stop();
    }

    public PIDController getController() {
        return controller;
    }

    public MecanumDrive getDrive() {
        return drive;
    }

    /**
     * Set the translational velocity of the base. This method does not actually update the
     * underlying motors; please use in tandem with {@link #update()}.
     * @param velocity the translational velocity
     */
    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    /**
     * Updates the drive system using the latest PID controller feedback.
     * @return the angular velocity
     */
    public double update() {
        double error = getHeadingError();
        double feedback = controller.update(error);
        drive.setVelocity(velocity, Range.clip(feedback, -MAX_TURN_SPEED, MAX_TURN_SPEED));
        return feedback;
    }

    /**
     * Stop the motors.
     */
    public void stop() {
        velocity = new Vector2D(0, 0);
        drive.stop();
    }

    /**
     * Turns the robot. Like {@link #setVelocity(Vector2D)}}, this method does not actually update
     * the underlying motors, so please use {@link #update()} or {@link #turnSync(double, double)}.
     * @param turnAngle the turn angle (right is positive, left is negative)
     */
    public void turn(double turnAngle) {
        setTargetHeading(targetHeading + turnAngle);
    }

    /**
     * Turns the robot synchronously.
     * @see #turn(double)
     * @param turnAngle the turn angle
     * @param error satisfactory orientation error
     */
    public void turnSync(double turnAngle, double error) {
        turn(turnAngle);
        do {
            update();
            Thread.yield();
        } while (Math.abs(getHeadingError()) > error);
        stop();
    }

    /**
     * Turns the robot synchronously.
     * @see #turnSync(double, double)
     * @param turnAngle the turn angle
     */
    public void turnSync(double turnAngle) {
        turnSync(turnAngle, DEFAULT_TURN_ERROR);
    }

    /**
     * Reset the target heading.
     */
    public void resetHeading() {
        setTargetHeading(getHeading());
    }

    /**
     * Set the target heading.
     * @param targetHeading the target heading
     */
    public void setTargetHeading(double targetHeading) {
        this.targetHeading = targetHeading % 360;
        if (this.targetHeading < 0) {
            this.targetHeading += 360;
        }
    }

    /**
     * Calculates the difference between the target heading and actual heading. A positive error
     * represents a clockwise correction, and a negative error represents a counter-clockwise
     * correction.
     * @return the heading error
     */
    public double getHeadingError() {
        double error = getHeading() - targetHeading;
        while (Math.abs(error) > 180) {
            error -= Math.signum(error) * 360;
        }
        return error;
    }

}
