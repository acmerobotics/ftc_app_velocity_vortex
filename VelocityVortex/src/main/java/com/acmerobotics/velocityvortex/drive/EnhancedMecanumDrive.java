package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.configuration.WheelType;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

/**
 * Orientation-preserving drive interface based on an existing mecanum drive
 */
public class EnhancedMecanumDrive {

    public static final double MAX_TURN_SPEED = 0.5;
    public static final double DEFAULT_TURN_ERROR = 1;

    private PIDController controller;
    private MecanumDrive drive;
    private BNO055IMU imu;
    private Vector2D velocity;
    private double targetHeading;
    private double initialHeading;

    public EnhancedMecanumDrive(MecanumDrive drive, BNO055IMU imu, RobotProperties properties) {
        this.drive = drive;
        this.imu = imu;
        controller = new PIDController(properties.getTurnParameters());
        velocity = new Vector2D(0, 0);
        resetHeading();
    }

    public void setInitialHeading(double heading) {
        initialHeading = heading;
    }

    /**
     * Get the robot's heading. This value is the size in degrees of the angle from the fixed axis
     * in a clockwise direction.
     *
     * @return the heading
     * @see BNO055IMU#getAngularOrientation()
     */
    public double getHeading() {
        return sanitizeHeading(getRawHeading() - initialHeading);
    }

    private double getRawHeading() {
        return -imu.getAngularOrientation().firstAngle;
    }

    public double getTargetHeading() {
        return targetHeading;
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
     *
     * @param velocity the translational velocity
     */
    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    /**
     * Updates the drive system using the latest PID controller feedback.
     *
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
     * the underlying motors, so please use {@link #update()} or {@link #turnSync(double, double, LinearOpMode)}.
     *
     * @param turnAngle the turn angle (right is positive, left is negative)
     */
    public void turn(double turnAngle) {
        setTargetHeading(targetHeading + turnAngle);
    }

    @Deprecated
    public void turnSync(double turnAngle, double error) {
        turnSync(turnAngle, error, null);
    }

    public void turnSync(double turnAngle, LinearOpMode opMode) {
        turnSync(turnAngle, DEFAULT_TURN_ERROR, opMode);
    }

    /**
     * Turns the robot synchronously.
     *
     * @param turnAngle the turn angle
     * @param error     satisfactory orientation error
     * @see #turn(double)
     */
    public void turnSync(double turnAngle, double error, LinearOpMode opMode) {
        turn(turnAngle);
        do {
            update();
            Thread.yield();
        }
        while (Math.abs(getHeadingError()) > error && (opMode == null || opMode.opModeIsActive()));
        stop();
    }

    /**
     * Turns the robot synchronously.
     *
     * @param turnAngle the turn angle
     * @see #turnSync(double, double, LinearOpMode)
     * @deprecated
     */
    @Deprecated
    public void turnSync(double turnAngle) {
        turnSync(turnAngle, DEFAULT_TURN_ERROR);
    }

    /**
     * Reset the target heading.
     */
    public void resetHeading() {
        setInitialHeading(getRawHeading());
    }

    /**
     * Set the target heading.
     *
     * @param targetHeading the target heading
     */
    public void setTargetHeading(double targetHeading) {
        this.targetHeading = sanitizeHeading(targetHeading);
    }

    /**
     * Calculates the difference between the target heading and actual heading. A positive error
     * represents a clockwise correction, and a negative error represents a counter-clockwise
     * correction.
     *
     * @return the heading error
     */
    public double getHeadingError() {
        double error = getHeading() - targetHeading;
        while (Math.abs(error) > 180) {
            error -= Math.signum(error) * 360;
        }
        return error;
    }

    private static double sanitizeHeading(double h) {
        double heading = h % 360;
        if (heading < 0) {
            heading += 360;
        }
        return heading;
    }

    public void move(double inches, double speed, LinearOpMode opMode) {
        DcMotor[] motors = drive.getMotors();

        drive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        drive.setRelativePosition(inches);

        setVelocity(new Vector2D(0, speed));

        boolean done = false;
        while (!done && (opMode == null || opMode.opModeIsActive())) {
            for (DcMotor motor : motors) {
                if (!motor.isBusy()) done = true;
            }
            update();
            Thread.yield();
        }

        stop();

        drive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

}
