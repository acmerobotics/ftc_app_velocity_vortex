package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.EnhancedMecanumDrive;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.mech.BeaconRam;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.acmerobotics.velocityvortex.sensors.RatioColorAnalyzer;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import static com.acmerobotics.library.configuration.OpModeConfiguration.*;

/**
 * @author Ryan Brott
 */

public abstract class Auto extends LinearOpMode {

    public static final int BLUE_LED_CHANNEL = 0;
    public static final int RED_LED_CHANNEL = 1;

    protected OpModeConfiguration opModeConfiguration;
    protected AllianceColor allianceColor;
    protected ParkDest parkDest;
    protected double allianceModifier;
    protected int delay, numBalls;
    protected RobotProperties properties;

    protected MecanumDrive basicDrive;

    protected FixedLauncher launcher;

    protected DeviceInterfaceModule dim;

    @Override
    public void runOpMode() throws InterruptedException {
        opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        allianceColor = opModeConfiguration.getAllianceColor();
        delay = opModeConfiguration.getDelay();
        numBalls = opModeConfiguration.getNumberOfBalls();
        parkDest = opModeConfiguration.getParkDest();
        allianceModifier = (allianceColor == AllianceColor.BLUE) ? 1 : -1;
        properties = opModeConfiguration.getRobotType().getProperties();

        dim = hardwareMap.deviceInterfaceModule.get("dim");
        if (allianceColor == AllianceColor.BLUE) {
            dim.setLED(BLUE_LED_CHANNEL, true);
            dim.setLED(RED_LED_CHANNEL, false);
        } else {
            dim.setLED(BLUE_LED_CHANNEL, false);
            dim.setLED(RED_LED_CHANNEL, true);
        }

        launcher = new FixedLauncher(hardwareMap);

        basicDrive = new MecanumDrive(hardwareMap, properties.getWheelRadius());

        initOpMode();

        displayConfigSummary();

        waitForStart();

        resetStartTime();

        Thread.sleep(1000 * delay);
    }

    public abstract void initOpMode();

    public void displayConfigSummary() {
        telemetry.addData("robot_type", opModeConfiguration.getRobotType());
        telemetry.addData("alliance_color", allianceColor);
        telemetry.addData("delay", delay);
        telemetry.addData("num_balls", numBalls);
        telemetry.addData("park_dest", parkDest);
        telemetry.update();
    }

}
