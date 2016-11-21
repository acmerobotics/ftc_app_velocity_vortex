package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name="MecanumStrafeTest")
public class MecanumStrafeTest extends OpMode {

    private MecanumDrive basicDrive;
    private EnhancedMecanumDrive drive;
    private AdafruitBNO055IMU imu;

    @Override
    public void init() {
        basicDrive = new MecanumDrive(hardwareMap);
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        imu.initialize(new BNO055IMU.Parameters());
        drive = new EnhancedMecanumDrive(basicDrive, imu);
        drive.setVelocity(new Vector2D(1, 0));
    }

    @Override
    public void loop() {
        drive.update();
    }
}
