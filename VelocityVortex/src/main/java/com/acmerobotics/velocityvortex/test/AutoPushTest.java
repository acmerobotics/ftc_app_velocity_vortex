package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan
 */

@Autonomous(name="Auto Push Test", group="Test")
public class AutoPushTest extends LinearOpMode {

    private DistanceSensor sensor;
    private BeaconPusher pusher;

    @Override
    public void runOpMode() throws InterruptedException {
        sensor = new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM);
        pusher = new BeaconPusher(hardwareMap, sensor);

        pusher.setLogFile(new DataFile("AutoPushTest_" + System.currentTimeMillis() + ".csv"));

        waitForStart();

        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.milliseconds() < 1000) {
            pusher.update();
            idle();
        }

        pusher.push(this);

        pusher.moveToPosition(0, 0.01, this);
    }
}
