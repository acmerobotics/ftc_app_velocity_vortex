package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;

import org.firstinspires.ftc.teamcode.control.RobotController;

/**
 * Created by Admin on 2/18/2016.
 */
public class LightSensorTest extends RobotController {

    public ColorSensor line;

    @Override
    public void init() {
        super.init();

        line = hardwareMap.colorSensor.get("line");
        line.setI2cAddress(new I2cAddr(0x3e));
    }

    @Override
    public void loop() {
        telemetry.addData("Alpha", line.alpha());
    }
}
