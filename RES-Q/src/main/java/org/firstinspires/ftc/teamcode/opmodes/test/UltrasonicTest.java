package org.firstinspires.ftc.teamcode.opmodes.test;

import org.firstinspires.ftc.teamcode.control.LinearRobotController;
import org.firstinspires.ftc.teamcode.hardware.sensors.UltrasonicPairHardware;

/**
 * Created by Admin on 12/10/2015.
 */
public class UltrasonicTest extends LinearRobotController {

    private UltrasonicPairHardware us;

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        us = new UltrasonicPairHardware();
        registerHardwareInterface("us", us);

        waitForStart();

        while(true) {
            waitOneFullHardwareCycle();
        }
    }
}
