package com.acmerobotics.velocityvortex.opmodes;

import android.annotation.SuppressLint;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.BeaconRam;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.acmerobotics.velocityvortex.sensors.TCS34725ColorSensor;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.Date;

@Autonomous(name="Cap Ball Auto")
public class CapBallAuto extends LinearOpMode {

    public static final double TILE_SIZE = 24;

    private OpModeConfiguration opModeConfiguration;
    private MecanumDrive basicDrive;
    private FixedLauncher launcher;
    private RobotProperties properties;

    @Override
    public void runOpMode() throws InterruptedException {
        opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        properties = opModeConfiguration.getRobotType().getProperties();

        basicDrive = new MecanumDrive(hardwareMap, properties.getWheelRadius());

        launcher = new FixedLauncher(hardwareMap);

        telemetry.addData("robot_type", opModeConfiguration.getRobotType());
        telemetry.addData("delay", opModeConfiguration.getDelay());
        telemetry.addData("num_balls", opModeConfiguration.getNumberOfBalls());
        telemetry.update();

        waitForStart();

        Thread.sleep(1000 * opModeConfiguration.getDelay());

        moveAndShoot();

        pushAndPark();

    }

    public void moveAndShoot() {

        basicDrive.move (-((Math.sqrt(2) * TILE_SIZE) + 3), .5);

        launcher.fireBalls(opModeConfiguration.getNumberOfBalls());


    }

    public void pushAndPark () {

        basicDrive.move (Math.sqrt(2) * -TILE_SIZE, .5);

    }
}
