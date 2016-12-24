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
import com.acmerobotics.velocityvortex.mech.BeaconSwitch;
import com.acmerobotics.velocityvortex.vision.VuforiaCamera;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import static com.acmerobotics.library.configuration.OpModeConfiguration.AllianceColor.BLUE;

@Autonomous(name="Dead Reckoning Auto")
public class DeadReckoningAuto extends LinearOpMode {

    public static final PIDController.PIDCoefficients LINE_PID_COEFF = new PIDController.PIDCoefficients(-0.06, 0, 0);
    public static final Vector2D BASE_VELOCITY = new Vector2D(-0.25, 0);

    public static final int PULSES_PER_REV = 1120;
    public static final double DIAMETER = 4; // inches
    public static final double ROBOT_LENGTH = 18; // inches
    public static final double TILE_WIDTH = 24; // inches

    public static final int MAX_TURN_ERROR = 3;

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;
    private VuforiaCamera camera;
    //private BeaconPusher pusher;
    private BeaconSwitch beacon;
    private SparkFunLineFollowingArray lineSensor;
    private OpModeConfiguration configuration;
    private OpModeConfiguration.AllianceColor allianceColor;
    private Beacon.BeaconColor targetBeaconColor;

    @Override
    public void runOpMode() throws InterruptedException {
        configuration = new OpModeConfiguration(hardwareMap.appContext);
        allianceColor = configuration.getAllianceColor();
        targetBeaconColor = allianceColor == BLUE ? Beacon.BeaconColor.BLUE : Beacon.BeaconColor.RED;

        VuforiaInterface vuforia = new VuforiaInterface("", 0);
        camera = new VuforiaCamera(hardwareMap.appContext, vuforia.getLocalizer());
        camera.initSync();

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        beacon = new BeaconSwitch(hardwareMap);
        beacon.store();

        lineSensor = new SparkFunLineFollowingArray(hardwareMap.i2cDeviceSynch.get("lineArray"));

        drive = new EnhancedMecanumDrive(new MecanumDrive(hardwareMap, MecanumDrive.Configuration.createFixedRadius(4)), imu);

        waitForStart();

        sleep(1000 * configuration.getDelay());

        moveForward(14);

        if (allianceColor == BLUE) {
            drive.turnSync(45, MAX_TURN_ERROR);
        } else {
            drive.turnSync(-45, MAX_TURN_ERROR);
        }

        moveForward(1.5 * TILE_WIDTH * Math.sqrt(2));

        if (allianceColor == BLUE) {
            drive.turnSync(-45, MAX_TURN_ERROR);
        } else {
            drive.turnSync(45, MAX_TURN_ERROR);
        }

        List<Beacon> beacons = new ArrayList<Beacon>();
        while (beacons.isEmpty()) {
            Mat frame = camera.getLatestFrame();
            BeaconAnalyzer.analyzeImage(frame, beacons);
            idle();
        }
        Beacon b = beacons.get(0);
        if (b.getLeftRegion().getColor() == targetBeaconColor) {
            //pusher.leftUp();
            beacon.left();
        } else {
            //pusher.rightUp();
            beacon.right();
        }

//        drive.setVelocity(BASE_VELOCITY);
//        lineSensor.scan();
//        while (opModeIsActive() && lineSensor.getDensity() == 0) {
//            lineSensor.scan();
//            Thread.yield();
//        }
//        drive.stop();
//
//        followLine(lineSensor, drive.getDrive(), this);
    }

    public void moveForward(double inches) {
        drive.moveForward((int)((inches * PULSES_PER_REV) / (Math.PI * DIAMETER)));
    }

    public static void followLine(SparkFunLineFollowingArray lineSensor, EnhancedMecanumDrive drive, LinearOpMode mode) {
        PIDController lineController = new PIDController(LINE_PID_COEFF);
        MecanumDrive basic = drive.getDrive();
        double lastError = 0;
        while(mode.opModeIsActive()) {
            lineSensor.scan();
            int[] values = lineSensor.getRawArray();
            double sum = 0;
            double count = 0;
            for (int i = 0; i < 8; i++) {
                sum += values[i] * (i - 4);
                count += values[i];
            }
            double error;
            if (count == 0) {
                error = Math.signum(lastError) * 5;
            } else {
                error = sum / count;
                lastError = error;
            }
//            if (lineSensor.getDensity() == 0) {
//                error = Math.signum(lastError) * 5;
//            } else {
//                lastError = error;
//            }
//            error += 0.4 * drive.getHeadingError();
            double update = lineController.update(error);
            // removed the copy -- watch out
            basic.setVelocity(BASE_VELOCITY, update);

            mode.telemetry.addData("error", error);
            mode.telemetry.addData("update", update);
            basic.log(mode.telemetry);
            mode.telemetry.update();
            mode.idle();
        }
    }
}
