package com.acmerobotics.velocityvortex.sensors;

import com.acmerobotics.library.configuration.WheelType;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * @author Ryan
 */

public class EncoderDistanceSensor implements DistanceSensor {

    private DcMotor motor;
    private WheelType wheelType;
    private int initialPosition;

    public EncoderDistanceSensor(DcMotor motor, WheelType wheelType) {
        this.motor = motor;
        this.wheelType = wheelType;
        initialPosition = motor.getCurrentPosition();
    }

    @Override
    public double getDistance(DistanceUnit unit) {
        int counts = motor.getCurrentPosition() - initialPosition;
        return unit.fromInches(wheelType.getDistance(counts));
    }

    @Override
    public Manufacturer getManufacturer() {
        return motor.getManufacturer();
    }

    @Override
    public String getDeviceName() {
        return motor.getDeviceName();
    }

    @Override
    public String getConnectionInfo() {
        return motor.getConnectionInfo();
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
