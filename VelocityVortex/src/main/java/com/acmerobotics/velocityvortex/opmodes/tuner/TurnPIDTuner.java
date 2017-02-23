package com.acmerobotics.velocityvortex.opmodes.tuner;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.acmerobotics.velocityvortex.opmodes.tuner.Tuner;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Turn PID Tuner", group="PID Tuner")
public class TurnPIDTuner extends Tuner {

    private EnhancedMecanumDrive drive;

    private OpModeConfiguration configuration;
    private RobotProperties properties;

    @Override
    public void init() {
        super.init();

        configuration = new OpModeConfiguration(hardwareMap.appContext);
        properties = configuration.getRobotType().getProperties();

        MecanumDrive basicDrive = new MecanumDrive(hardwareMap, properties);

        BNO055IMU imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);

        stickyGamepad1 = new StickyGamepad(gamepad1);
    }

    @Override
    public void loop() {
        super.loop();

        double x = -gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
//        double omega = -gamepad1.right_stick_x;
        drive.setVelocity(new Vector2D(x, y));
        telemetry.addData("target_heading", drive.getTargetHeading());
        telemetry.addData("error", drive.getHeadingError());
        telemetry.addData("update", drive.update());
    }

    @Override
    protected PIDController getController() {
        return drive.getController();
    }
}
