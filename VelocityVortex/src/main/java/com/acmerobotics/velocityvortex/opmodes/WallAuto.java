package com.acmerobotics.velocityvortex.opmodes;

import android.annotation.SuppressLint;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.BeaconRam;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.RatioColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.acmerobotics.velocityvortex.sensors.ThresholdColorAnalyzer;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Date;

@Autonomous(name="Wall Auto")
public class WallAuto extends LinearOpMode {

    public static final double TARGET_DISTANCE = 6.4;
    public static final double DISTANCE_SPREAD = 0.4;
    public static final double DISTANCE_SMOOTHER_EXP = 1;
    public static final double FORWARD_SPEED = 0.25;
    public static final double TILE_SIZE = 24;
    public static final double STRAFE_P = .1;

    private OpModeConfiguration opModeConfiguration;
    private MecanumDrive basicDrive;
    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;
    private FixedLauncher launcher;
    private DistanceSensor distanceSensor;
    private ExponentialSmoother smoother;
    private DataFile dataFile;
    private RobotProperties properties;
    private double allianceModifier;
    private OpModeConfiguration.AllianceColor allianceColor;
    private RatioColorAnalyzer.BeaconColor targetColor;
    private ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;
    private BeaconPusher beaconPusher;
    private int beaconsPressed;
    private double sensorOffset;
    private ElapsedTime timer;
    private BeaconRam beaconRam;
    private VoltageSensor voltageSensor;
    private double voltage;
    private double fireDistance;

    @Override
    public void runOpMode() throws InterruptedException {
        opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        properties = opModeConfiguration.getRobotType().getProperties();

        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        allianceColor = opModeConfiguration.getAllianceColor();
        allianceModifier = allianceColor == OpModeConfiguration.AllianceColor.BLUE ? 1 : -1;
        targetColor = allianceColor == OpModeConfiguration.AllianceColor.BLUE ?
                RatioColorAnalyzer.BeaconColor.BLUE : RatioColorAnalyzer.BeaconColor.RED;

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        basicDrive = new MecanumDrive(hardwareMap, properties.getWheelRadius());
        drive = new EnhancedMecanumDrive(basicDrive, imu, properties.getTurnParameters());

        distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        smoother = new ExponentialSmoother(DISTANCE_SMOOTHER_EXP);

        sensorOffset = properties.getSonarSensorOffset();

        launcher = new FixedLauncher(hardwareMap);

//        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("color");
//        colorSensor = new TCS34725ColorSensor(i2cDevice, true);
//        colorSensor.setIntegrationTime(TCS34725ColorSensor.IntegrationTime.INTEGRATION_TIME_24MS);
//        colorSensor.setGain(TCS34725ColorSensor.Gain.GAIN_4X);

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

//        colorAnalyzer = new RatioColorAnalyzer(colorSensor, 2.25, 0.75);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, 15, 5);

        beaconPusher = new BeaconPusher(hardwareMap);
        beaconRam = new BeaconRam(hardwareMap);

        dataFile = new DataFile("wall_auto3_" + System.currentTimeMillis() + ".csv");
        dataFile.write("Wall Autonomous");
        dataFile.write(new Date().toString());
        dataFile.write("loopTime, targetDistance, distance, targetHeading, heading, color, ratio, red, blue");

        voltageSensor = hardwareMap.voltageSensor.get("launcher");
        voltage = voltageSensor.getVoltage();
        double voltageError = Range.clip(voltage - 12, 0, 5);
        fireDistance = 24 - 2.6 * voltageError;

        telemetry.addData("robot_type", opModeConfiguration.getRobotType());
        telemetry.addData("alliance_color", allianceColor);
        telemetry.addData("delay", opModeConfiguration.getDelay());
        telemetry.addData("num_balls", opModeConfiguration.getNumberOfBalls());
        telemetry.addData("fire_distance", fireDistance);
        telemetry.update();

        waitForStart();

        Thread.sleep(1000 * opModeConfiguration.getDelay());

        moveAndFire();

        followWallAndPressBeacons();

        dataFile.close();
    }

    public void moveAndFire() {

        basicDrive.move(-fireDistance, 1);

        launcher.fireBalls(opModeConfiguration.getNumberOfBalls());

        drive.turnSync(allianceModifier * -110);

        basicDrive.move(48, 1);

        if (allianceColor == OpModeConfiguration.AllianceColor.BLUE) {
            drive.setTargetHeading(180);
        } else {
            drive.setTargetHeading(0);
        }
        drive.turnSync(0);
    }

    @SuppressLint("DefaultLocale")
    public void followWallAndPressBeacons() throws InterruptedException {
        while (opModeIsActive()) {
            double lastLoopTime = timer.milliseconds();
            timer.reset();

            double distance = getDistance();
            double distanceError = TARGET_DISTANCE - distance;

            ColorAnalyzer.BeaconColor color = colorAnalyzer.getBeaconColor();

            telemetry.addData("pidCoeff", drive.getController().toString());
            telemetry.addData("distance", distance);
            telemetry.addData("distanceError", distanceError);
            telemetry.addData("color", color.toString());
            telemetry.update();

            dataFile.write(String.format("%f,%f,%f,%f,%f,%s,%f,%f", lastLoopTime, TARGET_DISTANCE, distance, drive.getTargetHeading(), drive.getHeading(), color, colorSensor.red(), colorSensor.blue()));
            if (color == targetColor) {
                drive.stop();

                basicDrive.move(-1.5 * allianceModifier, FORWARD_SPEED);

                while (opModeIsActive() && Math.abs(distanceError) > DISTANCE_SPREAD) {
                    distance = getDistance();
                    distanceError = TARGET_DISTANCE - distance;
                    double lateralSpeed = STRAFE_P * distanceError;
                    drive.setVelocity(new Vector2D(lateralSpeed, 0));
                    drive.update();

                    idle();
                }

                drive.turnSync(0);

                pushBeacon();
            } else {
                double forwardSpeed = allianceModifier * FORWARD_SPEED;
                double lateralSpeed = 0;
                if (Math.abs(distanceError) > DISTANCE_SPREAD) {
                    lateralSpeed = STRAFE_P * distanceError;
                }
                if (Math.abs(distanceError) > 4) {
                    forwardSpeed = 0;
                }
                drive.setVelocity(new Vector2D(lateralSpeed, forwardSpeed));
                drive.update();
            }

            idle();
        }
    }

    private double getDistance() {
        double rawDistance = smoother.update(distanceSensor.getDistance(DistanceUnit.INCH));
        double headingError = Math.toRadians(drive.getHeadingError());
        return rawDistance * Math.cos(headingError) - sensorOffset * Math.sin(headingError);
    }

    private void updateLateralSpeed(double distanceError, double forwardSpeed) {
        double lateralSpeed = 0;
        if (Math.abs(distanceError) > DISTANCE_SPREAD) {
            lateralSpeed = STRAFE_P * distanceError;
        }
        drive.setVelocity(new Vector2D(lateralSpeed, forwardSpeed));
        drive.update();
    }

    private void pushBeacon() {
        beaconPusher.autoPush();
        beaconsPressed++;

        if (beaconsPressed < 2) {
            basicDrive.move(3 * allianceModifier * TILE_SIZE / 2, 1);
        } else {
            drive.stop();
            return;
        }
    }
}
