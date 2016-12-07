package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.i2c.SparkFunLineFollowingArray;
import com.acmerobotics.velocityvortex.localization.VuforiaInterface;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.vision.VuforiaCamera;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

@Autonomous(name="Dead Reckoning Auto")
public class DeadReckoningAuto extends LinearOpMode {

    public static final PIDController.PIDCoefficients LINE_PID_COEFF = new PIDController.PIDCoefficients(-0.1, 0, 0);
    public static final Vector2D BASE_VELOCITY = new Vector2D(-0.25, 0);

    public static final int PULSES_PER_REV = 1680;
    public static final double DIAMETER = 4; // inches
    public static final double ROBOT_LENGTH = 18; // inches
    public static final double TILE_WIDTH = 24; // inches

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;
    private VuforiaCamera camera;
    private BeaconPusher pusher;
    private SparkFunLineFollowingArray lineSensor;

    @Override
    public void runOpMode() throws InterruptedException {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        OpModeConfiguration config = new OpModeConfiguration(hardwareMap.appContext);

        pusher = new BeaconPusher(hardwareMap, config.getRobotType().getProperties());

        lineSensor = new SparkFunLineFollowingArray(hardwareMap.i2cDeviceSynch.get("lineArray"));

        drive = new EnhancedMecanumDrive(new MecanumDrive(hardwareMap), imu);

        VuforiaInterface vuforia = new VuforiaInterface("", 0);
        camera = new VuforiaCamera(hardwareMap.appContext, vuforia.getLocalizer());
        camera.initSync();

        waitForStart();

        moveForward(2 * TILE_WIDTH - ROBOT_LENGTH / 2);

        drive.turnSync(45);

        moveForward(1.5 * TILE_WIDTH * Math.sqrt(2));

        drive.turnSync(-45);

        List<Beacon> beacons = new ArrayList<Beacon>();
        while (beacons.isEmpty()) {
            Mat frame = camera.getLatestFrame();
            BeaconAnalyzer.analyzeImage(frame, beacons);
            idle();
        }
        Beacon b = beacons.get(0);
        if (b.getLeftRegion().getColor() == Beacon.BeaconColor.BLUE) {
            pusher.leftUp();
        } else {
            pusher.rightUp();
        }

        drive.setVelocity(BASE_VELOCITY);
        lineSensor.scan();
        while (opModeIsActive() && lineSensor.getDensity() == 0) {
            lineSensor.scan();
            Thread.yield();
        }
        drive.stop();

        followLine(lineSensor, drive.getDrive(), this);
    }

    public void moveForward(double inches) {
        drive.moveForward((int)((inches * PULSES_PER_REV) / (Math.PI * DIAMETER)));
    }

    public static void followLine(SparkFunLineFollowingArray lineSensor, MecanumDrive drive, LinearOpMode mode) {
        PIDController lineController = new PIDController(LINE_PID_COEFF);
        while(mode.opModeIsActive()) {
            lineSensor.scan();
            int[] values = lineSensor.getRawArray();
            int error = (values[0] + values[1] + values[2] + values[3]) - (values[4] + values[5] + values[6] + values[7]);
            double update = lineController.update(error);
            drive.setVelocity(BASE_VELOCITY.copy(), update);
            mode.telemetry.addData("error", error);
            mode.telemetry.addData("update", update);
            drive.log(mode.telemetry);
            mode.telemetry.update();
            mode.idle();
        }
    }
}
