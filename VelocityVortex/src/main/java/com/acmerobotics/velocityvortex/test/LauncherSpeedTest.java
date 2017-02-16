package com.acmerobotics.velocityvortex.test;

import android.util.Log;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.mech.FixedLauncher;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * @author Ryan
 */

@Autonomous(name="Launcher Speed Test")
public class LauncherSpeedTest extends LinearOpMode {

    private DataFile log;
    private FixedLauncher launcher;
    private long lastUpdateTime;
    private int lastLeftPos, lastRightPos;
    private double rightSpeed, leftSpeed;

    @Override
    public void runOpMode() throws InterruptedException {
        launcher = new FixedLauncher(hardwareMap);

        log = new DataFile("LauncherTest_" + System.currentTimeMillis() + ".csv");
        log.write("time,leftPos,rightPos,leftSpeed,rightSpeed");

        ElapsedTime timer = new ElapsedTime();

        waitForStart();

        while (opModeIsActive()) {
            timer.reset();
            launcher.reset();
            lastUpdateTime = 0;

            launcher.setPower(1, 1, 2000);
            while (opModeIsActive() && timer.milliseconds() < 5000) {
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
        long updateTime = System.currentTimeMillis();
        int leftPos = launcher.getLeftPosition();
        int rightPos = launcher.getRightPosition();
        if (lastUpdateTime == 0) {
            lastUpdateTime = updateTime;
            lastLeftPos = leftPos;
            lastRightPos = rightPos;
            rightSpeed = 0;
            leftSpeed = 0;
        } else if (lastLeftPos != leftPos || lastRightPos != rightPos) {
            double dt = updateTime - lastUpdateTime;
            rightSpeed = (leftPos - lastLeftPos) / dt;
            leftSpeed = (rightPos - lastRightPos) / dt;

            lastUpdateTime = updateTime;
            lastLeftPos = leftPos;
            lastRightPos = rightPos;
        }
        log.write(String.format("%d,%d,%d,%f,%f", updateTime, leftPos, rightPos, leftSpeed, rightSpeed));
    }
}
