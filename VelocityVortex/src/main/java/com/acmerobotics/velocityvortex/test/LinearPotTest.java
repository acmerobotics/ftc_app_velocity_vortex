package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.mech.BeaconPusher;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Created by kelly on 2/6/2017.
 */

@Autonomous(name="Linear Pot Test", group="Test")
public class LinearPotTest extends OpMode {

    DistanceSensor sensor;
    BeaconPusher pusher;
    StickyGamepad stickyGamepad1;

    @Override public void init () {
        sensor = new LinearPot(hardwareMap.analogInput.get("lp"), 200, DistanceUnit.MM);
        pusher = new BeaconPusher(hardwareMap, sensor);
        stickyGamepad1 = new StickyGamepad(gamepad1);
    }

    @Override public void loop () {
        stickyGamepad1.update();
        telemetry.addData ("distance", pusher.getCurrentPosition());
    }
}
