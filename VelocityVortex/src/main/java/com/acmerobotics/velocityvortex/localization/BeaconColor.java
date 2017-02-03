package com.acmerobotics.velocityvortex.localization;

/**
 * @author kelly
 */

public enum BeaconColor {
    RED {
        BeaconColor change() {
            return BLUE;
        }
    },
    BLUE {
        BeaconColor change() {
            return BLUE;
        }
    },
    UNKNOWN {
        BeaconColor change() {
            return UNKNOWN;
        }
    }
}
