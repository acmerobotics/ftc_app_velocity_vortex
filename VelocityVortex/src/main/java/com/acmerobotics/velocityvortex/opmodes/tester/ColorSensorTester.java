package com.acmerobotics.velocityvortex.opmodes.tester;

import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.acmerobotics.velocityvortex.opmodes.Tester;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ColorSensorTester extends Tester<ColorSensor> {
    public ColorSensorTester(String name, ColorSensor device) {
        super(name, device);
    }

    @Override
    public String getType() {
        return "Color Sensor";
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public void loop(Gamepad gamepad, StickyGamepad stickyGamepad, Telemetry telemetry) {
        telemetry.addData("alpha", device.alpha());
        telemetry.addData("red", device.red());
        telemetry.addData("green", device.green());
        telemetry.addData("blue", device.blue());
    }
}
