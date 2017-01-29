package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.BeaconRam;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.acmerobotics.velocityvortex.sensors.ThresholdColorAnalyzer;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan Brott
 */


@Autonomous(name="supermegabeconauto")
public class SuperBeaconsAuto extends Auto {

    public static final double TARGET_DISTANCE = 6.4;
    public static final double DISTANCE_SPREAD = 0.4;
    public static final double DISTANCE_SMOOTHER_EXP = 1;
    public static final double FORWARD_SPEED = 0.6;
    public static final double TILE_SIZE = 24;
    public static final double STRAFE_P = .15;
    public static final double ROOT2 = Math.sqrt(2);

    private FixedLauncher launcher;

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;

    private DistanceSensor distanceSensor;
    private ExponentialSmoother smoother;
    private double sensorOffset;

    private DataFile dataFile;
    private DataFile orientationFile;

    private ColorAnalyzer.BeaconColor targetColor;
    private ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;

    private BeaconPusher beaconPusher;
    private int beaconsPressed;
    private BeaconRam beaconRam;

    private ElapsedTime timer;

    private VoltageSensor voltageSensor;
    private double voltage;
    private double fireDistance;

    public void initOpMode () {
        targetColor = (allianceColor == OpModeConfiguration.AllianceColor.BLUE) ? ColorAnalyzer.BeaconColor.BLUE : ColorAnalyzer.BeaconColor.RED;

        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties.getTurnParameters());

        distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        smoother = new ExponentialSmoother(DISTANCE_SMOOTHER_EXP);
        sensorOffset = properties.getSonarSensorOffset();

        launcher = new FixedLauncher(hardwareMap);

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, 5, 5);

        beaconPusher = new BeaconPusher(hardwareMap);
        beaconRam = new BeaconRam(hardwareMap);

//        orientationFile = new DataFile("orientation.txt");
        dataFile = new DataFile("wall_auto4_" + System.currentTimeMillis() + ".csv");
        dataFile.write("loopTime, targetDistance, distance, targetHeading, heading, color, red, blue");

        voltageSensor = hardwareMap.voltageSensor.get("launcher");
        voltage = voltageSensor.getVoltage();
        double voltageError = Range.clip(voltage - 12, 0, 5);
        fireDistance = 24 - 2.6 * voltageError;
    }

    public void runOpMode () throws InterruptedException {
        super.runOpMode();

        moveAndShoot();
    }

    public void moveAndShoot() throws InterruptedException{
        basicDrive.move(-((Math.sqrt(2) * TILE_SIZE) + 3), .8, this);

        launcher.fireBalls(numBalls);

        ready();
        set ();
        go ();
    }

    public void ready () {
        basicDrive.move (-2 * TILE_SIZE * ROOT2 + 3, .8);
    }

    public void set () {
        drive.turnSync(-45 * allianceModifier);
        while (opModeIsActive() && getRuntime() < 10) {
            idle();
        }
        basicDrive.move (-TILE_SIZE * 2 + 2, .8);
    }

    public void go() throws InterruptedException{
        drive.turnSync (90 * allianceModifier);
        basicDrive.move(allianceModifier * -.5 * TILE_SIZE, .8);
        followWallAndPressBeacons();
    }

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

            dataFile.write(String.format("%f,%f,%f,%f,%f,%s,%d,%d", lastLoopTime, TARGET_DISTANCE, distance, drive.getTargetHeading(), drive.getHeading(), color, colorSensor.red(), colorSensor.blue()));
            if (color == targetColor) {
                drive.stop();

                moveToLateralPosition(TARGET_DISTANCE, DISTANCE_SPREAD, STRAFE_P);

                drive.turnSync(0, 3);

                beaconPusher.autoPush();

                return;

            } else {
                double forwardSpeed = allianceModifier * FORWARD_SPEED;
                double lateralSpeed = 0;
                if (Math.abs(distanceError) > DISTANCE_SPREAD) {
                    lateralSpeed = STRAFE_P * distanceError;
                }
                if (Math.abs(distanceError) > 2) {
                    forwardSpeed = 0;
                }
                drive.setVelocity(new Vector2D(lateralSpeed, forwardSpeed));
                drive.update();
            }

            idle();
        }
    }

    private void moveToLateralPosition(double target, double spread, double p) {
        double distance, distanceError;
        do {
            distance = getDistance();
            distanceError = target - distance;
            double lateralSpeed = p * distanceError;
            drive.setVelocity(new Vector2D(lateralSpeed, 0));
            drive.update();

            idle();
        } while(opModeIsActive() && Math.abs(distanceError) > spread);
    }

    private double getDistance() {
        double rawDistance = smoother.update(distanceSensor.getDistance(DistanceUnit.INCH));
        double headingError = Math.toRadians(drive.getHeadingError());
        return rawDistance * Math.cos(headingError) - sensorOffset * Math.sin(headingError);
    }

}
