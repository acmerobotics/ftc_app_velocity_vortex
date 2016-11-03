package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

/**
 * This class controls the basic drive functionality of a four
 * wheel differential drive system.
 */
public class Drive {

    private double leftPower, rightPower;
    private DcMotor left1, left2, right1, right2;

    public Drive(HardwareMap hardwareMap) {
        left1 = hardwareMap.dcMotor.get("left1");
        left2 = hardwareMap.dcMotor.get("left2");
        right1 = hardwareMap.dcMotor.get("right1");
        right2 = hardwareMap.dcMotor.get("right2");
    }

    /**
     * Stop the motors
     */
    public void stopMotors() {
        leftPower = 0;
        rightPower = 0;
    }

    /**
     * Set the raw powers of the motors. Note that this method
     * doesn't actually write to the motor controllers ({@link #updateMotors()}
     * @param left left power
     * @param right right power
     */
    public void setMotorPowers(double left, double right) {
        this.leftPower = Range.clip(left, -1, 1);
        this.rightPower = Range.clip(right, -1, 1);
    }

    /**
     * Write the current motor powers to the controllers
     */
    public void updateMotors() {
        left1.setPower(leftPower);
        left2.setPower(leftPower);
        right1.setPower(-rightPower);
        right2.setPower(-rightPower);
    }

    /**
     * Sets the motor speeds according to the values of the gamepad
     * joystick. This method set the motor powers and updates them.
     * @param gamepad the gamepad
     */
    public void arcadeDrive(Gamepad gamepad) {
        double leftRight = -gamepad.right_stick_x;
        double forwardBack = -gamepad.left_stick_y;

        double rightSpeed = 0;
        double leftSpeed = 0;

        if (Math.abs(forwardBack) > 0.05) {
            rightSpeed = forwardBack;
            leftSpeed = forwardBack;
        }
        if (Math.abs(leftRight) > 0.05) {
            rightSpeed -= leftRight;
            leftSpeed += leftRight;
        }

        setMotorPowers(leftSpeed, rightSpeed);
        updateMotors();
    }
}
