package com.acmerobotics.velocityvortex.sensors;

import com.qualcomm.robotcore.hardware.ColorSensor;

import java.util.Locale;

/**
 * @author kelly
 */

public class ColorAnalyzer {

    public double blueThreshold = 2.5;
    public double redThreshold = .9;

    private double red;
    private double blue;

    private ColorSensor device;

    public ColorAnalyzer (ColorSensor sensor) {
        device = sensor;
    }

    @Override
    public String toString() {
        return String.format(Locale.CANADA, "%s, %f, %f", read().getName(), red, blue);
    }

    public BeaconColor read () {
        red = device.red();
        blue = device.blue();
        double ratio = blue / red;

        BeaconColor color = BeaconColor.UNKNOWN;
        if (ratio > blueThreshold) color = BeaconColor.BLUE;
        else if (ratio < redThreshold) color = BeaconColor.RED;

        return color;
    }

    public double getRed() {
        return red;
    }

    public double getBlue() {
        return blue;
    }

    public enum BeaconColor {
        RED {
            public String getName () {return "red";}
        },
        BLUE {
            public String getName () {return "blue";}
        },
        UNKNOWN {
            public String getName () {return "unknown";}
        };

        public abstract String getName ();

    }

}
