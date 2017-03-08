package com.acmerobotics.velocityvortex.opmodes;

import android.media.MediaPlayer;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.FieldNavigator;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.WallFollower;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DistanceSensor;

/**
 * @author Ryan Brott
 */

@Autonomous(name = "Block Auto", group="Autonomous")
public class BlockAuto extends Auto {

    private FixedLauncher launcher;

    private EnhancedMecanumDrive drive;
    private FieldNavigator nav;
    private WallFollower wallFollower;

    private BNO055IMU imu;

    private double halfWidth;

    @Override
    public void initOpMode() {
        halfWidth = properties.getRobotSize() / 2;

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);
        drive.setInitialHeading(180);
        nav = new FieldNavigator(drive, allianceColor);
        nav.setLocation(4 * TILE_SIZE + halfWidth, halfWidth);

        DistanceSensor distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        wallFollower = new WallFollower(drive, distanceSensor, properties);

        launcher = new FixedLauncher(hardwareMap);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndFire();

        moveToLineAndWait();
    }

    public void moveAndFire() {
        nav.moveTo(4 * TILE_SIZE + halfWidth, TILE_SIZE - halfWidth, this);
        nav.moveTo(3.5 * TILE_SIZE, 1.5 * TILE_SIZE, this);

        Auto.fireBalls(launcher, numBalls, this);
    }

    public void moveToLineAndWait() {
        nav.moveTo(2.5 * TILE_SIZE, 2.5 * TILE_SIZE, 0, this);

        while (opModeIsActive() && getRuntime() < 10) {
            idle();
        }

        wallFollower.setTargetDistance(2.25 * TILE_SIZE, 2);
        wallFollower.setForwardSpeed(1);
        MecanumDrive basicDrive = drive.getDrive();
        basicDrive.resetEncoders();

        while (basicDrive.getMeanPosition() < 3 * TILE_SIZE) {
            wallFollower.update();
            idle();
        }
        drive.stop();

//        nav.moveTo(2.5 * TILE_SIZE, 5.5 * TILE_SIZE, this);
    }
}
