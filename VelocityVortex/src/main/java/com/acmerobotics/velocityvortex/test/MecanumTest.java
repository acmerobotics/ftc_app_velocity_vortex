package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
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
        if (Math.abs(x) < 0.05) {
            x = 0;
        }
        if (Math.abs(y) < 0.05) {
            y = 0;
        }
        if (Math.abs(omega) < 0.05) {
            omega = 0;
        }
        mecanumDrive.setVelocity(new Vector2D(-x, -y), -omega);
    }
}
