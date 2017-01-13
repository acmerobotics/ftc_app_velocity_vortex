package com.acmerobotics.velocityvortex.opmodes;

import android.provider.ContactsContract;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name="Wall Auto")
public class WallAuto extends LinearOpMode {

    public static final double[] TARGET_DISTANCE_RANGE = { 7, 10 };
    public static final double MAX_ACCEPTABLE_HEADING_ERROR = Math.toRadians(5);
    public static final double DISTANCE_SMOOTHER_EXP = 0.05;
    public static final double BASE_FORWARD_SPEED = 0.05;
    public static final double SENSOR_OFFSET = 6.5;

    private OpModeConfiguration opModeConfiguration;
    private MecanumDrive basicDrive;
    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;
    private DistanceSensor distanceSensor;
    private ExponentialSmoother smoother;
    private DataFile dataFile;
    private RobotProperties properties;

    @Override
    public void runOpMode() throws InterruptedException {
        opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        properties = opModeConfiguration.getRobotType().getProperties();

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        basicDrive = new MecanumDrive(hardwareMap, properties.getWheelRadius());
        drive = new EnhancedMecanumDrive(basicDrive, imu, properties.getTurnParameters());

        distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        smoother = new ExponentialSmoother(DISTANCE_SMOOTHER_EXP);

        dataFile = new DataFile("wall_auto_" + System.currentTimeMillis() + ".csv");
        dataFile.write("time,distance,heading,targetHeading");

        waitForStart();

        double startHeading = drive.getHeading();

        while (opModeIsActive()) {
            double rawDistance = smoother.update(distanceSensor.getDistance(DistanceUnit.INCH));
            double headingError = Math.toRadians(drive.getHeadingError());
            double distance = rawDistance * Math.cos(headingError) - SENSOR_OFFSET * Math.sin(headingError);

            double forwardSpeed = BASE_FORWARD_SPEED;
            double distanceError = 0;

            if (distance < TARGET_DISTANCE_RANGE[0] || distance > TARGET_DISTANCE_RANGE[1]) {
                distanceError = distance - (TARGET_DISTANCE_RANGE[0] + TARGET_DISTANCE_RANGE[1]);
            } else if (Math.abs(headingError) > MAX_ACCEPTABLE_HEADING_ERROR) {
                forwardSpeed = 0;
            }
            double targetHeading = startHeading + 5 * distanceError;
            double heading = drive.getHeading();
            drive.setTargetHeading(targetHeading);
            drive.setVelocity(new Vector2D(0, forwardSpeed));

            telemetry.addData("pidCoeff", drive.getController().toString());
            telemetry.addData("targetHeading", targetHeading);
            telemetry.addData("heading", heading);
            telemetry.addData("distance", distance);
            telemetry.update();

            dataFile.write(String.format("%d,%f,%f,%f", System.currentTimeMillis(), distance, drive.getHeading(), drive.getTargetHeading()));

            idle();
        }

        dataFile.close();
    }
}
