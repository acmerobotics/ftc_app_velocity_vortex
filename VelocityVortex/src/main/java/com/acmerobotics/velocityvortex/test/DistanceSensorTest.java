package com.acmerobotics.velocityvortex.test;

import com.qualcomm.hardware.hitechnic.HiTechnicNxtUltrasonicSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Distance Sensor Test")
public class DistanceSensorTest extends OpMode {

    private HiTechnicNxtUltrasonicSensor sensor;

    @Override
    public void init() {
        sensor = hardwareMap.get(HiTechnicNxtUltrasonicSensor.class, "distanceSensor");
    }

    @Override
    public void loop() {
        telemetry.addData("distance", sensor.getUltrasonicLevel());
    }
}
