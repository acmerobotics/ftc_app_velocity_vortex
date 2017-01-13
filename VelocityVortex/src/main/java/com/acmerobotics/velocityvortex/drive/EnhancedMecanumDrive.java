package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;
import com.qualcomm.robotcore.util.Range;

/**
 * Orientation-preserving drive interface based on an existing mecanum drive
 */
public class EnhancedMecanumDrive {

    public static final double MAX_TURN_SPEED = 1;
    public static final double MAX_TURN_ERROR = 1;

    private PIDController controller;
    private MecanumDrive drive;
    private BNO055IMU imu;
    private Vector2D velocity;
    private double targetHeading;

    public EnhancedMecanumDrive(MecanumDrive drive, BNO055IMU imu, DifferentialControlLoopCoefficients pid) {
        this.drive = drive;
        this.imu = imu;
        controller = new PIDController(pid);
        velocity = new Vector2D(0, 0);
        resetHeading();
    }

    /**
     * Get the robot's heading.
     * @see BNO055IMU#getAngularOrientation()
     * @return the heading
     */
    public double getHeading() {
        return imu.getAngularOrientation().firstAngle;
    }

    public double getTargetHeading() {
        return targetHeading;
    }

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
     * Set translational velocity of the base. This method does not actually update the underlying
     * motors; please use in tandem with {@link #update()}.
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
     * Turns the robot synchronously. The epsilon value specifies a satisfactory orientation error.
     * @see #turn(double)
     * @param turnAngle the turn angle
     * @param epsilon satisfactory orientation error
     */
    public void turnSync(double turnAngle, double epsilon) {
        turn(turnAngle);
        do {
            update();
            Thread.yield(); // equivalent to LinearOpMode#idle()
        } while (Math.abs(getHeadingError()) > epsilon);
        stop();
    }

    /**
     * Turns the robot synchronously.
     * @see #turnSync(double, double)
     * @param turnAngle the turn angle
     */
    public void turnSync(double turnAngle) {
        turnSync(turnAngle, MAX_TURN_ERROR);
    }

    /**
     * Reset the target heading.
     */
    public void resetHeading() {
        targetHeading = getHeading();
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
     * Calculates the difference between the target heading and actual heading. The sign of the
     * error is positive for a clockwise deviation and negative for a counter-clockwise deviation.
     * @return the heading error
     */
    public double getHeadingError() {
        double error = targetHeading - getHeading();
        while (Math.abs(error) > 180) {
            error += -Math.signum(error) * 360;
        }
        return error;
    }

}
