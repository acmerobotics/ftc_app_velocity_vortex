package com.acmerobotics.velocityvortex.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;

@TeleOp(name="ColorSensorTest")
public class ColorSensorTest extends OpMode {

    ColorSensor colorSensor;

    @Override
    public void init() {
//        ModernRoboticsUsbDeviceInterfaceModule dim = hardwareMap.get(ModernRoboticsUsbDeviceInterfaceModule.class, "dim");
//        dim.setLED(0, true);
        colorSensor = hardwareMap.colorSensor.get("colorSensor");
    }

    @Override
    public void loop() {
        telemetry.addData("red", colorSensor.red());
        telemetry.addData("green", colorSensor.green());
        telemetry.addData("blue", colorSensor.blue());
    }
}
