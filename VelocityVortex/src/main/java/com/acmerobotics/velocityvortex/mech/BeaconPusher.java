package com.acmerobotics.velocityvortex.mech;

import com.acmerobotics.library.configuration.RobotProperties;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class BeaconPusher {

    private double leftDown, leftUp, rightDown, rightUp;
    private boolean isLeftUp, isRightUp;
    private Servo leftServo, rightServo;

    public BeaconPusher(HardwareMap hardwareMap, RobotProperties properties) {
        leftServo = hardwareMap.servo.get("leftPusher");
        rightServo = hardwareMap.servo.get("rightPusher");

        leftDown = properties.getLeftPusherDown();
        leftUp = properties.getLeftPusherUp();
        rightDown = properties.getRightPusherDown();
        rightUp = properties.getRightPusherUp();

        leftDown();
        rightDown();
    }

    public void leftUp() {
        isLeftUp = true;
        leftServo.setPosition(leftUp);
    }

    public void leftDown() {
        isLeftUp = false;
        leftServo.setPosition(leftDown);
    }

    public void rightUp() {
        isRightUp = true;
        rightServo.setPosition(rightUp);
    }

    public void rightDown() {
        isRightUp = false;
        rightServo.setPosition(rightDown);
    }

    public void leftToggle() {
        if (isLeftUp) {
            leftDown();
        } else {
            leftUp();
        }
    }

    public void rightToggle() {
        if (isRightUp) {
            rightDown();
        } else {
            rightUp();
        }
    }

}
