package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Autonomous(name="Wall Auto")
public class WallAuto extends LinearOpMode {

    public static final double TARGET_DISTANCE = 12;
    public static final double DISTANCE_SPREAD = 1;
    public static final double DISTANCE_SMOOTHER_EXP = 0.05;
    public static final double BASE_FORWARD_SPEED = 0.35;
    public static final double SENSOR_OFFSET = 7.625;
    public static final double TILE_SIZE = 24;

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

//        dataFile = new DataFile("wall_auto_" + System.currentTimeMillis() + ".csv");
//        dataFile.write("distance,targetDistance,heading,targetHeading");

        waitForStart();

//        DbgLog.msg("[ROBOT] moving forward");

        basicDrive.move((TILE_SIZE - properties.getRobotSize()), 0.6);

//        DbgLog.msg("[ROBOT] turning 45");

        drive.turnSync(45);

//        DbgLog.msg("[ROBOT] moving forward again");

        basicDrive.move(2 * TILE_SIZE * Math.sqrt(2) - 8, 0.6);

//        DbgLog.msg("[ROBOT] turning -45");

        drive.turnSync(-45);

//        DbgLog.msg("[ROBOT] following wall");

        followWall();

//        dataFile.close();
    }

    public void followWall() {
        double startHeading = drive.getTargetHeading();

        while (opModeIsActive()) {
            double rawDistance = smoother.update(distanceSensor.getDistance(DistanceUnit.INCH));
            double headingError = Math.toRadians(drive.getHeadingError());
            double distance = rawDistance * Math.cos(headingError) - SENSOR_OFFSET * Math.sin(headingError);

            double forwardSpeed = BASE_FORWARD_SPEED;
            double distanceError = 0;

            if (Math.abs(distance - TARGET_DISTANCE) > DISTANCE_SPREAD) {
                distanceError = distance - TARGET_DISTANCE;
            }
            double targetHeading = startHeading + Range.clip(2 * distanceError, -15, 15);
            double heading = drive.getHeading();
            drive.setTargetHeading(targetHeading);
            drive.setVelocity(new Vector2D(0, forwardSpeed));
            drive.update();

            telemetry.addData("pidCoeff", drive.getController().toString());
            telemetry.addData("targetHeading", targetHeading);
            telemetry.addData("heading", heading);
            telemetry.addData("distance", distance);
            telemetry.update();

//            dataFile.write(String.format("%f,%f,%f,%f", distance, TARGET_DISTANCE, drive.getHeading(), drive.getTargetHeading()));

            idle();
        }
    }
}
