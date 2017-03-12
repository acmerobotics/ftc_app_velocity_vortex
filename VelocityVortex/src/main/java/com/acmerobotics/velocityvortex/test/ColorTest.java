package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.opmodes.Auto;
import com.acmerobotics.velocityvortex.sensors.ThresholdColorAnalyzer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * Created by ACME Robotics on 3/11/2017.
 */

@TeleOp(name="Color Test")
public class ColorTest extends OpMode {

    private ThresholdColorAnalyzer analyzer;
    private ColorSensor colorSensor;

    @Override
    public void init() {
        colorSensor = hardwareMap.colorSensor.get("color");
        analyzer = new ThresholdColorAnalyzer(colorSensor, Auto.BLUE_THRESHOLD, Auto.RED_THRESHOLD);
    }

    @Override
    public void loop() {
        telemetry.addData("beacon_color", analyzer.getBeaconColor());
        telemetry.addData("red", colorSensor.red());
        telemetry.addData("blue", colorSensor.blue());
    }
}
