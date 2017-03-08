package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.BeaconFollower;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.FieldNavigator;
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

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import static com.acmerobotics.library.configuration.OpModeConfiguration.AllianceColor;
import static com.acmerobotics.velocityvortex.sensors.ColorAnalyzer.BeaconColor;

@Autonomous(name = "Beacon Auto", group="Autonomous")
public class BeaconAuto extends Auto {

    public static final double FIRE_DISTANCE = 24;
    public static final int RAMP_PARK_ELEVATION = 7;
    public static final double BEACON_BUTTON_GAP = 5.3;
    public static final double WALL_DISTANCE = 5;

    private FixedLauncher launcher;

    private EnhancedMecanumDrive drive;
    private FieldNavigator nav;
    private BNO055IMU imu;

    private BeaconFollower beaconFollower;

    private BeaconColor targetColor;
    private ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;

    private BeaconPusher beaconPusher;

    private boolean targetFirstLastBeacon;
    private double halfWidth;

    @Override
    public void initOpMode() {
        halfWidth = properties.getRobotSize() / 2;

        targetColor = (allianceColor == AllianceColor.BLUE) ? BeaconColor.BLUE : BeaconColor.RED;

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);
        drive.setInitialHeading(180);
        drive.setLogFile(new DataFile(getFileName("EnhancedMecanumDrive")));

        nav = new FieldNavigator(drive, allianceColor);
        nav.setLocation(3 * TILE_SIZE - halfWidth, halfWidth);

        DistanceSensor distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));

        launcher = new FixedLauncher(hardwareMap);
//        launcher.setTrim(-.05);

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, Auto.BLUE_THRESHOLD, Auto.RED_THRESHOLD);

        beaconPusher = new BeaconPusher(hardwareMap, new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM));
        beaconPusher.setLogFile(new DataFile(getFileName("BeaconPusher")));

        beaconFollower = new BeaconFollower(drive, distanceSensor, colorAnalyzer, beaconPusher, properties);
        beaconFollower.setLogFile(new DataFile(getFileName("BeaconFollower")));
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndFire();

        beaconFollower.moveToDistance(BeaconFollower.BEACON_DISTANCE, BeaconFollower.BEACON_SPREAD / 2.0, this);

        targetFirstLastBeacon = beaconFollower.pushBeacons(1, allianceModifier, targetColor, this);
        double x = WALL_DISTANCE + halfWidth;
        double y = 4.5 * TILE_SIZE;
        if (targetFirstLastBeacon) {
            y -= BEACON_BUTTON_GAP / 2;
        } else {
            y += BEACON_BUTTON_GAP / 2;
        }
        nav.setLocation(x, y);

        beaconPusher.getServo().setPower(-1);

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
        nav.moveTo(3 * TILE_SIZE - halfWidth, TILE_SIZE + halfWidth, this);

        Auto.fireBalls(launcher, numBalls, this);

        nav.moveTo(WALL_DISTANCE + halfWidth, 2.15 * TILE_SIZE, allianceColor == AllianceColor.BLUE ? 0 : 180, this);
    }

    private void centerPark() {
        if (allianceColor == AllianceColor.RED) {
            drive.turnSync(90, this);
        }

        nav.moveTo(2.5 * TILE_SIZE, 2.5 * TILE_SIZE, this);
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
