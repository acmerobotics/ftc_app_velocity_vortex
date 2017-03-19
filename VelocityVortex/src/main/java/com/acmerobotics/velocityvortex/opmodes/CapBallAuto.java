package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.FieldNavigator;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Cap Ball Auto", group="Autonomous")
public class CapBallAuto extends Auto {

    private BNO055IMU imu;
    protected EnhancedMecanumDrive drive;

    protected FieldNavigator nav;

    protected double halfWidth;

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
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();
        
        moveAndFire();

        if (parkDest == OpModeConfiguration.ParkDest.CENTER) pushAndPark();
    }

    public void moveAndFire() {
        nav.moveTo(4 * TILE_SIZE + halfWidth, TILE_SIZE - halfWidth, this);
        nav.moveTo(3.5 * TILE_SIZE, 1.5 * TILE_SIZE, this);

        Auto.fireBalls(launcher, numBalls, this);
    }

    public void pushAndPark() {
        nav.moveTo(3 * TILE_SIZE, 2 * TILE_SIZE + 3.5, this);
    }
}
