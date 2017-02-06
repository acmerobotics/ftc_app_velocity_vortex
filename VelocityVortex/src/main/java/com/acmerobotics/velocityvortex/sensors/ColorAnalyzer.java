package com.acmerobotics.velocityvortex.sensors;

/**
 * @author Ryan Brott
 */

public interface ColorAnalyzer {
    enum BeaconColor {
        RED,
        BLUE,
        UNKNOWN
    }

    BeaconColor getBeaconColor();
    int red();
    int blue();
}
