package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.sensors.LinearPot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Created by kelly on 2/6/2017.
 */

@Autonomous(name="linearPotTest")
public class LinearPotTest extends OpMode {

    DistanceSensor sensor;

    @Override public void init () {
        sensor = new LinearPot(hardwareMap.analogInput.get("lp"), 500, DistanceUnit.MM);
    }

    @Override public void loop () {
        telemetry.addData ("distance", sensor.getDistance(DistanceUnit.INCH));
    }
}
