package com.acmerobotics.velocityvortex.opmodes;

import android.os.SystemClock;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.acmerobotics.velocityvortex.sensors.TCS34725ColorSensor;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Autonomous(name="Wall Auto")
public class WallAuto extends LinearOpMode {

    public static final double TARGET_DISTANCE = 6.5;
    public static final double DISTANCE_SPREAD = 0.5;
    public static final double DISTANCE_SMOOTHER_EXP = 1;
    public static final double BASE_FORWARD_SPEED = 0.25;
    public static final double TILE_SIZE = 24;

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
    private ColorAnalyzer.BeaconColor targetColor;
    private TCS34725ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;
    private BeaconPusher beaconPusher;
    private int beaconsPressed;
    private double sensorOffset;
    private ElapsedTime timer;

    @Override
    public void runOpMode() throws InterruptedException {
        opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        properties = opModeConfiguration.getRobotType().getProperties();

        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        allianceColor = opModeConfiguration.getAllianceColor();
        allianceModifier = allianceColor == OpModeConfiguration.AllianceColor.BLUE ? 1 : -1;

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

        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("color");
        colorSensor = new TCS34725ColorSensor(i2cDevice, true);
        colorSensor.setIntegrationTime(TCS34725ColorSensor.IntegrationTime.INTEGRATION_TIME_24MS);
        colorSensor.setGain(TCS34725ColorSensor.Gain.GAIN_4X);

        colorAnalyzer = new ColorAnalyzer(colorSensor);
        targetColor = allianceColor == OpModeConfiguration.AllianceColor.BLUE ?
                ColorAnalyzer.BeaconColor.BLUE : ColorAnalyzer.BeaconColor.RED;
        colorAnalyzer.read();

        beaconPusher = new BeaconPusher(hardwareMap);

        dataFile = new DataFile("wall_auto_" + System.currentTimeMillis() + ".csv");
        dataFile.write("loopTime, color, red, green, blue, alpha");

        waitForStart();

        moveAndShoot();

        followWallAndPressBeacons();

        dataFile.close();
    }

    public void moveAndShoot() {
        basicDrive.move(-24, 1);

        launcher.fireBalls(opModeConfiguration.getNumberOfBalls());

        drive.turnSync(allianceModifier * -110);

        basicDrive.move(45, 1);

        if (allianceColor == OpModeConfiguration.AllianceColor.BLUE) {
            drive.setTargetHeading(180);
        } else {
            drive.setTargetHeading(0);
        }
        drive.turnSync(0);
    }

    public void followWallAndPressBeacons() throws InterruptedException {
        beaconsPressed = 0;
        while (opModeIsActive()) {
            double lastLoopTime = timer.milliseconds();
            timer.reset();

            double distance = getDistance();
            double distanceError = TARGET_DISTANCE - distance;

            double forwardSpeed = allianceModifier * BASE_FORWARD_SPEED;
            double lateralSpeed = 0;
            if (Math.abs(distanceError) > DISTANCE_SPREAD) {
                lateralSpeed = 0.075 * distanceError;
            }
            if (Math.abs(distanceError) > 4) {
                forwardSpeed = 0;
            }
            drive.setVelocity(new Vector2D(lateralSpeed, forwardSpeed));
            drive.update();

            ColorAnalyzer.BeaconColor color = colorAnalyzer.read();
            if (color == targetColor) {
                drive.stop();
                drive.turn(0);

                beaconPusher.autoPush();
                beaconsPressed++;

                if (beaconsPressed < 2) {
                    basicDrive.move(3 * allianceModifier * TILE_SIZE / 2, 1);
                } else {
                    drive.stop();
                    return;
                }
            }

            telemetry.addData("pidCoeff", drive.getController().toString());
            telemetry.addData("distance", distance);
            telemetry.addData("distanceError", distanceError);
            telemetry.addData("color", color.getName());
            telemetry.update();

            dataFile.write(lastLoopTime + "," + colorAnalyzer.toString());

            idle();
        }
    }

    private double getDistance() {
        double rawDistance = smoother.update(distanceSensor.getDistance(DistanceUnit.INCH));
        double headingError = Math.toRadians(drive.getHeadingError());
        return rawDistance * Math.cos(headingError) - sensorOffset * Math.sin(headingError);
    }
}
