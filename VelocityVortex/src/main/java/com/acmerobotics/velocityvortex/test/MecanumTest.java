package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Disabled
@TeleOp(name = "Mecanum Test", group="Test")
public class MecanumTest extends OpMode {

    protected MecanumDrive mecanumDrive;

    private OpModeConfiguration configuration;
    private RobotProperties properties;

    @Override
    public void init() {
        configuration = new OpModeConfiguration(hardwareMap.appContext);
        properties = configuration.getRobotType().getProperties();

        mecanumDrive = new MecanumDrive(hardwareMap, properties);
    }

    @Override
    public void loop() {
        double x = 0, y = 0;
        if (gamepad1.dpad_down) y = -1;
        else if (gamepad1.dpad_up) y = 1;
        if (gamepad1.dpad_left) x = -1;
        else if (gamepad1.dpad_right) x = 1;
        double omega = -gamepad1.right_stick_x;
        telemetry.addData("omega", omega);
        mecanumDrive.setVelocity(new Vector2D(x, y), omega);
    }
}
