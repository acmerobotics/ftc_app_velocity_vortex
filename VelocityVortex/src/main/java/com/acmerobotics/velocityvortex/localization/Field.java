package com.acmerobotics.velocityvortex.localization;

/**
 * Contains data about the size and position of objects on the field, as well as the state of the field
 * All lengths in mm
 * Created by kelly on 10/24/2016.
 */

public class Field {
    public static final double length = 10; //mm todo find the actual length

    public static final RobotLocation gearsLocation = RobotLocation.ORIGIN;
    public static final RobotLocation toolsLocation = RobotLocation.ORIGIN;
    public static final RobotLocation legoesLocation = RobotLocation.ORIGIN;
    public static final RobotLocation wheelsLocation = RobotLocation.ORIGIN;

    public Beacon[] beacons = {new Beacon(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN, new RobotLocation(0,0,0)),
                        new Beacon(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN, new RobotLocation(0,0,0)),
                        new Beacon(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN, new RobotLocation(0,0,0)),
                        new Beacon(BeaconColor.UNKNOWN, BeaconColor.UNKNOWN, new RobotLocation(0,0,0))};

    //todo add in the actual locations of things that we will want to know the location of

    public class Beacon {
        public BeaconColor[] colors = {BeaconColor.UNKNOWN, BeaconColor.UNKNOWN};
        public RobotLocation location;

        public Beacon (BeaconColor c1, BeaconColor c2, RobotLocation location) {
            this.colors[0] = c1;
            this.colors[1] = c2;
            this.location = location;
        }
    }

    public enum BeaconColor {
        RED {
            BeaconColor change() { return BLUE; }
        },
        BLUE {
            BeaconColor change() { return BLUE; }
        },
        UNKNOWN {
            BeaconColor change() {return UNKNOWN; }
        }
    }
}