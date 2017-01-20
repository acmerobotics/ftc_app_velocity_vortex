package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Created by kelly on 1/19/2017.
 */

@TeleOp(name="beaconPushTest")
public class BeaconPushTest extends OpMode {

    private BeaconPusher pusher;

    public void init () {
        pusher = new BeaconPusher(hardwareMap);
    }

    public void loop () {
        if (gamepad1.right_bumper) pusher.extend();
        else pusher.retract();
    }

}
