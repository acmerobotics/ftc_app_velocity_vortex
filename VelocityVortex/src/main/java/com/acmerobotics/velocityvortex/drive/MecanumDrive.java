package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

/**
 * This class implements the basic functionality of an omnidirectional mecanum wheel drive system.
 */
public class MecanumDrive {

    private DcMotor[] motors;
    private Vector2D[] rollerDirs;
    private Vector2D[] wheelDirs;

    public MecanumDrive(HardwareMap map) {
        motors = new DcMotor[4];
        motors[0] = map.dcMotor.get("left1");
        motors[1] = map.dcMotor.get("right1");
        motors[2] = map.dcMotor.get("right2");
        motors[3] = map.dcMotor.get("left2");

        rollerDirs = new Vector2D[4];
        rollerDirs[0] = new Vector2D(-1, 1).normalize();
        rollerDirs[1] = new Vector2D(1, 1).normalize();
        rollerDirs[2] = rollerDirs[0];
        rollerDirs[3] = rollerDirs[1];

        wheelDirs = new Vector2D[4];
        wheelDirs[0] = new Vector2D(-1, 1).normalize();
        wheelDirs[1] = new Vector2D(1, 1).normalize();
        wheelDirs[2] = new Vector2D(1, -1).normalize();
        wheelDirs[3] = new Vector2D(-1, -1).normalize();
    }

    /**
     * Sets the angular velocity of the mecanum drive system. This includes both the translational
     * component and the angular component
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
        v.normalize();

        for (int i = 0; i < 4; i++) {
            Vector2D angularVelocity = wheelDirs[i].copy().multiply(angularSpeed);
            Vector2D transVelocity = v.copy().multiply(Math.min(1 - angularSpeed, speed));
            transVelocity.add(angularVelocity);
            motors[i].setPower(transVelocity.dot(rollerDirs[i]));
        }

    }

}
