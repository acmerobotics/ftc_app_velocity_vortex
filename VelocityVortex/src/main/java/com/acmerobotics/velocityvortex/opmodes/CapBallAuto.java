package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="Cap Ball Auto")
public class CapBallAuto extends LinearOpMode {

    public static final double TILE_SIZE = 24;

    private OpModeConfiguration opModeConfiguration;
    private MecanumDrive basicDrive;
    private FixedLauncher launcher;
    private RobotProperties properties;

    @Override
    public void runOpMode() throws InterruptedException {
        opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        properties = opModeConfiguration.getRobotType().getProperties();

        basicDrive = new MecanumDrive(hardwareMap, properties.getWheelRadius());

        launcher = new FixedLauncher(hardwareMap);

        telemetry.addData("robot_type", opModeConfiguration.getRobotType());
        telemetry.addData("delay", opModeConfiguration.getDelay());
        telemetry.addData("num_balls", opModeConfiguration.getNumberOfBalls());
        telemetry.update();

        waitForStart();

        Thread.sleep(1000 * opModeConfiguration.getDelay());

        moveAndShoot();

        pushAndPark();

    }

    public void moveAndShoot() {

        basicDrive.move (-((Math.sqrt(2) * TILE_SIZE) + 3), .5);

        launcher.fireBalls(opModeConfiguration.getNumberOfBalls());


    }

    public void pushAndPark () {

        basicDrive.move (Math.sqrt(2) * -TILE_SIZE, .5);

    }
}
