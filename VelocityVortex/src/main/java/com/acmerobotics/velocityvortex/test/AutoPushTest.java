package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan
 */

@Autonomous(name="Auto Push Test")
public class AutoPushTest extends LinearOpMode {

    private  DistanceSensor sensor;
    private BeaconPusher pusher;

    @Override
    public void runOpMode() throws InterruptedException {
        sensor = new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM);
        pusher = new BeaconPusher(hardwareMap, sensor);

        waitForStart();

        pusher.push(this);

        sleep(3000);
    }
}
