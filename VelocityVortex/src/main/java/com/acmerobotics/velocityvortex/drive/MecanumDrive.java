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

    private DcMotor[] motors;
    private Vector2D[] rollerDirs;
    private Vector2D[] rotDirs;

    private int[] offsets;

    public MecanumDrive(HardwareMap map, double offX, double offY) {
        motors = new DcMotor[4];
        motors[0] = map.dcMotor.get("left1");
        motors[1] = map.dcMotor.get("right1");
        motors[1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[2] = map.dcMotor.get("right2");
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = map.dcMotor.get("left2");

        rollerDirs = new Vector2D[4];
        rollerDirs[0] = new Vector2D(1, 1).normalize();
        rollerDirs[1] = new Vector2D(-1, 1).normalize();
        rollerDirs[2] = rollerDirs[0];
        rollerDirs[3] = rollerDirs[1];

        rotDirs = new Vector2D[4];
        rotDirs[0] = new Vector2D(-offY, -offX).normalize();
        rotDirs[1] = new Vector2D(-offY, offX).normalize();
        rotDirs[2] = new Vector2D(offY, offX).normalize();
        rotDirs[3] = new Vector2D(offY, -offX).normalize();

        resetEncoders();
    }

    public MecanumDrive(HardwareMap map) {
        this(map, 1, 1);
    }

    /**
     * Sets the angular velocity of the mecanum drive system. This includes both the translational
     * component and the angular component.
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

        if (Math.abs(speed) > 0.0000001) {
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

    public void log(Telemetry telemetry) {
        for (int i = 0; i < motors.length; i++) {
            DcMotor motor = motors[i];
            telemetry.addData("motor" + i, motor.getPower());
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
     * Move the robot forward a specific number of encoder ticks
     * @param ticks number of encoder ticks
     */
    public void moveForward(int ticks) {
        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motor.setTargetPosition(motor.getCurrentPosition() + ticks);
            motor.setPower(0.65);
        }
        boolean busy = true;
        while (busy) {
            busy = true;
            for (DcMotor motor : motors) {
                busy = busy && motor.isBusy();
            }
            Thread.yield();
        }
        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motor.setPower(0);
        }
    }

}
