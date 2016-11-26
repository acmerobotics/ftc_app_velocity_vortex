package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="IMU Test")
public class IMUTest extends MecanumTest {

    private AdafruitBNO055IMU imu;
    private EnhancedMecanumDrive drive;

    @Override
    public void init() {
        super.init();

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        drive = new EnhancedMecanumDrive(mecanumDrive, imu);
        drive.turn(90);
    }

    @Override
    public void loop() {
        drive.update();

        telemetry.addData("error_sum", drive.getController().getErrorSum());
        telemetry.addData("error", drive.getHeadingError());
        telemetry.addData("target", drive.getTargetHeading());
        telemetry.addData("heading", drive.getHeading());
    }
}
