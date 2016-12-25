package com.acmerobotics.velocityvortex.sensors;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class MaxSonarEZ3UltrasonicSensor implements UltrasonicSensor, DistanceSensor {

    public static final double VOLTS_PER_INCH = 0.0098;

    AnalogInput input;

    public MaxSonarEZ3UltrasonicSensor(AnalogInput analogInput) {
        input = analogInput;
    }

    @Override
    public double getDistance(DistanceUnit unit) {
        return unit.fromInches(getUltrasonicLevel());
    }

    @Override
    public double getUltrasonicLevel() {
        return input.getVoltage() / VOLTS_PER_INCH;
    }

    @Override
    public String status() {
        return "";
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "MaxSonar EZ3 Ultrasonic Sensor";
    }

    @Override
    public String getConnectionInfo() {
        return input.getConnectionInfo();
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void resetDeviceConfigurationForOpMode() {

    }

    @Override
    public void close() {

    }
}
