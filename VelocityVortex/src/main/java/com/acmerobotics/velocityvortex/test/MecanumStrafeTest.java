package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
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
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);
        drive = new EnhancedMecanumDrive(basicDrive, imu);
    }

    @Override
    public void loop() {
        drive.update();

        telemetry.addData("sum_error", drive.getController().getErrorSum());
        telemetry.addData("error", drive.getHeadingError());
        telemetry.addData("target", drive.getTargetHeading());
        telemetry.addData("heading", drive.getHeading());
    }
}
