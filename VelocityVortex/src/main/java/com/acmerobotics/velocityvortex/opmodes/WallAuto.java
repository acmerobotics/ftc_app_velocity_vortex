package com.acmerobotics.velocityvortex.opmodes;

import android.annotation.SuppressLint;

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

import static com.acmerobotics.velocityvortex.sensors.ColorAnalyzer.BeaconColor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import static com.acmerobotics.library.configuration.OpModeConfiguration.AllianceColor;

@Autonomous(name="Wall Auto")
public class WallAuto extends Auto {

    public static final double TARGET_DISTANCE = 6.4;
    public static final double DISTANCE_SPREAD = 0.3;
    public static final double DISTANCE_SMOOTHER_EXP = 1;
    public static final double FORWARD_SPEED = 0.6;
    public static final double TILE_SIZE = 24;
    public static final double STRAFE_P = .15;

    private FixedLauncher launcher;

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;

    private DistanceSensor distanceSensor;
    private ExponentialSmoother smoother;
    private double sensorOffset;

    private DataFile dataFile;

    private BeaconColor targetColor;
    private ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;

    private BeaconPusher beaconPusher;
    private int beaconsPressed;

    private ElapsedTime timer;

    private VoltageSensor voltageSensor;
    private double voltage;
    private double fireDistance;

    @Override
    public void initOpMode() {
        targetColor = (allianceColor == AllianceColor.BLUE) ? BeaconColor.BLUE : BeaconColor.RED;

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
        launcher.setTrim(-.05);

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, 5, 5);

        beaconPusher = new BeaconPusher(hardwareMap);

        dataFile = new DataFile("wall_auto4_" + System.currentTimeMillis() + ".csv");
        dataFile.write("loopTime, targetDistance, distance, targetHeading, heading, color, red, blue");

//        voltageSensor = hardwareMap.voltageSensor.get("launcher");
//        voltage = voltageSensor.getVoltage();
//        double voltageError = Range.clip(voltage - 12, 0, 5);
        fireDistance = 24;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndFire();

        followWallAndPressBeacons();

        switch (parkDest) {
            case NONE:
                break;
            case CENTER:
                pushBallAndCentralPark();
                break;
            case CORNER:
                cornerPark();
                break;
        }

        dataFile.close();
    }

    public void moveAndFire() {

        basicDrive.move(-fireDistance, 1, this);

        launcher.fireBalls(opModeConfiguration.getNumberOfBalls());

        drive.turnSync(allianceModifier * -110, this);

        basicDrive.move(50, 1, this);

        if (allianceColor == OpModeConfiguration.AllianceColor.BLUE) {
            drive.setTargetHeading(180);
        } else {
            drive.setTargetHeading(0);
        }

        drive.turnSync(0, this);
    }

    @SuppressLint("DefaultLocale")
    public void followWallAndPressBeacons() throws InterruptedException {
        while (opModeIsActive()) {
            double lastLoopTime = timer.milliseconds();
            timer.reset();

            double distance = getDistance();
            double distanceError = TARGET_DISTANCE - distance;

            BeaconColor color = colorAnalyzer.getBeaconColor();

            telemetry.addData("pidCoeff", drive.getController().toString());
            telemetry.addData("distance", distance);
            telemetry.addData("distanceError", distanceError);
            telemetry.addData("color", color.toString());
            telemetry.update();

            dataFile.write(String.format("%f,%f,%f,%f,%f,%s,%d,%d", lastLoopTime, TARGET_DISTANCE, distance, drive.getTargetHeading(), drive.getHeading(), color, colorSensor.red(), colorSensor.blue()));
            if (color == targetColor) {
                drive.stop();

                moveToLateralPosition(TARGET_DISTANCE, DISTANCE_SPREAD, STRAFE_P);

                drive.turnSync(0, 2, this);

                beaconPusher.autoPush();
                beaconsPressed++;

                if (beaconsPressed < 2) {
                    basicDrive.move(allianceModifier * TILE_SIZE, .8, this);
                } else {
                    drive.stop();
                    return;
                }
            } else {
                double forwardSpeed = allianceModifier * FORWARD_SPEED;
                double lateralSpeed = 0;
                if (Math.abs(distanceError) > DISTANCE_SPREAD) {
                    lateralSpeed = STRAFE_P * distanceError;
                }
                if (Math.abs(distanceError) > 1) {
                    forwardSpeed = 0;
                }
                drive.setVelocity(new Vector2D(lateralSpeed, forwardSpeed));
                drive.update();
            }

            idle();
        }
    }

    private void pushBallAndCentralPark() {
        moveToLateralPosition(20, 2 * DISTANCE_SPREAD, 2 * STRAFE_P);

        drive.turnSync(0, this);
        basicDrive.move(-2 * allianceModifier * TILE_SIZE, 1, this);
        drive.turnSync(90, this);
        basicDrive.move(-TILE_SIZE * 1.3, 1, this);
    }

    private void cornerPark() {
        moveToLateralPosition(15, 2 * DISTANCE_SPREAD, 2 * STRAFE_P);

        if (allianceColor == AllianceColor.RED) {
            drive.turnSync(180, this);
        }

        drive.setVelocity(new Vector2D(0, -1));

        // might be other angle
        while (opModeIsActive() && Math.abs(imu.getAngularOrientation().thirdAngle) < 7) {
            drive.update();
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

    private void updateLateralSpeed(double distanceError, double forwardSpeed) {
        double lateralSpeed = 0;
        if (Math.abs(distanceError) > DISTANCE_SPREAD) {
            lateralSpeed = STRAFE_P * distanceError;
        }
        drive.setVelocity(new Vector2D(lateralSpeed, forwardSpeed));
        drive.update();
    }
}
