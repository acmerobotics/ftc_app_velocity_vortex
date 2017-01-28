package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="Cap Ball Auto")
public class CapBallAuto extends Auto {

    public static final double TILE_SIZE = 24;

    @Override
    public void initOpMode() {

    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndShoot();

        pushAndPark();

    }

    public void moveAndShoot() {
        basicDrive.move(-((Math.sqrt(2) * TILE_SIZE) + 3), .5, this);

        launcher.fireBalls(numBalls);
    }

    public void pushAndPark() {
        basicDrive.move(Math.sqrt(2) * -TILE_SIZE, .5, this);
    }
}
