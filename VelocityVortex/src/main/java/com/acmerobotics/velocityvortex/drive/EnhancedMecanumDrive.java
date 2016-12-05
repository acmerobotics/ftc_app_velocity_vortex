package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

/**
 * Orientation-preserving drive interface based on an existing mecanum drive
 */
public class EnhancedMecanumDrive {

    public static final Vector2D INERT_VELOCITY = new Vector2D(0, 0);

    public static final PIDController.PIDCoefficients PID_COEFFICIENTS = new PIDController.PIDCoefficients(-0.05, 0, 0);

    public static final double MAX_TURN_SPEED = 1;
    public static final double DEFAULT_TURN_EPSILON = 2;

    private PIDController controller;
    private MecanumDrive drive;
    private BNO055IMU imu;
    private Vector2D velocity;
    private double targetHeading;

    public EnhancedMecanumDrive(MecanumDrive drive, BNO055IMU imu) {
        this.drive = drive;
        this.imu = imu;
        controller = new PIDController(PID_COEFFICIENTS);
        velocity = INERT_VELOCITY;
        resetHeading();
    }

    /**
     * Get the robot's heading.
     * @see BNO055IMU#getAngularOrientation()
     * @return the heading
     */
    public double getHeading() {
        return -imu.getAngularOrientation().firstAngle;
    }

    public double getTargetHeading() {
        return targetHeading;
    }

    public void moveForward(int ticks) {
        DcMotor[] motors = drive.getMotors();
        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motor.setTargetPosition(motor.getCurrentPosition() + ticks);
        }
        setVelocity(new Vector2D(0, 0.65));
        resetHeading();
        while (motors[0].isBusy()) {
            update();
            Thread.yield();
        }
        stop();
        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
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
        velocity = INERT_VELOCITY;
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
        double feedback;
        do {
            feedback = update();
            Thread.yield(); // equivalent to LinearOpMode#idle()
        } while (Math.abs(getHeadingError()) > epsilon || Math.abs(feedback) > 0.0025);
        stop();
    }

    /**
     * Turns the robot synchronously.
     * @see #turnSync(double, double)
     * @param turnAngle the turn angle
     */
    public void turnSync(double turnAngle) {
        turnSync(turnAngle, DEFAULT_TURN_EPSILON);
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
        this.targetHeading = targetHeading;
    }

    /**
     * Calculates the difference between the target heading and actual heading. The sign of the
     * orientation is positive for a clockwise correction and negative for a counter-clockwise
     * correction to maintain consistency with the BNO055 spec (see 3.6.2 in @see
     * <a href="https://cdn-shop.adafruit.com/datasheets/BST_BNO055_DS000_12.pdf">the spec</a>).
     * @return the heading error
     */
    public double getHeadingError() {
//        double error = Math.abs(targetHeading - getHeading()) % 360;
//        return (error > 180) ? (180 - error) : error;
        double error = targetHeading - getHeading();
        while (Math.abs(error) > 180) {
            error += -Math.signum(error) * 360;
        }
        return error;
    }

}
