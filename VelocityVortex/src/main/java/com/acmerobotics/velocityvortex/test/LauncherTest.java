package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.mech.Launcher;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Created by kelly on 12/6/2016.
 */

@TeleOp(name="Launcher")
public class LauncherTest extends OpMode {

    private Launcher launcher;
    private boolean upReset;
    private boolean downReset;



    public void init () {
        launcher = new Launcher (hardwareMap);
    }

    public void loop () {
        telemetry.addData("velocity", launcher.getVelocity());
        if (gamepad1.dpad_up) {
            if (upReset) {
                launcher.velocityUp();
                upReset = false;
            }
        }
        else upReset = true;

        if (gamepad1.dpad_down) {
            if (downReset) {
                launcher.velocityDown();
                downReset = false;
            }
        }
        else downReset = true;
    }

}
