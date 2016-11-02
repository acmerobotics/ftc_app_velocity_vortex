package com.acmerobotics.velocityvortex.localization;

/**
 * Created by kelly on 10/26/2016.
 */

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
