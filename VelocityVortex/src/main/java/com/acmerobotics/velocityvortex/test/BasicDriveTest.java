package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
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
    public void runOpMode() throws InterruptedException {
        OpModeConfiguration config = new OpModeConfiguration(hardwareMap.appContext);
        RobotProperties props = config.getRobotType().getProperties();

        drive = new MecanumDrive(hardwareMap, props.getWheelRadius());BNO055IMU imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        EnhancedMecanumDrive enhancedDrive = new EnhancedMecanumDrive(drive, imu, props.getTurnParameters());

        waitForStart();

        drive.move(2 * 12, 0.6);

        Thread.sleep(2000);

        enhancedDrive.turnSync(90);
    }
}
