package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

/**
 * Created by kelly on 12/23/2016.
 *
 */

@TeleOp(name="CRServoTester")
public class CRServoTester extends OpMode{

    private Servo servo;
    private double scaling = 1;
    private double increment = .1;
    private StickyGamepad gamepad;

    private void scalingUp () {
        scaling = Range.clip((scaling + increment), .01, 1);
    }

    private void scalingDown () {
        scaling = Range.clip((scaling - increment), .01, 1);
    }

    private double scale (double i) {
        double d = i - .5;
        d *= scaling;
        return d + .5;
    }

    public void init () {
        servo = hardwareMap.servo.get("pusher");
        gamepad = new StickyGamepad(gamepad1);
    }

    public void loop () {
        gamepad.update();
        if(gamepad.dpad_down) scalingDown();
        if(gamepad.dpad_up) scalingUp();

        double position = scale(-gamepad1.left_stick_y);
        servo.setPosition(position);
        telemetry.addData("velocity", position);
            }

}
