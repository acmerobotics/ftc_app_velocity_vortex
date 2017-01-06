package com.acmerobotics.velocityvortex.opmodes.tester;

import com.acmerobotics.library.logging.Logger;
import com.acmerobotics.velocityvortex.opmodes.StickyGamepad;
import com.acmerobotics.velocityvortex.opmodes.Tester;
import com.acmerobotics.velocityvortex.sensors.TCS34725ColorSensor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TCS34725ColorSensorTester extends Tester<I2cDeviceSynch> {

    private TCS34725ColorSensor colorSensor;

    public TCS34725ColorSensorTester(String name, I2cDeviceSynch device) {
        super(name, device);
        colorSensor = new TCS34725ColorSensor(device, true);
        colorSensor.initialize();
    }

    @Override
    public String getType() {
        return "AMS TCS34725 Color Sensor";
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public void loop(Gamepad gamepad, StickyGamepad stickyGamepad, Telemetry telemetry, Logger logger) {
        telemetry.addData("alpha", colorSensor.alpha());
        telemetry.addData("red", colorSensor.red());
        telemetry.addData("green", colorSensor.green());
        telemetry.addData("blue", colorSensor.blue());
    }
}
