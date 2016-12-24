package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.sensors.TCS34725ColorSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

@TeleOp(name="ColorSensorTest")
public class ColorSensorTest extends OpMode {

    TCS34725ColorSensor colorSensor;
//    AMSColorSensor colorSensor;

    @Override
    public void init() {
//        ModernRoboticsUsbDeviceInterfaceModule dim = hardwareMap.get(ModernRoboticsUsbDeviceInterfaceModule.class, "dim");
//        dim.setLED(0, true);

        I2cDeviceSynch device = hardwareMap.i2cDeviceSynch.get("colorSensor");
        colorSensor = new TCS34725ColorSensor(device, true);
        colorSensor.initialize();
//        colorSensor = AMSColorSensorImpl.create(AMSColorSensor.Parameters.createForAdaFruit(), device, true);
    }

    @Override
    public void loop() {
        telemetry.addData("red", colorSensor.red());
        telemetry.addData("green", colorSensor.green());
        telemetry.addData("blue", colorSensor.blue());
    }
}
