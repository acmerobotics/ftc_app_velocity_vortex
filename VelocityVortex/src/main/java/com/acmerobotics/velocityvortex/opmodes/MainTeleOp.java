package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="TeleOp")
public class MainTeleOp extends OpMode {

    private MecanumDrive drive;
    private EnhancedMecanumDrive enhancedMecanumDrive;
    private BeaconPusher beaconPusher;
    private boolean leftBumperDown, rightBumperDown;
    private BNO055IMU imu;

    @Override
    public void init() {
        drive = new MecanumDrive(hardwareMap);

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        enhancedMecanumDrive = new EnhancedMecanumDrive(drive, imu);

        beaconPusher = new BeaconPusher(hardwareMap);
    }

    @Override
    public void loop() {
        double x = -gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
        double omega = -gamepad1.right_stick_x;
        enhancedMecanumDrive.setVelocity(new Vector2D(x, y));
        enhancedMecanumDrive.update();
//        drive.setVelocity(new Vector2D(x, y), omega);

        if (gamepad1.left_bumper) {
            if (!leftBumperDown) {
                leftBumperDown = true;
                beaconPusher.leftToggle();
            }
        } else {
            leftBumperDown = false;
        }

        if (gamepad1.right_bumper) {
            if (!rightBumperDown) {
                rightBumperDown = true;
                beaconPusher.rightToggle();
            }
        } else {
            rightBumperDown = false;
        }

    }
}
