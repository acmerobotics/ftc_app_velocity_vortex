package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Arcade Drive")
public class ArcadeDrive extends OpMode {

    private Drive drive;

    @Override
    public void init() {
        drive = new Drive(hardwareMap);
    }

    @Override
    public void loop() {
        drive.arcadeDrive(gamepad1);
    }
}
