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
    private BNO055IMU imu;
    private EnhancedMecanumDrive enhancedMecanumDrive;

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

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        enhancedMecanumDrive = new EnhancedMecanumDrive(drive, imu, properties.getTurnParameters());

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

        drive.setVelocity(new Vector2D(x, y));

        //collector
        if (stickyGamepad1.right_bumper) {
            collector.toggle();
        }

        //launcher
        //trigger
        if (gamepad2.right_trigger > .95 ) {
            launcher.triggerUp();
        } else {
            launcher.triggerDown();
        }

        //wheels
        if (gamepad2.left_trigger > 0) {
            launcher.setPower(gamepad2.left_trigger);
        } else if (gamepad2.left_bumper) {
            launcher.setPower(0);
        }

     }
}
