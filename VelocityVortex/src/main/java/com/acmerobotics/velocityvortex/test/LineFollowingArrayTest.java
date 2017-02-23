package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.sensors.SparkFunLineFollowingArray;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

/**
 * This class tests the functionality of the line following array
 */
@Disabled
@Autonomous(name = "Line Following Array Test", group="Test")
public class LineFollowingArrayTest extends OpMode {

    SparkFunLineFollowingArray lineFollowerArray;

    @Override
    public void init() {
        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("lineArray");
        lineFollowerArray = new SparkFunLineFollowingArray(i2cDevice);
    }

    @Override
    public void loop() {
        lineFollowerArray.scan();

        telemetry.addData("raw", Integer.toBinaryString(lineFollowerArray.getRaw()));
        telemetry.addData("density", lineFollowerArray.getDensity());
        telemetry.addData("position", lineFollowerArray.getPosition());
    }
}
