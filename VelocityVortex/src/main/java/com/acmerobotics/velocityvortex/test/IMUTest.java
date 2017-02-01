package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * @author Ryan Brott
 */

@TeleOp(name="IMU Test")
public class IMUTest extends OpMode {

    private static final String XYZ_FORMAT_STRING = "%5.2f,%5.2f,%5.2f";

    private DataFile file;
    private BNO055IMU imu;

    @Override
    public void init() {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        file = new DataFile("imu_test_" + System.currentTimeMillis() + ".csv");
        file.write("orient_x,orient_y,orient_z,acc_x,acc_y,acc_z,vel_x,vel_y,vel_z");
    }

    @Override
    public void loop() {
        Orientation o = imu.getAngularOrientation();
        Acceleration a = imu.getLinearAcceleration();
        AngularVelocity v = imu.getAngularVelocity();
        telemetry.addData("orientation", String.format(XYZ_FORMAT_STRING, o.thirdAngle, o.secondAngle, -o.firstAngle));
        telemetry.addData("linear_acceleration", String.format(XYZ_FORMAT_STRING, a.xAccel, a.yAccel, a.zAccel));
        telemetry.addData("angular_velocity", String.format(XYZ_FORMAT_STRING, v.xRotationRate, v.yRotationRate, v.zRotationRate));
        file.write(
                o.thirdAngle + "," + o.secondAngle + "," + (-o.firstAngle) + ","
                + a.xAccel + "," + a.yAccel + "," + a.zAccel + ","
                + v.xRotationRate + "," + v.yRotationRate + "," + v.zRotationRate
        );
    }

    @Override
    public void stop() {
        file.close();
    }
}
