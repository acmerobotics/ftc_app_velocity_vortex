package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.file.DataFile;
import com.acmerobotics.velocityvortex.sensors.TCS34725ColorSensor;
import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.AdafruitI2cColorSensor;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;

/**
 * @author Ryan Brott
 */

@TeleOp(name="I2c Device Comparison Test")
public class I2cDeviceComparisonTest extends OpMode {

    private ColorSensor amsColor;
    private ColorSensor mrColor;
    private BNO055IMU imu;
    private DataFile file;

    @Override
    public void init() {
        mrColor = hardwareMap.colorSensor.get("mrColor");
        mrColor.setI2cAddress(I2cAddr.create8bit(0x3E));

        amsColor = new AdafruitI2cColorSensor(hardwareMap.deviceInterfaceModule.get("dim"), 0);

//        amsColor = new TCS34725ColorSensor(hardwareMap.i2cDeviceSynch.get("amsColor"), true);
//        amsColor.setGain(TCS34725ColorSensor.Gain.GAIN_1X);
//        amsColor.setIntegrationTime(TCS34725ColorSensor.IntegrationTime.INTEGRATION_TIME_2_4MS);
//        amsColor.initialize();

        imu = new AdafruitBNO055IMU(hardwareMap.i2cDeviceSynch.get("imu"));
        BNO055IMU.Parameters params = new BNO055IMU.Parameters();
        params.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        imu.initialize(params);

        file = new DataFile("i2c_device_comp_" + System.currentTimeMillis() + ".csv");
        file.write("time,MR,AMS,IMU");
    }

    @Override
    public void loop() {
        double mrRed = mrColor.red();
        double amsRed = amsColor.red();
        double imuHeading = -imu.getAngularOrientation().firstAngle;

        file.write(String.format("%f,%f,%f,%f", getRuntime(), mrRed, amsRed, imuHeading));
    }

    @Override
    public void stop() {
        file.close();
    }
}
