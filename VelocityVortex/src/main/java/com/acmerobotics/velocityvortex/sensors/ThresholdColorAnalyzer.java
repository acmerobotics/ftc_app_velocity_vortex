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
        double red = red();
        double blue = blue();
        if (red >= redThreshold) {
            return BeaconColor.RED;
        } else if (blue >= blueThreshold) {
            return BeaconColor.BLUE;
        } else {
            return BeaconColor.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return String.format("ThresholdAnalyzer[red=%f,blue=%f]", redThreshold, blueThreshold);
    }

    @Override
    public int red() {
        return sensor.red();
    }

    @Override
    public int blue() {
        return sensor.blue();
    }
}
