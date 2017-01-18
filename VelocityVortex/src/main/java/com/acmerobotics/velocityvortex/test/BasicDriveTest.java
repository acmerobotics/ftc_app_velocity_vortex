package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * @author Ryan Brott
 */

@Autonomous(name="Basic Drive Test")
public class BasicDriveTest extends LinearOpMode {

    MecanumDrive drive;

    @Override
    public void runOpMode() {
        OpModeConfiguration config = new OpModeConfiguration(hardwareMap.appContext);
        RobotProperties props = config.getRobotType().getProperties();

        drive = new MecanumDrive(hardwareMap, props.getWheelRadius());

        waitForStart();

        drive.move(4 * 12, 0.6);
    }
}
