package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Arcade DifferentialDrive")
public class ArcadeDrive extends OpMode {

    private DifferentialDrive drive;

    @Override
    public void init() {
        drive = new DifferentialDrive(hardwareMap);
    }

    @Override
    public void loop() {
        drive.arcadeDrive(gamepad1);
    }
}
