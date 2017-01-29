package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.Collector;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.acmerobotics.velocityvortex.sensors.ThresholdColorAnalyzer;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name="TeleOp")
public class MainTeleOp extends OpMode {

    private MecanumDrive basicDrive;

    private FixedLauncher launcher;
    private Collector collector;
    private BeaconPusher beaconPusher;

    private StickyGamepad stickyGamepad1, stickyGamepad2;

    private OpModeConfiguration configuration;
    private RobotProperties properties;

    private State state, previousState;

    private AdafruitBNO055IMU imu;
    private EnhancedMecanumDrive drive;
    private DistanceSensor distanceSensor;
    private ExponentialSmoother smoother;
    private double sensorOffset, distance, distanceError;

    private ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;

    private double sideModifier;

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

        basicDrive = new MecanumDrive(hardwareMap, properties.getWheelRadius());

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties.getTurnParameters());
        drive.setInitialHeading(configuration.getLastHeading());

        distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        smoother = new ExponentialSmoother(WallAuto.DISTANCE_SMOOTHER_EXP);
        sensorOffset = properties.getSonarSensorOffset();

        launcher = new FixedLauncher(hardwareMap);
        collector = new Collector(hardwareMap);
        beaconPusher = new BeaconPusher(hardwareMap);
        beaconPusher = new BeaconPusher(hardwareMap);

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, 5, 5);

        stickyGamepad1 = new StickyGamepad(gamepad1);
        stickyGamepad2 = new StickyGamepad(gamepad2);

        sideModifier = 1;

        state = State.DRIVER;
        previousState = state;

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

                telemetry.addData("leftPower", launcher.getLeftPower());
                telemetry.addData("rightPower", launcher.getRightPower());

                break;

            case BEACON_FORWARD:
                // align to the nearest 90-degree orientation
                drive.setTargetHeading(Math.round(drive.getHeading() / 90.0) * 90);

                if (colorAnalyzer.getBeaconColor() == ColorAnalyzer.BeaconColor.UNKNOWN) {
                    distance = getDistance();
                    distanceError = WallAuto.TARGET_DISTANCE - distance;
                    double forwardSpeed = sideModifier * WallAuto.FORWARD_SPEED;
                    double lateralSpeed = 0;
                    if (Math.abs(distanceError) > WallAuto.DISTANCE_SPREAD) {
                        lateralSpeed = WallAuto.STRAFE_P * distanceError;
                    }
                    if (Math.abs(distanceError) > 2) {
                        forwardSpeed = 0;
                    }
                    drive.setVelocity(new Vector2D(lateralSpeed, forwardSpeed));
                    drive.update();
                } else {
                    drive.stop();
                    state = State.BEACON_LATERAL;
                }

                break;

            case BEACON_LATERAL:
                distance = getDistance();
                distanceError = WallAuto.TARGET_DISTANCE - distance;

                if (Math.abs(distanceError) < WallAuto.DISTANCE_SPREAD) {
                    drive.stop();
                    state = State.BEACON_ALIGN;
                } else {
                    double lateralSpeed = WallAuto.STRAFE_P * distanceError;
                    drive.setVelocity(new Vector2D(lateralSpeed, 0));
                    drive.update();
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
                beaconPusher.autoPush();

                state = State.DRIVER;

                break;

        }
     }

    private double getDistance() {
        double rawDistance = smoother.update(distanceSensor.getDistance(DistanceUnit.INCH));
        double headingError = Math.toRadians(drive.getHeadingError());
        return rawDistance * Math.cos(headingError) - sensorOffset * Math.sin(headingError);
    }

}
