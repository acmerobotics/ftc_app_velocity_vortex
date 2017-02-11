package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.configuration.WheelType;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

/**
 * This class implements the basic functionality of an omnidirectional mecanum wheel drive system.
 */
public class MecanumDrive {

    public static final double RUN_TO_POSITION_MAX_SPEED = 0.65;
    public static final double RUN_WITH_ENCODER_MAX_SPEED = 0.85;

    private DcMotor[] motors;
    private WheelType[] wheelTypes;
    private double smallestRPM;
    private Vector2D[] rollerDirs;
    private Vector2D[] rotDirs;
    private int[] offsets;

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
        motors[0] = map.dcMotor.get("leftFront");
        motors[0].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[1] = map.dcMotor.get("rightFront");
        motors[2] = map.dcMotor.get("rightBack");
//        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = map.dcMotor.get("leftBack");
        motors[3].setDirection(DcMotorSimple.Direction.REVERSE);

        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        rollerDirs = new Vector2D[4];
        rollerDirs[0] = new Vector2D(-1, 1).normalize();
        rollerDirs[1] = new Vector2D(1, 1).normalize();
        rollerDirs[2] = rollerDirs[0];
        rollerDirs[3] = rollerDirs[1];

        //double offX = 1;
        //double offY = 1;

        rotDirs = new Vector2D[4];
        rotDirs[0] = new Vector2D(0, -1);
        rotDirs[1] = new Vector2D(0, 1);
        rotDirs[2] = new Vector2D(0, 1);
        rotDirs[3] = new Vector2D(0, -1);

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
     * @see #setVelocity(Vector2D, double)
     */
    public void setVelocity(Vector2D v) {
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
    public void setVelocity(Vector2D v, double angularSpeed) {
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
            Vector2D angularVelocity = rotDirs[i].copy().multiply(angularSpeed);
            Vector2D transVelocity = v.copy().multiply(Math.min(1 - angularSpeed, speed));
            transVelocity.add(angularVelocity);
            double wheelSpeed = transVelocity.dot(rollerDirs[i]);
            motors[i].setPower(RUN_WITH_ENCODER_MAX_SPEED * getAdjustedSpeed(wheelSpeed, wheelTypes[i]));
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
    public int[] getCurrentPositions() {
        int[] pos = getRawPositions();
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
    public int[] getRawPositions() {
        int[] raw = new int[motors.length];
        for (int i = 0; i < motors.length; i++) {
            raw[i] = motors[i].getCurrentPosition();
        }
        return raw;
    }

    /**
     * Compute and return the mean encoder position.
     *
     * @return the mean encoder position
     */
    public int getMeanPosition() {
        int sum = 0;
        for (int pos : getCurrentPositions()) {
            sum += pos;
        }
        return sum / motors.length;
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
        DcMotor.RunMode[] prevModes = new DcMotor.RunMode[motors.length];

        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            prevModes[i] = motor.getMode();
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            int counts = wheelTypes[i].getCounts(inches);
            motor.setTargetPosition(motor.getCurrentPosition() + counts);
            motor.setPower(RUN_TO_POSITION_MAX_SPEED * getAdjustedSpeed(speed, wheelTypes[i]));
        }

        boolean done = false;
        while (!done && (opMode == null || opMode.opModeIsActive())) {
            for (DcMotor motor : motors) {
                if (!motor.isBusy()) done = true;
            }
            Thread.yield();
        }

        stop();

        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            motor.setMode(prevModes[i]);
        }
    }

    private double getAdjustedSpeed(double speed, WheelType wheelType) {
        return speed * (smallestRPM / wheelType.getRPM());
    }

}