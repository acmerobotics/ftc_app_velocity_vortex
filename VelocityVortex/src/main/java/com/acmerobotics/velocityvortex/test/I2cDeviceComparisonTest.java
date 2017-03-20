package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.sensors.TCS34725ColorSensor;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.AdafruitI2cColorSensor;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;

/**
 * @author Ryan Brott
 */

@TeleOp(name = "I2c Device Comparison Test", group="Test")
public class I2cDeviceComparisonTest extends OpMode {

    private TCS34725ColorSensor amsColor;
    private DataFile file;

    @Override
    public void init() {
//        amsColor = new AdafruitI2cColorSensor(hardwareMap.deviceInterfaceModule.get("dim"), 0);

        amsColor = new TCS34725ColorSensor(hardwareMap.i2cDeviceSynch.get("amsColor"), true);
        amsColor.setGain(TCS34725ColorSensor.Gain.GAIN_1X);
        amsColor.setIntegrationTime(TCS34725ColorSensor.IntegrationTime.INTEGRATION_TIME_50MS);
        amsColor.initialize();

//        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
//        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
//        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
//        imu.initialize(params);

        file = new DataFile("I2cDeviceComparison_" + System.currentTimeMillis() + ".csv");
        file.write("AMS settings," + amsColor.getGain() + "," + amsColor.getIntegrationTime());
        file.write("AMS actual settings,ATIME=" + amsColor.read8(TCS34725ColorSensor.Registers.TCS34725_ATIME) + ",CONTROL=" + amsColor.read8(TCS34725ColorSensor.Registers.TCS34725_CONTROL));
        file.write("time,AMS");
    }

    @Override
    public void loop() {
        double amsRed = amsColor.red();

        file.write(String.format("%d,%f", System.currentTimeMillis(), amsRed));
    }

    @Override
    public void stop() {
        file.close();
        amsColor.close();
    }
}
