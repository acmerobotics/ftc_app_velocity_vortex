package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;

import org.firstinspires.ftc.teamcode.control.LinearRobotController;

public class MRColorLogger extends LinearRobotController {

    private ColorSensor colorSensor;

    @Override
    public void runOpMode() throws InterruptedException {
        super.runOpMode();

        colorSensor = hardwareMap.colorSensor.get("line");
        colorSensor.setI2cAddress(new I2cAddr(0x3e));
        colorSensor.enableLed(true);

        boolean collectingData = true;

        waitForStart();

        while (collectingData && opModeIsActive()) {
            telemetry.addData("Status", "Currently logging color sensor data. Press [x] to stop.");

            if (gamepad1.x) {
                collectingData = false;
            }

            waitOneFullHardwareCycle();
        }
    }
}
