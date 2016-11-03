package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class Drive {

    private double leftPower, rightPower;
    private DcMotor left1, left2, right1, right2;

    public Drive(HardwareMap hardwareMap) {
        left1 = hardwareMap.dcMotor.get("left1");
        left2 = hardwareMap.dcMotor.get("left2");
        right1 = hardwareMap.dcMotor.get("right1");
        right2 = hardwareMap.dcMotor.get("right2");
    }

    public void stopMotors() {
        leftPower = 0;
        rightPower = 0;
    }

    public void setMotorPowers(double left, double right) {
        this.leftPower = Range.clip(left, -1, 1);
        this.rightPower = Range.clip(right, -1, 1);
    }

    public void updateMotors() {
        left1.setPower(leftPower);
        left2.setPower(leftPower);
        right1.setPower(-rightPower);
        right2.setPower(-rightPower);
    }

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
