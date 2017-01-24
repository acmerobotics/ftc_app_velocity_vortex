package com.acmerobotics.velocityvortex.sensors;

import java.util.Locale;

/**
 * @author kelly
 */

public class ColorAnalyzer {

    public double blueThreshold = 2.25;
    public double redThreshold = .75;

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
        int[] colors = device.getColors();
        alpha = colors[0];
        red = colors[1];
        green = colors[2];
        blue = colors[3];
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

    public double getRatio () {return blue/red; }

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
