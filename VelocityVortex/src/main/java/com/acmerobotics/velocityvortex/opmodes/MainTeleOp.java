package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.Collector;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.mech.Launcher;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="TeleOp")
public class MainTeleOp extends OpMode {

    private MecanumDrive drive;

    private FixedLauncher launcher;
    private Collector collector;

    private StickyGamepad stickyGamepad1, stickyGamepad2;

    private OpModeConfiguration configuration;
    private RobotProperties properties;

    @Override
    public void init() {
        configuration = new OpModeConfiguration(hardwareMap.appContext);
        properties = configuration.getRobotType().getProperties();

        drive = new MecanumDrive(hardwareMap, properties.getWheelRadius());

        launcher = new FixedLauncher(hardwareMap);
        collector = new Collector(hardwareMap);

        stickyGamepad1 = new StickyGamepad(gamepad1);
        stickyGamepad2 = new StickyGamepad(gamepad2);
    }

    @Override
    public void loop() {
        // update gamepads
        stickyGamepad1.update();
        stickyGamepad2.update();

        //driver
        double x = -gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
        if (x == 0) x = -gamepad2.left_stick_x;

        double omega = -gamepad1.right_stick_x;
        if (omega == 0) omega = -gamepad2.left_stick_x;

        // apply quadratic (square) function
        double radius = Math.pow(Math.hypot(x, y), 2);
        double theta = Math.atan2(y, x);
        drive.setVelocity(new Vector2D(radius * Math.cos(theta), radius * Math.sin(theta)), omega);

        //collector
        if (stickyGamepad1.right_bumper) {
            collector.toggle();
        }

        //launcher
        //trigger
        if (gamepad2.right_bumper) {
            launcher.triggerUp();
        } else {
            launcher.triggerDown();
        }

        //wheels
        if (stickyGamepad2.left_bumper) {
            if (launcher.isRunning()) {
                launcher.setPower(0);
            } else {
                launcher.setPower(1, 1, 2000);
            }
        }
        launcher.update();

        telemetry.addData("leftPower", launcher.getLeftPower());
        telemetry.addData("rightPower", launcher.getRightPower());

     }
}
