package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.DifferentialDrive;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Disabled
@TeleOp(name="Arcade Differential Drive")
public class ArcadeDifferentialTest extends OpMode {

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
