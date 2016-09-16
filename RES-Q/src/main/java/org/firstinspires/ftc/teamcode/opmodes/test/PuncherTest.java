package org.firstinspires.ftc.teamcode.opmodes.test;

import org.firstinspires.ftc.teamcode.control.LinearRobotController;
import org.firstinspires.ftc.teamcode.hardware.mechanisms.PuncherHardware;

/**
 * Created by Admin on 12/11/2015.
 */
public class PuncherTest extends LinearRobotController {
    private PuncherHardware puncherHardware;

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        puncherHardware = new PuncherHardware();
        registerHardwareInterface("punch", puncherHardware);

        waitForStart();

        puncherHardware.punchLeft();

        waitMillis(3000);

        puncherHardware.punchRight();

        waitMillis(3000);
    }
}
