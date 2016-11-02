package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="Arcade Drive")
public class ArcadeDrive extends OpMode {

    private DcMotor left1, left2, right1, right2;

    @Override
    public void init() {
        left1 = hardwareMap.dcMotor.get("left1");
        left2 = hardwareMap.dcMotor.get("left2");
        right1 = hardwareMap.dcMotor.get("right1");
        right2 = hardwareMap.dcMotor.get("right2");
    }

    @Override
    public void loop() {
        double leftRight = -gamepad1.right_stick_x;
        double forwardBack = -gamepad1.left_stick_y;

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

        left1.setPower(leftSpeed);
        left2.setPower(leftSpeed);
        right1.setPower(-rightSpeed);
        right2.setPower(-rightSpeed);
    }
}
