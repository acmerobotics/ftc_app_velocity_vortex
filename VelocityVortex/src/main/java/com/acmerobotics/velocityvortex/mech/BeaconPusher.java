package com.acmerobotics.velocityvortex.mech;

import android.os.SystemClock;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbServoController;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImpl;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

import org.firstinspires.ftc.robotcontroller.external.samples.HardwareK9bot;

/**
 * @author Ryan Brott
 */

public class BeaconPusher {

    private boolean extended;
    private CRServo servo;

    public BeaconPusher(HardwareMap hardwareMap) {
        ServoController servoController = hardwareMap.servoController.get("servo");
        servo = new CRServoImpl(servoController, 2);
        servo.setPower(-1);
        extended = false;
    }

    public void extend() {
        if (!extended) {
            servo.setPower(1);
            extended = true;
        }
    }

    public void retract() {
        if (extended) {
            servo.setPower(-1);
            extended = false;
        }
    }

    public void autoPush() {
        extend();
        SystemClock.sleep(1500);
        retract();
        SystemClock.sleep(800);
    }

    public boolean isExtended() {
        return extended;
    }

}
