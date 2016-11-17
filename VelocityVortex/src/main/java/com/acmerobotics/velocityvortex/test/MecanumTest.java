package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Mecanum Test")
public class MecanumTest extends OpMode {

    private MecanumDrive mecanumDrive;

    @Override
    public void init() {
        mecanumDrive = new MecanumDrive(hardwareMap);
    }

    @Override
    public void loop() {
        double x = gamepad1.left_stick_x;
        double y = gamepad1.left_stick_y;
        double omega = gamepad1.right_stick_x;
        if (Math.abs(x) < 0.1) {
            x = 0;
        }
        if (Math.abs(y) < 0.1) {
            y = 0;
        }
        if (Math.abs(omega) < 0.05) {
            omega = 0;
        }
        mecanumDrive.setVelocity(x, y, omega);
    }
}
