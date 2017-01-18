package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * @author Ryan Brott
 */

@TeleOp(name="IMU Test")
public class IMUTest extends OpMode {

    private BNO055IMU imu;

    @Override
    public void init() {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);
    }

    @Override
    public void loop() {
        Orientation o = imu.getAngularOrientation();
        telemetry.addData("XYZ", String.format("%5.2f,%5.2f,%5.2f", o.thirdAngle, o.secondAngle, -o.firstAngle));
    }
}
