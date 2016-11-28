package com.acmerobotics.velocityvortex.mech;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class BeaconPusher {

    public static final double LEFT_DOWN = 0.44;
    public static final double RIGHT_DOWN = 0.41;

    public static final double LEFT_UP = 0;
    public static final double RIGHT_UP = 1;

    private boolean leftUp, rightUp;
    private Servo leftServo, rightServo;

    public BeaconPusher(HardwareMap hardwareMap) {
        leftServo = hardwareMap.servo.get("leftPusher");
        rightServo = hardwareMap.servo.get("rightPusher");

        leftDown();
        rightDown();
    }

    public void leftUp() {
        leftUp = true;
        leftServo.setPosition(LEFT_UP);
    }

    public void leftDown() {
        leftUp = false;
        leftServo.setPosition(LEFT_DOWN);
    }

    public void rightUp() {
        rightUp = true;
        rightServo.setPosition(RIGHT_UP);
    }

    public void rightDown() {
        rightUp = false;
        rightServo.setPosition(RIGHT_DOWN);
    }

    public void leftToggle() {
        if (leftUp) {
            leftDown();
        } else {
            leftUp();
        }
    }

    public void rightToggle() {
        if (rightUp) {
            rightDown();
        } else {
            rightUp();
        }
    }

}
