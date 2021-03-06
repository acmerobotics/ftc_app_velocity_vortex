package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.configuration.WheelType;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * This class implements the basic functionality of an omnidirectional mecanum wheel drive system.
 */
public class MecanumDrive {

    public static final double RUN_TO_POSITION_MAX_SPEED = 0.65;
    public static final double RUN_WITH_ENCODER_MAX_SPEED = 0.85;

    public static final String[] MOTOR_ORDER = {
            "leftFront",
            "rightFront",
            "leftBack",
            "rightBack"
    };

    private DcMotor[] motors;
    private WheelType[] wheelTypes;
    private double smallestRPM;
    private Vector2d[] rollerDirs;
    private Vector2d[] rotDirs;
    private double[] offsets;
    private DcMotor.RunMode runMode;

    public MecanumDrive(HardwareMap map, RobotProperties properties) {
        wheelTypes = properties.getWheelTypes();

        smallestRPM = wheelTypes[0].getRPM();
        for (int i = 1; i < 4; i++) {
            double RPM = wheelTypes[i].getRPM();
            if (RPM < smallestRPM) {
                smallestRPM = RPM;
            }
        }

        motors = new DcMotor[4];
        for (int i = 0; i < motors.length; i++) {
            motors[i] = map.dcMotor.get(MOTOR_ORDER[i]);
            motors[i].setDirection(wheelTypes[i].getDirection());
        }

        setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        rollerDirs = new Vector2d[4];
        rollerDirs[0] = new Vector2d(-1, 1).normalize();
        rollerDirs[1] = new Vector2d(1, 1).normalize();
        rollerDirs[2] = rollerDirs[1];
        rollerDirs[3] = rollerDirs[0];

        rotDirs = new Vector2d[4];
        rotDirs[0] = new Vector2d(0, -1);
        rotDirs[1] = new Vector2d(0, 1);
        rotDirs[2] = rotDirs[0];
        rotDirs[3] = rotDirs[1];

        resetEncoders();
    }

    public DcMotor[] getMotors() {
        return motors;
    }

    public WheelType[] getWheelTypes() {
        return wheelTypes;
    }

    /**
     * Sets the velocity of the mecanum drive system.
     *
     * @param v translational velocity
     * @see #setVelocity(Vector2d, double)
     */
    public void setVelocity(Vector2d v) {
        setVelocity(v, 0);
    }

    /**
     * Sets the velocity of the mecanum drive system. This includes both the translational
     * component and the angular component. A positive speed indicates a clockwise rotation, and a
     * negative speed indicates a counter-clockwise rotation.
     *
     * @param v            translational velocity
     * @param angularSpeed angular speed
     */
    public void setVelocity(Vector2d v, double angularSpeed) {
        angularSpeed = Range.clip(angularSpeed, -1, 1);
        double speed;
        if (v.norm() > 1) {
            speed = 1;
        } else {
            speed = v.norm();
        }

        if (Math.abs(speed) > 1E-10) {
            v = v.copy().normalize();
        }

        for (int i = 0; i < 4; i++) {
            Vector2d angularVelocity = rotDirs[i].copy().multiply(angularSpeed);
            Vector2d transVelocity = v.copy().multiply(Math.min(1 - angularSpeed, speed));
            transVelocity.add(angularVelocity);
            double wheelSpeed = transVelocity.dot(rollerDirs[i]);
            double adjustedSpeed = getAdjustedSpeed(wheelSpeed, wheelTypes[i]);
            switch (runMode) {
                case RUN_USING_ENCODER:
                    adjustedSpeed *= RUN_WITH_ENCODER_MAX_SPEED;
                    break;
                case RUN_TO_POSITION:
                    adjustedSpeed *= RUN_TO_POSITION_MAX_SPEED;
                    break;
                default:
                    // do nothing
            }
            motors[i].setPower(adjustedSpeed);
        }

    }

    /**
     * Stop the motors.
     */
    public void stop() {
        for (DcMotor motor : motors) {
            motor.setPower(0);
        }
    }

    public void logPowers(Telemetry telemetry) {
        for (int i = 0; i < 4; i++) {
            telemetry.addData(MOTOR_ORDER[i], motors[i].getPower());
        }
    }

    /**
     * Reset the encoder positions. This will reset the value of {@link #getCurrentPositions()}.
     */
    public void resetEncoders() {
        offsets = getRawPositions();
    }

    /**
     * Get the adjusted positions for each motor
     *
     * @return an array of positions
     */
    public double[] getCurrentPositions() {
        double[] pos = getRawPositions();
        for (int i = 0; i < pos.length; i++) {
            pos[i] -= offsets[i];
        }
        return pos;
    }

    /**
     * Get the raw encoder positions for each motor
     *
     * @return an array of the positions
     */
    private double[] getRawPositions() {
        double[] raw = new double[motors.length];
        for (int i = 0; i < motors.length; i++) {
            raw[i] = wheelTypes[i].getDistance(motors[i].getCurrentPosition());
        }
        return raw;
    }

    public double getMeanPosition() {
        double[] pos = getRawPositions();
        double sum = 0;
        for (double val : pos) {
            sum += val;
        }
        return sum / pos.length;
    }

    public void move(double inches, double speed) {
        move(inches, speed, null);
    }

    /**
     * Move forward synchronously by a specific amount.
     *
     * @param inches the distance to travel
     * @param speed  the speed to travel at
     */
    public void move(double inches, double speed, LinearOpMode opMode) {
        setMode(DcMotor.RunMode.RUN_TO_POSITION);

        setRelativePosition(inches);

        setVelocity(new Vector2d(0, speed));

        boolean done = false;
        while (!done && (opMode == null || opMode.opModeIsActive())) {
            for (DcMotor motor : motors) {
                if (!motor.isBusy()) done = true;
            }
            Thread.yield();
        }

        stop();

        setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public double getAdjustedSpeed(double speed, WheelType wheelType) {
        return speed * (smallestRPM / wheelType.getRPM());
    }

    public DcMotor.RunMode getMode() {
        return runMode;
    }

    public void setMode(DcMotor.RunMode mode) {
        runMode = mode;
        for (int i = 0; i < motors.length; i++) {
            motors[i].setMode(mode);
        }
    }

    public void setRelativePosition(double inches) {
        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            int counts = wheelTypes[i].getCounts(inches);
            motor.setTargetPosition(motor.getCurrentPosition() + counts);
        }
    }

}