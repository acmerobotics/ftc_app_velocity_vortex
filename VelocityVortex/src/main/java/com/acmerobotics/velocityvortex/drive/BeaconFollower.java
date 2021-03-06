package com.acmerobotics.velocityvortex.drive;

import android.os.SystemClock;

import com.acmerobotics.library.configuration.RobotProperties;
import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.opmodes.Auto;
import com.acmerobotics.velocityvortex.sensors.ColorAnalyzer;
import com.acmerobotics.velocityvortex.sensors.RatioColorAnalyzer;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

/**
 * @author Ryan
 */

public class BeaconFollower extends WallFollower {

    public static final double BEACON_DISTANCE = 8;
    public static final double BEACON_SPREAD = 1;
    public static final double BEACON_SEARCH_SPEED = 0.35;
    public static final double PUSHER_DISTANCE = 6.5;

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

    public boolean pushBeacons(int numBeacons, int direction, ColorAnalyzer.BeaconColor targetColor, LinearOpMode opMode) {
        int beaconsPressed = 0;
        boolean targetFirstLastBeacon = true;
        while (opMode == null || opMode.opModeIsActive()) {
            ColorAnalyzer.BeaconColor color = colorAnalyzer.getBeaconColor();

            if (logFile != null) {
                double blueRedRatio = (colorAnalyzer instanceof RatioColorAnalyzer) ? ((RatioColorAnalyzer) colorAnalyzer).getRatio() : Double.POSITIVE_INFINITY;
                logFile.write(String.format("%d,%s,%d,%d,%f,%f,%f,%f", System.currentTimeMillis(), color, colorAnalyzer.red(), colorAnalyzer.blue(), blueRedRatio, getDistance(), beaconPusher.getCurrentPosition(), drive.getHeading()));
            }

            beaconPusher.setTargetPosition(getDistance() - PUSHER_DISTANCE);
            beaconPusher.update();

            if (color == targetColor) {
                drive.stop();

                moveToDistance(BEACON_DISTANCE, BEACON_SPREAD, opMode);

                drive.turnSync(0, 1, opMode);

                beaconPusher.push(opMode);
                beaconsPressed++;

                beaconPusher.moveToPosition(getDistance() - PUSHER_DISTANCE, 0.25, opMode);

                if (beaconsPressed < numBeacons) {
                    targetFirstLastBeacon = true;
                    drive.getDrive().move( direction * Auto.TILE_SIZE, Auto.MOVEMENT_SPEED, opMode);
                } else {
                    drive.stop();
                    beaconPusher.retract();
                    return targetFirstLastBeacon;
                }
            } else {
                if (color != ColorAnalyzer.BeaconColor.UNKNOWN) {
                    targetFirstLastBeacon = false;
                }
                setForwardSpeed(direction * BEACON_SEARCH_SPEED);
                setTargetDistance(BEACON_DISTANCE, BEACON_SPREAD);
                update();
            }

            Thread.yield();
        }
        return false;
    }

    public void setLogFile(DataFile file) {
        if (file == null) {
            if (logFile != null) logFile.close();
            logFile = null;
        } else {
            logFile = file;
            logFile.write("color: " + colorAnalyzer.toString());
            logFile.write("distance: target=" + BEACON_DISTANCE + ",spread=" + BEACON_SPREAD);
            logFile.write("pusher: target=" + PUSHER_DISTANCE);
            logFile.write("time,color,red,blue,blueRedRatio,distance,pusherExt,heading");
        }
    }
}
