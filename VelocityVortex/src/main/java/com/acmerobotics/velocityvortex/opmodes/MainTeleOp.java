package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconSwitch;
import com.acmerobotics.velocityvortex.mech.Collector;
import com.acmerobotics.velocityvortex.mech.Launcher;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="TeleOp")
public class MainTeleOp extends OpMode {

    private double turningCoefficient = 10;
    private double elevationCefficient = 1;

    private double stickExponent = 1.8;

    private MecanumDrive drive;
    private BeaconSwitch beaconPusher;
    private Launcher launcher;
    private Collector collector;
    private boolean leftTriggerDown, rightTriggerDown, rightBumperDown = false, leftBumperDown = false, leftBumperDown2 = false;
    private boolean upDown, rightDown, leftDown, downDown;
    private BNO055IMU imu;
    private OpModeConfiguration configuration;

    @Override
    public void init() {
        configuration = new OpModeConfiguration(hardwareMap.appContext);

        drive = new MecanumDrive(hardwareMap);

//        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
//        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
//        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
//        imu.initialize(params);



        launcher = new Launcher (hardwareMap);
        collector = new Collector(hardwareMap);
    }

    @Override
    public void loop() {
        //driver
        ////double x = Math.pow(-gamepad1.left_stick_x, stickExponent);
        //double y = Math.pow(-gamepad1.left_stick_y, stickExponent);
        double x = -gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
        if (x == 0) x = -gamepad2.left_stick_x;
        //double omega = Math.pow(-gamepad1.right_stick_x, stickExponent);
        double omega = -gamepad1.right_stick_x;
        if (omega == 0) omega = -gamepad2.left_stick_x;
        /*enhancedMecanumDrive.setVelocity(new Vector2D(Range.clip(x, -1, 1), Range.clip(y, -1, 1)));
        enhancedMecanumDrive.setTargetHeading(enhancedMecanumDrive.getHeading() + omega * turningCoefficient);
        enhancedMecanumDrive.update();*/
        drive.setVelocity(new Vector2D(x, y), omega);



        if (gamepad1.left_bumper) {
            if (!leftBumperDown) {
                leftBumperDown = true;
                beaconPusher.store();
            }
        } else {
            leftBumperDown = false;
        }

        if (gamepad1.right_bumper) {
            if (!rightBumperDown) {
                collector.toggle();
                rightBumperDown = true;
            }
        } else rightBumperDown = false;

        //launcher
        //trigger
        if (gamepad2.right_trigger > .95 ) {
            launcher.triggerUp();
        } else {
            launcher.triggerDown();
        }

        /*//gate
        if (gamepad2.right_bumper) {
            launcher.gateOpen();
        } else {
            launcher.gateClose();
        }*/

        //elevation
        launcher.setElevationVelocity(-gamepad2.right_stick_y);


        //wheels
        if (gamepad2.left_trigger > 0) {
            launcher.setVelocity(gamepad2.left_trigger);
        } else if (gamepad2.left_bumper) {
            if (!leftBumperDown2) {
                leftBumperDown2 = true;
                launcher.stop();
            }
        } else leftBumperDown2 = false;

        if (gamepad2.y) {
            if (!upDown) {
                launcher.maxVelocityUp();
                upDown = true;
            }
        } else upDown = false;

        if (gamepad2.a) {
            if (!downDown) {
                launcher.maxVelocityDown();
                downDown = true;
            }
        } else downDown = false;

        if (gamepad2.b) {
            if (!rightDown) {
                launcher.trimUp();
                rightDown = true;
            }
        } else rightDown = false;

        if (gamepad2.x) {
            if (!leftDown) {
                launcher.trimDown();
                leftDown = true;
            }
        } else leftDown = false;

        telemetry.addData ("maxVelocity", launcher.getMaxVelocity());
        telemetry.addData ("trim", launcher.getTrim());

     }
}
