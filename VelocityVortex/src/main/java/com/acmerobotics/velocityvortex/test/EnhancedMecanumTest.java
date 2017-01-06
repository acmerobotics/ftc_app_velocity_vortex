package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Enhanced Mecanum Test")
public class EnhancedMecanumTest extends OpMode {

    private EnhancedMecanumDrive drive;

    private StickyGamepad stickyGamepad1;

    @Override
    public void init() {
        MecanumDrive basicDrive = new MecanumDrive(hardwareMap);
        BNO055IMU imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);
        drive = new EnhancedMecanumDrive(basicDrive, imu);
        stickyGamepad1 = new StickyGamepad(gamepad1);
    }

    @Override
    public void loop() {
        stickyGamepad1.update();

        if (stickyGamepad1.dpad_up) {
            EnhancedMecanumDrive.PID_COEFFICIENTS.p += 0.005;
        }
        if (stickyGamepad1.dpad_down) {
            EnhancedMecanumDrive.PID_COEFFICIENTS.p -= 0.005;
        }

        double x = -gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
//        double omega = -gamepad1.right_stick_x;
        drive.setVelocity(new Vector2D(x, y));
        telemetry.addData("target_heading", drive.getTargetHeading());
        telemetry.addData("error", drive.getHeadingError());
        telemetry.addData("update", drive.update());
        telemetry.addData("pid", EnhancedMecanumDrive.PID_COEFFICIENTS.toString());
    }
}
