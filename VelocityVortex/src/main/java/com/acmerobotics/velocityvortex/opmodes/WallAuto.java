package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

public class WallAuto extends LinearOpMode {

    public static final double DESIRED_DISTANCE = 10; // inches

    private MecanumDrive basicDrive;
    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;
    private DistanceSensor distanceSensor;

    @Override
    public void runOpMode() throws InterruptedException {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        basicDrive = new MecanumDrive(hardwareMap, MecanumDrive.Configuration.createFixedRadius(4));
        drive = new EnhancedMecanumDrive(basicDrive, imu);

        // create and initialize distance sensor

        waitForStart();

        while (opModeIsActive()) {

        }
    }
}
