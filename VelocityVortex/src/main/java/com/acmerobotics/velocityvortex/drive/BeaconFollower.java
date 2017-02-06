package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.opmodes.Auto;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

/**
 * @author Ryan
 */

public class BeaconFollower extends WallFollower {

    public static final double BEACON_DISTANCE = 6.4;
    public static final double BEACON_SPREAD = 0.3;
    public static final double BEACON_SEARCH_SPEED = 0.6;

    private ColorAnalyzer colorAnalyzer;
    private BeaconPusher beaconPusher;

    private DataFile logFile;

    public BeaconFollower(EnhancedMecanumDrive drive, DistanceSensor sensor, ColorAnalyzer analyzer, BeaconPusher pusher, RobotProperties properties) {
        super(drive, sensor, properties);
        colorAnalyzer = analyzer;
        beaconPusher = pusher;
    }

    public void pushBeacons(int numBeacons, int direction, ColorAnalyzer.BeaconColor targetColor) {
        pushBeacons(numBeacons, direction, targetColor, null);
    }

    public void pushBeacons(int numBeacons, int direction, ColorAnalyzer.BeaconColor targetColor, LinearOpMode opMode) {
        int beaconsPressed = 0;
        while (opMode == null || opMode.opModeIsActive()) {
            ColorAnalyzer.BeaconColor color = colorAnalyzer.getBeaconColor();

            if (logFile != null) {
                logFile.write(String.format("%d,%s,%f,%f", System.currentTimeMillis(), color, colorAnalyzer.red(), colorAnalyzer.blue()));
            }

            if (color == targetColor) {
                drive.stop();

                moveToDistance(BEACON_DISTANCE, BEACON_SPREAD, opMode);

                drive.turnSync(0, 1, opMode);

                beaconPusher.autoPush();
                beaconsPressed++;

                if (beaconsPressed < numBeacons) {
                    drive.move(direction * Auto.TILE_SIZE, 1, opMode);
                } else {
                    drive.stop();
                    return;
                }
            } else {
                setForwardSpeed(direction * BEACON_SEARCH_SPEED);
                setTargetDistance(BEACON_DISTANCE, BEACON_SPREAD);
                update();
            }

            Thread.yield();
        }
    }

    public void setLogFile(DataFile file) {
        if (file == null) {
            if (logFile != null) logFile.close();
            logFile = null;
        } else {
            logFile = file;
            logFile.write("analyzer: " + colorAnalyzer.toString());
            logFile.write("time,color,red,blue");
        }
    }
}
