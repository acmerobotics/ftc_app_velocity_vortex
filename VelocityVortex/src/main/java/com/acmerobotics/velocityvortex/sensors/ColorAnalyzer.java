package com.acmerobotics.velocityvortex.sensors;

import java.util.Locale;

/**
 * @author kelly
 */

public class ColorAnalyzer {

    public double blueThreshold = 3;
    public double redThreshold = .85;

    private double red;
    private double green;
    private double blue;
    private double alpha;

    private TCS34725ColorSensor device;

    public ColorAnalyzer (TCS34725ColorSensor sensor) {
        device = sensor;
        device.initialize();
    }

    @Override
    public String toString() {
        return String.format(Locale.CANADA, "%s, %f, %f, %f, %f", read().getName(), red, green, blue, alpha);
    }

    public BeaconColor read () {
        red = device.red();
        blue = device.blue();
        alpha = device.alpha();
        green = device.green();
        double ratio = blue / red;

        BeaconColor color = BeaconColor.UNKNOWN;
        if (ratio > blueThreshold) color = BeaconColor.BLUE;
        else if (ratio < redThreshold) color = BeaconColor.RED;

        return color;
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
