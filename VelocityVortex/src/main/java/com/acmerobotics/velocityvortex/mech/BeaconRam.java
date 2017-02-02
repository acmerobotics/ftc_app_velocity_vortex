package com.acmerobotics.velocityvortex.mech;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoImpl;

/**
 * @author Kelly Muir
 */

public class BeaconRam {

    public static final double IN = 0.37;
    public static final double OUT = 0.7;

    private Servo servo;
    private boolean extended;

    public BeaconRam(HardwareMap hardwareMap) {
        ServoController servoController = hardwareMap.servoController.get("servo");
        servo = new ServoImpl(servoController, 3);
        servo.setPosition(IN);
        extended = false;
    }

    public void extend() {
        if (!extended) {
            servo.setPosition(OUT);
            extended = true;
        }
    }

    public void retract() {
        if (extended) {
            servo.setPosition(IN);
            extended = false;
        }
    }

    public void toggle () {
        setExtended (!extended);
    }

    public void setExtended (boolean isExtended) {
        if (isExtended) extend();
        else retract();
    }

    public boolean isExtended() {
        return extended;
    }

}
