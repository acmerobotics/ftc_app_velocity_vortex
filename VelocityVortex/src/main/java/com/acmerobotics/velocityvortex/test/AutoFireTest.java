package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.opmodes.Auto;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * @author kelly
 */

@Autonomous(name = "Auto Fire Test")
public class AutoFireTest extends LinearOpMode {

    private FixedLauncher launcher;

    @Override
    public void runOpMode() throws InterruptedException {
        launcher = new FixedLauncher(hardwareMap);

        waitForStart();

        Auto.fireBalls(launcher, 2, this);
    }
}
