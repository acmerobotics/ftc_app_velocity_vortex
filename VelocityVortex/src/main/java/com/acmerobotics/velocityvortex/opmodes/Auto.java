package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.util.ClassFilter;

import static com.acmerobotics.library.configuration.OpModeConfiguration.AllianceColor;
import static com.acmerobotics.library.configuration.OpModeConfiguration.MatchType;
import static com.acmerobotics.library.configuration.OpModeConfiguration.ParkDest;

/**
 * @author Ryan Brott
 */

public abstract class Auto extends LinearOpMode {

    public static final int BLUE_LED_CHANNEL = 0;
    public static final int RED_LED_CHANNEL = 1;

    public static final double MOVEMENT_SPEED = 0.75;

    public static final double ROOT2 = Math.sqrt(2);

    public static final double TILE_SIZE = 24;

    protected OpModeConfiguration opModeConfiguration;
    protected AllianceColor allianceColor;
    protected ParkDest parkDest;
    protected int delay, numBalls, matchNumber, allianceModifier;
    protected double lastHeading;
    protected MatchType matchType;
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
        matchType = opModeConfiguration.getMatchType();
        matchNumber = opModeConfiguration.getMatchNumber();
        allianceModifier = (allianceColor == AllianceColor.BLUE) ? 1 : -1;
        lastHeading = opModeConfiguration.getLastHeading();
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

        basicDrive = new MecanumDrive(hardwareMap, properties);

        initOpMode();

        displayConfigSummary();

        waitForStart();

        resetStartTime();

        sleep(1000 * delay);
    }

    public abstract void initOpMode();

    public void displayConfigSummary() {
        telemetry.addData("robot_type", opModeConfiguration.getRobotType());
        telemetry.addData("match_type", matchType);
        if (matchType != MatchType.PRACTICE) telemetry.addData("match_number", matchNumber);
        telemetry.addData("alliance_color", allianceColor);
        telemetry.addData("delay", delay);
        telemetry.addData("num_balls", numBalls);
        telemetry.addData("park_dest", parkDest);
        telemetry.addData("last_heading", lastHeading);
        telemetry.update();
    }

    public String getFileName(String tag) {
        return tag + "_" + matchType + "_" + ((matchType == OpModeConfiguration.MatchType.PRACTICE) ? System.currentTimeMillis() : matchNumber) + ".csv";
    }

}
