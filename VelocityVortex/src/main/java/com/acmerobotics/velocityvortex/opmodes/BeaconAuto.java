package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.BeaconFollower;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.Vector2D;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.acmerobotics.velocityvortex.sensors.ThresholdColorAnalyzer;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import static com.acmerobotics.library.configuration.OpModeConfiguration.AllianceColor;
import static com.acmerobotics.velocityvortex.sensors.ColorAnalyzer.BeaconColor;

@Autonomous(name = "Beacon Auto")
public class BeaconAuto extends Auto {

    private FixedLauncher launcher;

    private EnhancedMecanumDrive drive;
    private BNO055IMU imu;

    private BeaconFollower beaconFollower;

    private BeaconColor targetColor;
    private ColorSensor colorSensor;
    private ColorAnalyzer colorAnalyzer;

    private BeaconPusher beaconPusher;
    private int beaconsPressed;

    private ElapsedTime timer;

    private VoltageSensor voltageSensor;
    private double voltage;
    private double fireDistance;

    @Override
    public void initOpMode() {
        targetColor = (allianceColor == AllianceColor.BLUE) ? BeaconColor.BLUE : BeaconColor.RED;

        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        AdafruitBNO055IMU.Parameters parameters = new AdafruitBNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(parameters);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);

        DistanceSensor distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));

        launcher = new FixedLauncher(hardwareMap);
        launcher.setTrim(-.05);

        colorSensor = hardwareMap.colorSensor.get("color");
        colorSensor.setI2cAddress(I2cAddr.create8bit(0x3e));
        colorSensor.enableLed(false);

        colorAnalyzer = new ThresholdColorAnalyzer(colorSensor, 5, 5);

        beaconPusher = new BeaconPusher(hardwareMap);

        beaconFollower = new BeaconFollower(drive, distanceSensor, colorAnalyzer, beaconPusher, properties);
        beaconFollower.setLogFile(new DataFile(getFileName("BeaconFollower")));

//        voltageSensor = hardwareMap.voltageSensor.get("launcher");
//        voltage = voltageSensor.getVoltage();
//        double voltageError = Range.clip(voltage - 12, 0, 5);
        fireDistance = 24;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        moveAndFire();

        beaconFollower.pushBeacons(2, allianceModifier, targetColor, this);

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
        basicDrive.move(-fireDistance, 1, this);

        launcher.fireBalls(numBalls, this);

        drive.turnSync(allianceModifier * -110, this);

        basicDrive.move(50, 1, this);

        if (allianceColor == OpModeConfiguration.AllianceColor.BLUE) {
            drive.setTargetHeading(180);
        } else {
            drive.setTargetHeading(0);
        }

        drive.turnSync(0, this);
    }

    private void centerPark() {
        beaconFollower.moveToDistance(20, 2 * BeaconFollower.BEACON_SPREAD, this);

        drive.turnSync(0, this);
        basicDrive.move(-2 * allianceModifier * TILE_SIZE, 1, this);
        drive.turnSync(90, this);
        basicDrive.move(-TILE_SIZE * 1.3, 1, this);
    }

    private void cornerPark() {
        beaconFollower.moveToDistance(15, 2 * BeaconFollower.BEACON_SPREAD, this);

        if (allianceColor == AllianceColor.RED) {
            drive.turnSync(180, this);
        }

        drive.setVelocity(new Vector2D(0, -1));

        while (opModeIsActive() && Math.abs(imu.getAngularOrientation().thirdAngle) < 7) {
            drive.update();
            idle();
        }
    }
}
