package com.acmerobotics.velocityvortex.test;

import android.graphics.Path;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * Created by ACME Robotics on 2/18/2017.
 */

@Autonomous(name="Encoder Drive Test")
public class EncoderDriveTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        OpModeConfiguration opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        RobotProperties properties = opModeConfiguration.getRobotType().getProperties();

        BNO055IMU imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        MecanumDrive basicDrive = new MecanumDrive(hardwareMap, properties);
        EnhancedMecanumDrive drive = new EnhancedMecanumDrive(basicDrive, imu, properties);

        waitForStart();

        drive.move(8 * 12, 1, this);
    }
}
