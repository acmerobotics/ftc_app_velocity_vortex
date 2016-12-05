package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name="Strafe Test")
public class StrafeTest extends OpMode {

    private MecanumDrive basicDrive;
    private EnhancedMecanumDrive drive;
    private AdafruitBNO055IMU imu;

    @Override
    public void init() {
        basicDrive = new MecanumDrive(hardwareMap);
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);
        drive = new EnhancedMecanumDrive(basicDrive, imu);
        drive.setVelocity(new Vector2D(1, 0));
    }

    @Override
    public void loop() {
        drive.update();
    }
}
