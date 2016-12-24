package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.i2c.SparkFunLineFollowingArray;
import com.acmerobotics.velocityvortex.opmodes.DeadReckoningAuto;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="Horizontal Line Follow Test")
public class HorizontalLineFollowTest extends LinearOpMode {

    private SparkFunLineFollowingArray lineSensor;
    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;

    @Override
    public void runOpMode() throws InterruptedException {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        lineSensor = new SparkFunLineFollowingArray(hardwareMap.i2cDeviceSynch.get("lineArray"));
        lineSensor.getParameters().invertBits = true;

        //drive = new EnhancedMecanumDrive(new MecanumDrive(hardwareMap, MecanumDrive.Configuration.createFixedRadius(4)), imu);

        waitForStart();

        DeadReckoningAuto.followLine(lineSensor, drive, this);
    }
}
