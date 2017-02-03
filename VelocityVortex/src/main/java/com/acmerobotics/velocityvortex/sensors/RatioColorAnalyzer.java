package com.acmerobotics.velocityvortex.sensors;

import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * @author kelly
 */

public class RatioColorAnalyzer implements ColorAnalyzer {

    private double blueThreshold, redThreshold;

    private ColorSensor sensor;

    public RatioColorAnalyzer(ColorSensor device, double blueThreshold, double redThreshold) {
        sensor = device;
        this.blueThreshold = blueThreshold;
        this.redThreshold = redThreshold;
    }

    @Override
    public ColorAnalyzer.BeaconColor getBeaconColor() {
        double red = sensor.red();
        double blue = sensor.blue();
        double blueRedRatio = blue / red;
        if (blueRedRatio >= blueThreshold) {
            return BeaconColor.BLUE;
        } else if (blueRedRatio <= redThreshold) {
            return BeaconColor.RED;
        } else {
            return BeaconColor.UNKNOWN;
        }
    }
}
