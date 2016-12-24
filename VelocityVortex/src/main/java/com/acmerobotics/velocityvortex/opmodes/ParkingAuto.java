package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.i2c.SparkFunLineFollowingArray;
import com.acmerobotics.velocityvortex.mech.BeaconSwich;
import com.acmerobotics.velocityvortex.mech.Launcher;
import com.acmerobotics.velocityvortex.vision.FastCamera;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.R;

import static com.acmerobotics.library.configuration.OpModeConfiguration.AllianceColor.BLUE;

/**
 * Created by kelly on 12/11/2016.
 */

@Autonomous(name="Parking Auto")
public class ParkingAuto extends LinearOpMode{

    public static final PIDController.PIDCoefficients LINE_PID_COEFF = new PIDController.PIDCoefficients(-0.06, 0, 0);
    public static final Vector2D BASE_VELOCITY = new Vector2D(-0.25, 0);

    public static final int PULSES_PER_REV = 1120;
    public static final double DIAMETER = 4; // inches
    public static final double ROBOT_LENGTH = 18; // inches
    public static final double TILE_WIDTH = 24; // inches

    public static final int MAX_TURN_ERROR = 3;

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;
    //private VuforiaCamera camera;
    private FastCamera camera;
    //private BeaconPusher pusher;
    private BeaconSwich beacon;
    private SparkFunLineFollowingArray lineSensor;
    private OpModeConfiguration configuration;
    private OpModeConfiguration.AllianceColor allianceColor;
    private Beacon.BeaconColor targetBeaconColor;
    private Launcher launcher;

    public void runOpMode() {
        //configuration = new OpModeConfiguration(hardwareMap.appContext);
        //allianceColor = configuration.getAllianceColor();
        //targetBeaconColor = allianceColor == BLUE ? Beacon.BeaconColor.BLUE : Beacon.BeaconColor.RED;

        //VuforiaInterface vuforia = new VuforiaInterface("", 0);
        //camera = new VuforiaCamera(hardwareMap.appContext, vuforia.getLocalizer());
        //camera.initSync();
        //camera = new FastCamera(hardwareMap.appContext, R.id.cameraMonitorViewId);

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        //pusher = new BeaconPusher(hardwareMap, configuration.getRobotType().getProperties());
        beacon = new BeaconSwich(hardwareMap);
        beacon.store();

//        lineSensor = new SparkFunLineFollowingArray(hardwareMap.i2cDeviceSynch.get("lineArray"));

        drive = new EnhancedMecanumDrive(new MecanumDrive(hardwareMap), imu);

        launcher = new Launcher (hardwareMap);
        launcher.setMaxVelocity(.4);
        launcher.setTrim(0);

        waitForStart();

        launcher.setVelocity(.4);
        try {
            wait(5000);
            launcher.triggerUp();
            wait (1000);
            launcher.triggerDown();
            wait(4000);
            launcher.triggerUp();
            wait (1000);
            launcher.triggerDown();
            launcher.setVelocity (0);
        }catch (Exception e) {

        }

        moveBackward((TILE_WIDTH*3) - (ROBOT_LENGTH / 2));
//        int start = drive.getDrive().getMeanPosition();
//        int end = (int)(start - ((TILE_WIDTH*3)- (ROBOT_LENGTH / 2)));
//        //drive.setVelocity (new Vector2D(0,-1));
//        while (drive.getDrive().getMeanPosition() > end) {
//            drive.setVelocity(new Vector2D(0, -.5));
//        }
//        drive.setVelocity(EnhancedMecanumDrive.INERT_VELOCITY);
    }

    public void moveBackward(double inches) {
        drive.moveBackward((int)((inches * PULSES_PER_REV) / (Math.PI * DIAMETER)));
    }

}
