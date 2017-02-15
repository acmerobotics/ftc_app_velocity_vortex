package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
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

@Autonomous(name = "Block Auto")
public class BlockAuto extends Auto {

    private FixedLauncher launcher;

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;

    private WallFollower wallFollower;

    @Override
    public void initOpMode() {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);

        DistanceSensor distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        wallFollower = new WallFollower(drive, distanceSensor, properties);

        launcher = new FixedLauncher(hardwareMap);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndFire();

        moveToLineAndWait();

        basicDrive.move(-3 * TILE_SIZE, MOVEMENT_SPEED, this);

//        go();
    }

    public void moveAndFire() {
        basicDrive.move(-((Math.sqrt(2) * TILE_SIZE) + 3), MOVEMENT_SPEED, this);

        launcher.fireBalls(numBalls, this);
    }

    public void moveToLineAndWait() {
        basicDrive.move(-1 * TILE_SIZE * ROOT2 + 3, MOVEMENT_SPEED, this);

        drive.turnSync(-45 * allianceModifier, this);
        while (opModeIsActive() && getRuntime() < 10) {
            idle();
        }
    }

//    public void go() throws InterruptedException {
//        basicDrive.move(-TILE_SIZE * 2 + 2, 1, this);
//        drive.turnSync(90, this);
//        basicDrive.move(-.5 * TILE_SIZE, 1, this);
//        wallFollower.moveToDistance(BeaconFollower.BEACON_DISTANCE, BeaconFollower.BEACON_SPREAD, this);
//    }
}
