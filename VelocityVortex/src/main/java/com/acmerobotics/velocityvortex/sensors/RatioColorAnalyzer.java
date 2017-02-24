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
        double red = red();
        double blue = blue();
        double blueRedRatio = blue / red;
        if (blueRedRatio >= blueThreshold) {
            return BeaconColor.BLUE;
        } else if (blueRedRatio <= redThreshold) {
            return BeaconColor.RED;
        } else {
            return BeaconColor.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return String.format("RatioAnalyzer[red=%f,blue=%f]", redThreshold, blueThreshold);
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