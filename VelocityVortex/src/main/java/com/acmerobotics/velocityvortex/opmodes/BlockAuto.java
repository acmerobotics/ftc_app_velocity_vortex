package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.FieldNavigator;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * @author Ryan Brott
 */

@Autonomous(name = "Block Auto", group="Autonomous")
public class BlockAuto extends Auto {

    private FixedLauncher launcher;

    private EnhancedMecanumDrive drive;
    private FieldNavigator nav;

    private BNO055IMU imu;

    private double halfWidth;

    @Override
    public void initOpMode() {
        halfWidth = properties.getRobotSize() / 2;

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);
        drive.setInitialHeading(180);
        nav = new FieldNavigator(drive, allianceColor);
        nav.setLocation(4 * TILE_SIZE + halfWidth, halfWidth);

        launcher = new FixedLauncher(hardwareMap);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndFire();

        moveToLineAndWait();
    }

    public void moveAndFire() {
        nav.moveTo(4 * TILE_SIZE + halfWidth, TILE_SIZE - halfWidth, 180, this);
        nav.moveTo(3.5 * TILE_SIZE, 1.5 * TILE_SIZE, 225, this);

        Auto.fireBalls(launcher, numBalls, this);
    }

    public void moveToLineAndWait() {
        nav.moveTo(2.5 * TILE_SIZE, 2.5 * TILE_SIZE, 180, this);

        while (opModeIsActive() && getRuntime() < 10) {
            idle();
        }
    }
}
