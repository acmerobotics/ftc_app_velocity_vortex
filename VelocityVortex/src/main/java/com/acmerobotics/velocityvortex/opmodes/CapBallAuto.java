package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Cap Ball Auto")
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

        pushAndPark();

        drive.turnSync(45, this);
    }

    public void moveAndShoot() {
        basicDrive.move(-((Math.sqrt(2) * TILE_SIZE) + 3), MOVEMENT_SPEED, this);

        launcher.fireBalls(numBalls, this);
    }

    public void pushAndPark() {
        basicDrive.move(Math.sqrt(2) * -TILE_SIZE, MOVEMENT_SPEED, this);
    }
}
