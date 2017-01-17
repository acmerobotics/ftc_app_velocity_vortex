package com.acmerobotics.velocityvortex.sensors;

/**
 * @author kelly
 */

public class ColorAnalyzer {

    public double blueThreshold = 2.5;
    public double redThreshold = .85;

    private TCS34725ColorSensor device;

    public ColorAnalyzer (TCS34725ColorSensor sensor) {
        device = sensor;
        device.initialize();
    }

    public BeaconColor read () {
        double red = device.red();
        double blue = device.blue();
        double ratio = blue / red;

        BeaconColor color = BeaconColor.UNKNOWN;
        if (ratio > blueThreshold) color = BeaconColor.BLUE;
        else if (ratio < redThreshold) color = BeaconColor.RED;

        return color;
    }

    enum BeaconColor {
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
