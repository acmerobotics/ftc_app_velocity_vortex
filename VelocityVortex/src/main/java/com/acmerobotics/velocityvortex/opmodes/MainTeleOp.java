package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.BeaconFollower;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.drive.WallFollower;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.Collector;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.acmerobotics.velocityvortex.sensors.ThresholdColorAnalyzer;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name = "TeleOp")
public class MainTeleOp extends OpMode {

    public static final int FLASH_MS = 250;

    private MecanumDrive basicDrive;

    private FixedLauncher launcher;
    private Collector collector;
    private BeaconPusher beaconPusher;

    private StickyGamepad stickyGamepad1, stickyGamepad2;

    private OpModeConfiguration configuration;
    private RobotProperties properties;

    private State state;

    private AdafruitBNO055IMU imu;
    private EnhancedMecanumDrive drive;

    private WallFollower wallFollower;

    private ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;

    private DeviceInterfaceModule dim;

    private double sideModifier;

    private ElapsedTime flashTimer;
    private boolean shouldFlash;

    private enum State {
        DRIVER,
        BEACON_FORWARD,
        BEACON_LATERAL,
        BEACON_ALIGN,
        BEACON_PUSH
    }

    @Override
    public void init() {
        configuration = new OpModeConfiguration(hardwareMap.appContext);
        properties = configuration.getRobotType().getProperties();

        basicDrive = new MecanumDrive(hardwareMap, properties);

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);
//        drive.setInitialHeading(configuration.getLastHeading());

        DistanceSensor distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        wallFollower = new WallFollower(drive, distanceSensor, properties);
        wallFollower.setTargetDistance(BeaconFollower.BEACON_DISTANCE, BeaconFollower.BEACON_SPREAD);


        launcher = new FixedLauncher(hardwareMap);
        collector = new Collector(hardwareMap);
        beaconPusher = new BeaconPusher(hardwareMap, new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM));

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, 5, 5);

        stickyGamepad1 = new StickyGamepad(gamepad1);
        stickyGamepad2 = new StickyGamepad(gamepad2);

        flashTimer = new ElapsedTime();

        dim = hardwareMap.deviceInterfaceModule.get("dim");

        sideModifier = 1;

        state = State.DRIVER;

    }

    @Override
    public void loop() {
        // update gamepads
        stickyGamepad1.update();
        stickyGamepad2.update();

        telemetry.addData("state", state);

        // abort
        if (gamepad1.a) {
            drive.stop();
            state = State.DRIVER;
        }

        switch (state) {
            case DRIVER:
                //driver
                double x = -gamepad1.left_stick_x;
                double y = -gamepad1.left_stick_y;
                if (x == 0) x = -gamepad2.left_stick_x;

                double omega = -gamepad1.right_stick_x;
                if (omega == 0) omega = -gamepad2.left_stick_x;

                // check the dpad
                if (gamepad1.dpad_up) y = -1;
                else if (gamepad1.dpad_down) y = 1;
                if (gamepad1.dpad_right) x = 1;
                else if (gamepad1.dpad_left) x = -1;

                // apply quadratic (square) function
                double radius = Math.pow(Math.hypot(x, y), 2);
                double theta = Math.atan2(y, x);
                basicDrive.setVelocity(new Vector2D(radius * Math.cos(theta), radius * Math.sin(theta)), omega);

                if (gamepad1.left_bumper && gamepad1.right_bumper) {
                    drive.resetHeading();
                } else {
                    //collector
                    if (stickyGamepad1.right_bumper) {
                        if (collector.isRunning()) {
                            collector.stop();
                        } else {
                            collector.forward();
                        }
                    } else if (gamepad1.right_trigger > 0.95) {
                        collector.reverse();
                    }

                    //beacons
                    if (stickyGamepad1.x) {
                        sideModifier = 1;
                        state = State.BEACON_FORWARD;
                    } else if (stickyGamepad1.b) {
                        sideModifier = -1;
                        state = State.BEACON_FORWARD;
                    }

                    if (gamepad1.left_bumper) {
                        beaconPusher.extend();
                    } else {
                        beaconPusher.retract();
                    }
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

                // launcher dim lights
                if (launcher.isRunning() && !launcher.isBusy()) {
                    if (flashTimer.milliseconds() >= FLASH_MS) {
                        shouldFlash = !shouldFlash;
                        flashTimer.reset();
                    }
                } else {
                    shouldFlash = false;
                }

                dim.setLED(Auto.BLUE_LED_CHANNEL, shouldFlash);
                dim.setLED(Auto.RED_LED_CHANNEL, shouldFlash);

                break;

            case BEACON_FORWARD:
                // align to the nearest 90-degree orientation
                drive.setTargetHeading(Math.round(drive.getHeading() / 90.0) * 90);

                if (colorAnalyzer.getBeaconColor() == ColorAnalyzer.BeaconColor.UNKNOWN) {
                    wallFollower.setForwardSpeed(sideModifier * BeaconFollower.BEACON_SEARCH_SPEED);
                    wallFollower.update();
                } else {
                    drive.stop();
                    state = State.BEACON_LATERAL;
                }

                break;

            case BEACON_LATERAL:
                wallFollower.setForwardSpeed(0);
                if (wallFollower.update()) {
                    drive.stop();
                    state = State.BEACON_ALIGN;
                }

                break;

            case BEACON_ALIGN:
                if (drive.getHeadingError() < EnhancedMecanumDrive.DEFAULT_TURN_ERROR) {
                    drive.stop();
                    state = State.BEACON_PUSH;
                }
                drive.update();

                break;

            case BEACON_PUSH:
                beaconPusher.push();

                state = State.DRIVER;

                break;

        }

        // launcher status telemetry
        if (launcher.isRunning()) {
            if (launcher.isBusy()) {
                telemetry.addData("launcher", "busy");
            } else {
                telemetry.addData("launcher", "ready");
            }
        } else {
            telemetry.addData("launcher", "stopped");
        }

        telemetry.addData("pusher_position", beaconPusher.getCurrentPosition());
    }
}
