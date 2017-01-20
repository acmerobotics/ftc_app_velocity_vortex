package com.acmerobotics.velocityvortex.test;

import android.graphics.Path;

import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Created by kelly on 1/19/2017.
 */

@Autonomous(name="Auto Fire Test")
public class AutoFireTest extends LinearOpMode {

    private FixedLauncher launcher;

    @Override
    public void runOpMode() throws InterruptedException {
        launcher = new FixedLauncher(hardwareMap);

        waitForStart();

        launcher.fireBalls(3);
    }
}
