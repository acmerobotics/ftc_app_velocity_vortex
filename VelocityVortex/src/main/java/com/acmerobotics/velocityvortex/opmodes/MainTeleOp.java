package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.CollectorHardware;
import com.acmerobotics.velocityvortex.mech.Launcher;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="TeleOp")
public class MainTeleOp extends OpMode {

    private double turningCoefficient = 10;
    private double elevationCefficient = 1;

    private double stickExponent = 1.8;

    private MecanumDrive drive;
    private BeaconPusher beaconPusher;
    private Launcher launcher;
    private CollectorHardware collector;
    private boolean leftTriggerDown, rightTriggerDown, rightBumperDown = false, leftBumperDown2 = false;
    private BNO055IMU imu;
    private OpModeConfiguration configuration;

    @Override
    public void init() {
        configuration = new OpModeConfiguration(hardwareMap.appContext);

        drive = new MecanumDrive(hardwareMap);

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        //enhancedMecanumDrive = new EnhancedMecanumDrive(drive, imu);

        beaconPusher = new BeaconPusher(hardwareMap, configuration.getRobotType().getProperties());

        launcher = new Launcher (hardwareMap);
        collector = new CollectorHardware(hardwareMap);
    }

    @Override
    public void loop() {
        //driver
        ////double x = Math.pow(-gamepad1.left_stick_x, stickExponent);
        //double y = Math.pow(-gamepad1.left_stick_y, stickExponent);
        double x = -gamepad1.left_stick_x;
        double y = -gamepad1.left_stick_y;
        //double omega = Math.pow(-gamepad1.right_stick_x, stickExponent);
        double omega = -gamepad1.right_stick_x;
        if (omega == 0) omega = -gamepad2.left_stick_x;
        /*enhancedMecanumDrive.setVelocity(new Vector2D(Range.clip(x, -1, 1), Range.clip(y, -1, 1)));
        enhancedMecanumDrive.setTargetHeading(enhancedMecanumDrive.getHeading() + omega * turningCoefficient);
        enhancedMecanumDrive.update();*/
        drive.setVelocity(new Vector2D(x, y), omega);

        if (gamepad1.left_trigger == 1) {
            if (!leftTriggerDown) {
                leftTriggerDown = true;
                beaconPusher.leftToggle();
            }
        } else if (gamepad1.left_trigger < .5){
            leftTriggerDown = false;
        }

        if (gamepad1.right_trigger == 1) {
            if (!rightTriggerDown) {
                rightTriggerDown = true;
                beaconPusher.rightToggle();
            }
        } else if (gamepad1.right_trigger < .5){
            rightTriggerDown = false;
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
                launcher.setVelocity (0);
            }
        } else leftBumperDown2 = false;
     }
}
