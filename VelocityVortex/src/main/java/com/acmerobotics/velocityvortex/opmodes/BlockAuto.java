package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.FieldNavigator;
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

    private BNO055IMU imu;

    @Override
    public void initOpMode() {
        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);
//        nav = new FieldNavigator(drive, allianceColor == OpModeConfiguration.AllianceColor.BLUE);

        nav.setPosition(1.5 * TILE_SIZE, -3 * TILE_SIZE + properties.getRobotSize() / 2);

        launcher = new FixedLauncher(hardwareMap);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndFire();

        moveToLineAndWait();

        drive.move(-3 * TILE_SIZE, MOVEMENT_SPEED, this);

//        go();
    }

    public void moveAndFire() {
//        nav.moveTo(1.5 * TILE_SIZE, -2 * TILE_SIZE, this);
        nav.moveTo(0.5 * TILE_SIZE, -TILE_SIZE, 45, this);

        Auto.fireBalls(launcher, numBalls, this);
    }

    public void moveToLineAndWait() {
        drive.move(-1 * TILE_SIZE * ROOT2 + 6, MOVEMENT_SPEED, this);

        drive.turnSync(-45 * allianceModifier, this);
        while (opModeIsActive() && getRuntime() < 10) {
            idle();
        }
    }
}
