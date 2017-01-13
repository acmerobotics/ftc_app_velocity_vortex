package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
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

    private OpModeConfiguration configuration;
    private RobotProperties properties;

    @Override
    public void init() {
        configuration = new OpModeConfiguration(hardwareMap.appContext);
        properties = configuration.getRobotType().getProperties();

        MecanumDrive basicDrive = new MecanumDrive(hardwareMap, properties.getWheelRadius());
        BNO055IMU imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);
        drive = new EnhancedMecanumDrive(basicDrive, imu, properties.getTurnParameters());
        stickyGamepad1 = new StickyGamepad(gamepad1);
    }

    @Override
    public void loop() {
        stickyGamepad1.update();

        if (stickyGamepad1.dpad_up) {
            drive.getController().getCoefficients().p += 0.001;
        }
        if (stickyGamepad1.dpad_down) {
            drive.getController().getCoefficients().p -= 0.001;
        }

        double x = -gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
//        double omega = -gamepad1.right_stick_x;
        drive.setVelocity(new Vector2D(x, y));
        telemetry.addData("target_heading", drive.getTargetHeading());
        telemetry.addData("error", drive.getHeadingError());
        telemetry.addData("update", drive.update());
        telemetry.addData("pid", drive.getController().toString());
    }
}
