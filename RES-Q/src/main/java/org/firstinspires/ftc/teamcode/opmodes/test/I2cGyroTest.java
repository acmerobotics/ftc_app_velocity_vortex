package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.hardware.GyroSensor;

import org.firstinspires.ftc.teamcode.control.RobotController;

/**
 * Created by Ryan on 11/29/2015.
 */
public class I2cGyroTest extends RobotController {

    private GyroSensor gyroSensor;

    @Override
    public void init() {
        super.init();

        gyroSensor = hardwareMap.gyroSensor.get("gyro");
    }

    @Override
    public void loop() {
        if (gamepad1.a) {
            gyroSensor.resetZAxisIntegrator();
        }

        telemetry.addData("reading", gyroSensor.getHeading());
    }

}
