package com.acmerobotics.velocityvortex.i2c;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

/**
 * This class tests the functionality of the line following array
 */
@Autonomous(name = "Line Following Array Test", group = "test")
public class LineFollowingArrayTest extends OpMode {

    SX1509LineFollowingArray lineFollowerArray;

    @Override
    public void init() {
        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("lineArray");
        lineFollowerArray = new SX1509LineFollowingArray(i2cDevice);
    }

    @Override
    public void loop() {
        lineFollowerArray.scan();

        telemetry.addData("raw", Integer.toBinaryString(lineFollowerArray.getRaw()));
        telemetry.addData("density", lineFollowerArray.getDensity());
        telemetry.addData("position", lineFollowerArray.getPosition());
    }
}
