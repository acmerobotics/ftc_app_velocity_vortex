package com.acmerobotics.velocityvortex.opmodes;

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

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Autonomous(name="Wall Auto")
public class WallAuto extends LinearOpMode {

    public static final double TARGET_DISTANCE = 6.75;
    public static final double DISTANCE_SPREAD = .5;
    public static final double DISTANCE_SMOOTHER_EXP = 0.1;
    public static final double BASE_FORWARD_SPEED = 0.35;
    public static final double TILE_SIZE = 24;
    public static final long BEACON_PRESS_DELAY = 3000;//ms

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

    @Override
    public void runOpMode() throws InterruptedException {
        opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        properties = opModeConfiguration.getRobotType().getProperties();

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
        colorAnalyzer = new ColorAnalyzer(colorSensor);
        targetColor = allianceColor == OpModeConfiguration.AllianceColor.BLUE ?
                ColorAnalyzer.BeaconColor.BLUE : ColorAnalyzer.BeaconColor.RED;

        beaconPusher = new BeaconPusher(hardwareMap);
        beaconPusher.retract();

        dataFile = new DataFile("wall_auto_" + System.currentTimeMillis() + ".csv");
        dataFile.write("color, red, green, blue, alpha");

        waitForStart();

        basicDrive.move(-24, 0.6);

        launcher.fireBalls(opModeConfiguration.getNumberOfBalls());

        drive.turnSync(-110);

        basicDrive.move(42, 0.6);

        drive.setTargetHeading(180);
        drive.turnSync(0);

        double distanceError;
        do {
            distanceError = getDistance() - TARGET_DISTANCE;
            drive.setVelocity(new Vector2D(0.1 * distanceError, 0));
            idle();
        } while (Math.abs(distanceError) > DISTANCE_SPREAD);

        drive.stop();

        beaconsPressed = 0;
        followWall();

        dataFile.close();
    }

    public void followWall() {
        colorAnalyzer.read();
        try {Thread.sleep(2000); }catch (Exception e ) {}

        while (opModeIsActive()) {
            double distance = getDistance();

            double forwardSpeed = allianceModifier * BASE_FORWARD_SPEED;

            double distanceError = distance - TARGET_DISTANCE;

            if (Math.abs(distanceError) > DISTANCE_SPREAD) {
                if (distance > TARGET_DISTANCE + DISTANCE_SPREAD)
                    drive.setVelocity (new Vector2D(0.1*(-distanceError), forwardSpeed));
                else if (distance < TARGET_DISTANCE)
                    drive.setVelocity(new Vector2D(0.1*(-distanceError), forwardSpeed));
            } else {
                if(beaconsPressed < 2)
                    drive.setVelocity(new Vector2D(0, forwardSpeed));
                else
                    drive.stop();
            }

            ColorAnalyzer.BeaconColor color = colorAnalyzer.read();
            if (color == targetColor) {
                drive.stop();
                drive.turn(0);
                pushBeacon ();
                if (beaconsPressed < 2) basicDrive.move(allianceModifier * TILE_SIZE/2, .6);
                drive.stop();
            }

            drive.update();

            telemetry.addData("pidCoeff", drive.getController().toString());
            telemetry.addData("distance", distance);
            telemetry.addData("color", color.getName());
            telemetry.update();

            //dataFile.write(colorAnalyzer.toString());

            idle();
        }
    }

    private void pushBeacon () {
        long endTime = System.currentTimeMillis() + BEACON_PRESS_DELAY;
        beaconPusher.extend();
        try {Thread.sleep(BEACON_PRESS_DELAY); }catch (Exception e ) {}
        beaconPusher.retract();
        try {Thread.sleep(2000); }catch (Exception e ) {}
        beaconsPressed++;


    }

    private double getDistance () {
        double rawDistance = smoother.update(distanceSensor.getDistance(DistanceUnit.INCH));
        double headingError = Math.toRadians(drive.getHeadingError());
        return rawDistance * Math.cos(headingError) - sensorOffset * Math.sin(headingError);
    }
}
