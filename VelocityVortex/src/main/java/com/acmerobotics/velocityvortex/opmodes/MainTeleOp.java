package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
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

    private int sideModifier;

    private ElapsedTime flashTimer;
    private boolean shouldFlash, collectorReversed, launcherRunning;

    private boolean gamepad1HalfSpeed;

    private double launcherSpeed;

    private DataFile logFile;

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

        try {
            imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
            AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
            parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
            imu.initialize(parameters);

            drive = new EnhancedMecanumDrive(basicDrive, imu, properties);
//        drive.setInitialHeading(configuration.getLastHeading());
        } catch (Throwable t) {
            telemetry.addData("WARNING", "IMU did not initialize!");
            imu = null;
            drive = null;
        }


        DistanceSensor distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        wallFollower = new WallFollower(drive, distanceSensor, properties);
        wallFollower.setTargetDistance(BeaconFollower.BEACON_DISTANCE, BeaconFollower.BEACON_SPREAD);


        launcher = new FixedLauncher(hardwareMap);

        collector = new Collector(hardwareMap);
        beaconPusher = new BeaconPusher(hardwareMap, new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM));

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, Auto.BLUE_THRESHOLD, Auto.RED_THRESHOLD);

        stickyGamepad1 = new StickyGamepad(gamepad1);
        stickyGamepad2 = new StickyGamepad(gamepad2);

        flashTimer = new ElapsedTime();

        dim = hardwareMap.deviceInterfaceModule.get("dim");

        sideModifier = 1;

        state = State.DRIVER;

        logFile = new DataFile("TeleOp_" + System.currentTimeMillis() + ".csv");
        logFile.write("time,launcherSpeed");
    }

    @Override
    public void loop() {
        // update gamepads
        stickyGamepad1.update();
        stickyGamepad2.update();

        telemetry.addData("state", state);

        // abort
        if (gamepad1.a) {
            basicDrive.stop();
            state = State.DRIVER;
        }

        switch (state) {
            case DRIVER:
                //driver
                double x = -gamepad1.left_stick_x;
                double y = -gamepad1.left_stick_y;
                double omega = -gamepad1.right_stick_x;

                if (gamepad1HalfSpeed) {
                    x *= 0.6;
                    y *= 0.6;
                    omega *= 0.6;
                }

                if (y == 0) y = 0.4 * gamepad2.left_stick_y;
                if (omega == 0) omega = 0.5 * -gamepad2.right_stick_x;

                // check the dpad
                if (gamepad1.dpad_up) y = -1;
                else if (gamepad1.dpad_down) y = 1;
                if (gamepad1.dpad_right) x = 1;
                else if (gamepad1.dpad_left) x = -1;

                // apply quadratic (square) function
                double radius = Math.pow(Math.hypot(x, y), 2);
                double theta = Math.atan2(y, x);
                omega = Math.signum(omega) * Math.pow(omega, 2);

                telemetry.addData("radius", radius);
                telemetry.addData("theta", theta);
                telemetry.addData("omega", omega);

                basicDrive.setVelocity(new Vector2D(radius * Math.cos(theta), radius * Math.sin(theta)), omega);
                basicDrive.logPowers(telemetry);

                if (stickyGamepad1.y) {
                    gamepad1HalfSpeed = !gamepad1HalfSpeed;
                }

                if (gamepad1.left_bumper && gamepad1.right_bumper) {
                    if (drive != null) drive.resetHeading();
                } else {
                    //collector
                    if (gamepad1.right_trigger > 0.95) {
                        collector.reverse();
                        collectorReversed = true;
                    } else {
                        if (stickyGamepad1.right_bumper) {
                            if (collector.isRunning()) {
                                collector.stop();
                            } else {
                                collector.forward();
                            }
                        } else if (collectorReversed) {
                            collector.stop();
                            collectorReversed = false;
                        }
                    }

                    //beacons
                    if (drive != null) {
                        if (stickyGamepad1.x) {
                            sideModifier = 1;
                            state = State.BEACON_FORWARD;
                        } else if (stickyGamepad1.b) {
                            sideModifier = -1;
                            state = State.BEACON_FORWARD;
                        }
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
                        launcherRunning = false;
                    } else {
                        launcher.setPower(1);
                    }
                }
                launcher.update();

                launcherSpeed = launcher.getSpeed();
                if (launcherSpeed > 2.5 && launcher.isRunning()) {
                    launcherRunning = true;
                }

                logFile.write(System.currentTimeMillis() + "," + launcherSpeed);

                // launcher dim lights
                if (launcherRunning) {
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

                beaconPusher.setTargetPosition(wallFollower.getDistance() - BeaconFollower.PUSHER_DISTANCE);
                beaconPusher.update();

                if (colorAnalyzer.getBeaconColor() == ColorAnalyzer.BeaconColor.UNKNOWN) {
                    wallFollower.setForwardSpeed(sideModifier * BeaconFollower.BEACON_SEARCH_SPEED);
                    wallFollower.update();
                } else {
                    drive.stop();
                    state = State.BEACON_LATERAL;
                }

                break;

            case BEACON_LATERAL:
                beaconPusher.setTargetPosition(wallFollower.getDistance() - BeaconFollower.PUSHER_DISTANCE);
                beaconPusher.update();

                wallFollower.setForwardSpeed(0);
                if (wallFollower.update()) {
                    drive.stop();
                    state = State.BEACON_ALIGN;
                }

                break;

            case BEACON_ALIGN:
                beaconPusher.setTargetPosition(wallFollower.getDistance() - BeaconFollower.PUSHER_DISTANCE);
                beaconPusher.update();

                if (drive.getHeadingError() < EnhancedMecanumDrive.DEFAULT_TURN_ERROR) {
                    drive.stop();
                    state = State.BEACON_PUSH;
                }
                drive.update();

                break;

            case BEACON_PUSH:
                drive.stop();

                beaconPusher.push();

                state = State.DRIVER;

                break;

        }

        telemetry.addData("pusher", beaconPusher.getCurrentPosition());
        telemetry.addData("heading", drive.getHeading());
        telemetry.addData("color", colorAnalyzer.getBeaconColor());
        telemetry.addData("speed", launcherSpeed);
    }

    @Override
    public void stop() {
        logFile.close();
    }
}
