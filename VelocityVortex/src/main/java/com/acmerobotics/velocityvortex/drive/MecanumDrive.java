package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

/**
 * This class implements the basic functionality of an omnidirectional mecanum wheel drive system.
 */
public class MecanumDrive {

    private DcMotor[] motors;
    private Vector2D[] rollerDirs;
    private Vector2D[] rotDirs;

    public MecanumDrive(HardwareMap map, double offX, double offY) {
        motors = new DcMotor[4];
        motors[0] = map.dcMotor.get("left1");
        motors[1] = map.dcMotor.get("right1");
        motors[1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[2] = map.dcMotor.get("right2");
        motors[2].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[3] = map.dcMotor.get("left2");

        for (DcMotor motor : motors) {
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

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
            v.normalize();
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

}
