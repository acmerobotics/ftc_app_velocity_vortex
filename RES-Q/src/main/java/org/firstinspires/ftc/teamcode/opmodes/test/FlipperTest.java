package org.firstinspires.ftc.teamcode.opmodes.test;


import org.firstinspires.ftc.teamcode.control.LinearRobotController;
import org.firstinspires.ftc.teamcode.hardware.mechanisms.FlipperHardware;

/**
 * Created by Admin on 1/19/2016.
 */
public class FlipperTest extends LinearRobotController {

    private FlipperHardware flipperHardware;

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        flipperHardware = new FlipperHardware();
        registerHardwareInterface("flipper", flipperHardware);

        waitForStart();

        flipperHardware.dump();
    }
}
