package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Cap Ball Auto", group="Autonomous")
public class CapBallAuto extends Auto {

    private BNO055IMU imu;
    private EnhancedMecanumDrive drive;

    @Override
    public void initOpMode() {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndShoot();

        if (parkDest == OpModeConfiguration.ParkDest.CENTER) pushAndPark();

        drive.turnSync(45, this);
    }

    public void moveAndShoot() {
        drive.move(-((Math.sqrt(2) * TILE_SIZE) + 7), MOVEMENT_SPEED, this);

        Auto.fireBalls(launcher, numBalls, this);
    }

    public void pushAndPark() {
        drive.move(Math.sqrt(2) * -TILE_SIZE - 4, MOVEMENT_SPEED, this);
    }
}