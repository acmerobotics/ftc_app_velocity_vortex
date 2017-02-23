package com.acmerobotics.velocityvortex.opmodes.tuner;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.velocityvortex.drive.BeaconFollower;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.velocityvortex.drive.WallFollower;
import com.acmerobotics.velocityvortex.sensors.MaxSonarEZ1UltrasonicSensor;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DistanceSensor;

/**
 * @author Ryan
 */

@TeleOp(name="Wall PID Tuner", group="PID Tuner")
public class WallPIDTuner extends Tuner {

    private EnhancedMecanumDrive drive;
    private WallFollower wallFollower;

    private OpModeConfiguration configuration;
    private RobotProperties properties;

    @Override
    public void init() {
        super.init();

        configuration = new OpModeConfiguration(hardwareMap.appContext);
        properties = configuration.getRobotType().getProperties();

        MecanumDrive basicDrive = new MecanumDrive(hardwareMap, properties);

        BNO055IMU imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        drive = new EnhancedMecanumDrive(basicDrive, imu, properties);

        DistanceSensor distanceSensor = new MaxSonarEZ1UltrasonicSensor(hardwareMap.analogInput.get("maxSonar"));
        wallFollower = new WallFollower(drive, distanceSensor, properties);

        wallFollower.setTargetDistance(BeaconFollower.BEACON_DISTANCE, BeaconFollower.BEACON_SPREAD);
    }

    @Override
    public void loop() {
        super.loop();

        telemetry.addData("target_distance", wallFollower.getTargetDistance());
        telemetry.addData("target_spread", wallFollower.getTargetSpread());
        telemetry.addData("error", wallFollower.getDistanceError());

        wallFollower.update();
    }

    @Override
    protected PIDController getController() {
        return wallFollower.getController();
    }
}
