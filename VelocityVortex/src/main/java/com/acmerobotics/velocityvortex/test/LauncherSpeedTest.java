package com.acmerobotics.velocityvortex.test;

import android.util.Log;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.acmerobotics.velocityvortex.sensors.AverageDifferentiator;
import com.acmerobotics.velocityvortex.sensors.ExponentialSmoother;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * @author Ryan
 */

@Disabled
@Autonomous(name="Launcher Speed Test", group="Test")
public class LauncherSpeedTest extends LinearOpMode {

    private DataFile log;
    private FixedLauncher launcher;
    private long lastUpdateTime;
    private double lastLeftPos, lastRightPos;
    private double rightSpeed, leftSpeed;

    private ElapsedTime timer2;

    @Override
    public void runOpMode() throws InterruptedException {
        launcher = new FixedLauncher(hardwareMap);

        log = new DataFile("LauncherTest_" + System.currentTimeMillis() + ".csv");
        log.write("time,leftPos,rightPos,leftSpeed,rightSpeed");

        ElapsedTime timer = new ElapsedTime();

        timer2 = new ElapsedTime();
        lastLeftPos = launcher.getLeftPosition();
        lastRightPos = launcher.getRightPosition();

        waitForStart();

        while (opModeIsActive()) {
            timer.reset();
            launcher.reset();
            lastUpdateTime = 0;

            launcher.setPower(1);
            while (opModeIsActive() && timer.milliseconds() < 7000) {
                launcher.update();
                logData();
            }
            launcher.setPower(0);
            while (opModeIsActive() && timer.milliseconds() < 10000) {
                logData();
            }
        }
    }

    private void logData() {
        double leftPos = launcher.getLeftPosition();
        double rightPos = launcher.getRightPosition();
        if (timer2.milliseconds() >= 100) {
            double dt = timer2.milliseconds();
            rightSpeed = (leftPos - lastLeftPos) / dt;
            leftSpeed = (rightPos - lastRightPos) / dt;
            lastLeftPos = leftPos;
            lastRightPos = rightPos;
            timer2.reset();
        }
        log.write(String.format("%d,%f,%f,%f,%f", System.currentTimeMillis(), leftPos, rightPos, leftSpeed, rightSpeed));
    }
}
