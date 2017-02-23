package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.BeaconFollower;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.acmerobotics.velocityvortex.sensors.ThresholdColorAnalyzer;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import static com.acmerobotics.library.configuration.OpModeConfiguration.AllianceColor;
import static com.acmerobotics.velocityvortex.sensors.ColorAnalyzer.BeaconColor;

@Autonomous(name = "Beacon Auto", group="Autonomous")
public class BeaconAuto extends Auto {

    public static final double FIRE_DISTANCE = 24;
    public static final int RAMP_PARK_ELEVATION = 7;

    private FixedLauncher launcher;

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;

    private BeaconFollower beaconFollower;

    private BeaconColor targetColor;
    private ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;

    private BeaconPusher beaconPusher;

    private boolean targetFirstLastBeacon;

    @Override
    public void initOpMode() {
        targetColor = (allianceColor == AllianceColor.BLUE) ? BeaconColor.BLUE : BeaconColor.RED;

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);

        DistanceSensor distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));

        launcher = new FixedLauncher(hardwareMap);
//        launcher.setTrim(-.05);

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, 10, 4);

        beaconPusher = new BeaconPusher(hardwareMap, new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM));

        beaconFollower = new BeaconFollower(drive, distanceSensor, colorAnalyzer, beaconPusher, properties);
        beaconFollower.setLogFile(new DataFile(getFileName("BeaconFollower")));
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndFire();

        beaconFollower.moveToDistance(BeaconFollower.BEACON_DISTANCE, BeaconFollower.BEACON_SPREAD / 2.0, this);

        targetFirstLastBeacon = beaconFollower.pushBeacons(2, allianceModifier, targetColor, this);

        switch (parkDest) {
            case NONE:
                break;
            case CENTER:
                centerPark();
                break;
            case CORNER:
                cornerPark();
                break;
        }
    }

    public void moveAndFire() {
        drive.move(-FIRE_DISTANCE, MOVEMENT_SPEED, this);

        Auto.fireBalls(launcher, numBalls, this);

        if (allianceColor == AllianceColor.BLUE) {
            drive.turnSync(-110, this);
        } else {
            drive.turnSync(100, this);
        }

        drive.move(54, MOVEMENT_SPEED, this);

        if (allianceColor == OpModeConfiguration.AllianceColor.BLUE) {
            drive.setTargetHeading(180);
        } else {
            drive.setTargetHeading(0);
        }

        drive.turnSync(0, this);
    }

    private void centerPark() {
        //beaconFollower.moveToDistance(12, 2 * BeaconFollower.BEACON_SPREAD, this);

        if (allianceColor == AllianceColor.BLUE) {
            if (targetFirstLastBeacon) {
                drive.turnSync(45, this);
            } else {
                drive.turnSync(40, this);
            }
        } else {
            if (targetFirstLastBeacon) {
                drive.turnSync(127, this);
            } else {
                drive.turnSync(135, this);
            }
        }
        drive.move(-2 * ROOT2 * TILE_SIZE - 3, MOVEMENT_SPEED, this);

        drive.setTargetHeading(180);
        drive.turnSync(0, this);
//        drive.turnSync(0, this);
//        drive.move(-2 * allianceModifier * TILE_SIZE, MOVEMENT_SPEED, this);
//        drive.turnSync(90, this);
//        drive.move(-TILE_SIZE * 1.3, MOVEMENT_SPEED, this);
    }

    private void cornerPark() {
        beaconFollower.moveToDistance(15, 2 * BeaconFollower.BEACON_SPREAD, this);

        if (allianceColor == AllianceColor.RED) {
            drive.turnSync(180, this);
        }

            drive.setVelocity(new Vector2D(0, -1));

            while (opModeIsActive() && Math.abs(imu.getAngularOrientation().thirdAngle) < RAMP_PARK_ELEVATION) {
                drive.update();
            idle();
        }
    }
}
