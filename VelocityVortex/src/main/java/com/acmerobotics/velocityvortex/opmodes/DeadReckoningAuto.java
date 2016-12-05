package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
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

    public static final int PULSES_PER_REV = 1680;
    public static final double DIAMETER = 4; // inches

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;
    private VuforiaCamera camera;
    private BeaconPusher pusher;

    @Override
    public void runOpMode() throws InterruptedException {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        pusher = new BeaconPusher(hardwareMap);

        drive = new EnhancedMecanumDrive(new MecanumDrive(hardwareMap), imu);

        VuforiaInterface vuforia = new VuforiaInterface("", 0);
        camera = new VuforiaCamera(hardwareMap.appContext, vuforia.getLocalizer());
        camera.initSync();

        waitForStart();

        moveForward(9);

        drive.turnSync(45);

        moveForward(12 * 3 * Math.sqrt(2));

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
    }

    public void moveForward(double inches) {
        drive.moveForward((int)((inches * PULSES_PER_REV) / (Math.PI * DIAMETER)));
    }
}
