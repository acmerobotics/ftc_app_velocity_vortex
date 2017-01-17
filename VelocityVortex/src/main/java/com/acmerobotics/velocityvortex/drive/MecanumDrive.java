package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * This class implements the basic functionality of an omnidirectional mecanum wheel drive system.
 */
public class MecanumDrive {

    private static final int TICKS_PER_REV = 1670;

    private DcMotor[] motors;
    private Vector2D[] rollerDirs;
    private Vector2D[] rotDirs;
    private double wheelRadius;
    private int[] offsets;

    public MecanumDrive(HardwareMap map, double wheelRadius) {
        this.wheelRadius = wheelRadius;

        motors = new DcMotor[4];
        motors[0] = map.dcMotor.get("leftFront");
        motors[1] = map.dcMotor.get("rightFront");
        motors[1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[2] = map.dcMotor.get("rightBack");
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = map.dcMotor.get("leftBack");

        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        rollerDirs = new Vector2D[4];
        rollerDirs[0] = new Vector2D(1, 1).normalize();
        rollerDirs[1] = new Vector2D(-1, 1).normalize();
        rollerDirs[2] = rollerDirs[0];
        rollerDirs[3] = rollerDirs[1];

        double offX = 1;
        double offY = 1;

        rotDirs = new Vector2D[4];
        rotDirs[0] = new Vector2D(-offY, -offX).normalize();
        rotDirs[1] = new Vector2D(-offY, offX).normalize();
        rotDirs[2] = new Vector2D(offY, offX).normalize();
        rotDirs[3] = new Vector2D(offY, -offX).normalize();

        resetEncoders();
    }

    /**
     * Sets the velocity of the mecanum drive system.
     * @see #setVelocity(Vector2D, double)
     * @param v translational velocity
     */
    public void setVelocity(Vector2D v) {
        setVelocity(v, 0);
    }

    /**
     * Sets the velocity of the mecanum drive system. This includes both the translational
     * component and the angular component. A positive speed indicates a clockwise rotation, and a
     * negative speed indicates a counter-clockwise rotation.
     * @param v translational velocity
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
            motors[i].setPower(transVelocity.dot(rollerDirs[i]));
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

    public DcMotor[] getMotors() {
        return motors;
    }

    /**
     * Reset the encoder positions. This will reset the value of {@link #getPositions()}.
     */
    public void resetEncoders() {
        offsets = getRawPositions();
    }

    /**
     * Get the adjusted positions for each motor
     * @return an array of positions
     */
    public int[] getPositions() {
        int[] pos = getRawPositions();
        for (int i = 0; i < pos.length; i++) {
            pos[i] -= offsets[i];
        }
        return pos;
    }

    /**
     * Get the raw encoder positions for each motor
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
     * @return the mean encoder position
     */
    public int getMeanPosition() {
        int sum = 0;
        for (int pos : getPositions()) {
            sum += pos;
        }
        return sum / motors.length;
    }

    /**
     * Move forward synchronously by a specific amount.
     * @param inches the distance to travel
     * @param speed the speed to travel at
     */
    public void move(double inches, double speed) {
        DcMotor.RunMode[] prevModes = new DcMotor.RunMode[motors.length];
        double rev = inches / (2 * Math.PI * wheelRadius);
        int ticks = (int) Math.round(rev * TICKS_PER_REV);

        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            prevModes[i] = motor.getMode();
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motor.setTargetPosition(motor.getCurrentPosition() + ticks);
            motor.setPower(speed);
        }

        boolean done = false;
        while (!done) {
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

}