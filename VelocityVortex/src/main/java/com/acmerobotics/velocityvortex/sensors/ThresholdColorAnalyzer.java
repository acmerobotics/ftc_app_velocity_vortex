package com.acmerobotics.velocityvortex.sensors;

import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * @author Ryan Brott
 */

public class ThresholdColorAnalyzer implements ColorAnalyzer {

    private ColorSensor sensor;

    private double blueThreshold, redThreshold;

    public ThresholdColorAnalyzer(ColorSensor sensor, double blueThreshold, double redThreshold) {
        this.sensor = sensor;
        this.blueThreshold = blueThreshold;
        this.redThreshold = redThreshold;
    }

    @Override
    public BeaconColor getBeaconColor() {
        double red = sensor.red();
        double blue = sensor.blue();
        if (red >= redThreshold) {
            return BeaconColor.RED;
        } else if (blue >= blueThreshold) {
            return BeaconColor.BLUE;
        } else {
            return BeaconColor.UNKNOWN;
        }
    }
}
