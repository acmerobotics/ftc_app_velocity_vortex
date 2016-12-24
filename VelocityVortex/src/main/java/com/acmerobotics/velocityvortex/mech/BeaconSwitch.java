package com.acmerobotics.velocityvortex.mech;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by kelly on 12/10/2016.
 *
 */

public class BeaconSwitch {

    private Servo servo;

    private double rightPos = .14, leftPos = .8, storePos = 0;

    public BeaconSwitch(HardwareMap hardwareMap) {
        servo = hardwareMap.servo.get("beacon");
    }

    public void right() {
        servo.setPosition(rightPos);
    }

    public void left () {
        servo.setPosition(leftPos);
    }

    public void store () {
        servo.setPosition (storePos);
    }

}
