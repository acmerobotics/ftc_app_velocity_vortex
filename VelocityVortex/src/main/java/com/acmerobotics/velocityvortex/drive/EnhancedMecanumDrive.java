package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.hardware.adafruit.BNO055IMU;

/**
 * Orientation-preserving drive interface based on an existing mecanum drive
 */
public class EnhancedMecanumDrive {

    public static final Vector2D INERT = new Vector2D(0, 0);

    public static final PIDController.PIDCoefficients PID_COEFFICIENTS = new PIDController.PIDCoefficients(1, 0, 0);
    public static final double DEFAULT_TURN_EPSILON = 2.5;

    public enum AngleUnit {
        DEGREES(BNO055IMU.AngleUnit.DEGREES),
        RADIANS(BNO055IMU.AngleUnit.RADIANS);
        private BNO055IMU.AngleUnit unit;
        AngleUnit(BNO055IMU.AngleUnit angleUnit) {
            unit = angleUnit;
        }
        public double fromDegrees(double degrees) {
            return (unit == BNO055IMU.AngleUnit.DEGREES) ? degrees : Math.toRadians(degrees);
        }
        public double fromRadians(double radians) {
            return (unit == BNO055IMU.AngleUnit.RADIANS) ? radians : Math.toDegrees(radians);
        }
        public static AngleUnit from(BNO055IMU.AngleUnit unit) {
            for (AngleUnit angleUnit : AngleUnit.values()) {
                if (angleUnit.unit == unit) {
                    return angleUnit;
                }
            }
            return null;
        }
    }

    private PIDController controller;
    private MecanumDrive drive;
    private BNO055IMU imu;
    private Vector2D velocity;
    private AngleUnit angleUnit;
    private double targetHeading;

    public EnhancedMecanumDrive(MecanumDrive drive, BNO055IMU imu) {
        this.drive = drive;
        this.imu = imu;
        this.angleUnit = AngleUnit.from(imu.getParameters().angleUnit);
        controller = new PIDController(PID_COEFFICIENTS);
        velocity = INERT;
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
     * @return the orientation error
     */
    public double update() {
        double error = getHeadingError();
        double feedback = controller.update(error);
        drive.setVelocity(velocity, feedback);
        return error;
    }

    /**
     * Stop the motors.
     */
    public void stop() {
        velocity = INERT;
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
        double error;
        do {
            error = update();
            Thread.yield(); // equivalent to LinearOpMode#idle()
        } while (Math.abs(error) < epsilon);
    }

    /**
     * Turns the robot synchronously.
     * @see #turnSync(double, double)
     * @param turnAngle the turn angle
     */
    public void turnSync(double turnAngle) {
        turnSync(turnAngle, angleUnit.fromDegrees(DEFAULT_TURN_EPSILON));
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
    protected double getHeadingError() {
        double error = Math.abs(targetHeading - getHeading()) % angleUnit.fromDegrees(360);
        return (error > angleUnit.fromDegrees(180)) ? (angleUnit.fromDegrees(180) - error) : error;
    }

}
