package com.acmerobotics.velocityvortex.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Created by kelly on 12/7/2016.
 */

@TeleOp(name="gamepadTest")
public class ControllerTest extends OpMode {

    public void init() {

    }

    public void loop () {
        telemetry.addData("Right trigger", gamepad1.right_trigger);
    }

}
