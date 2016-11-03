package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.velocityvortex.i2c.SX1509LineFollowingArray;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

/**
 * This class contains a concept line following array opmode with
 * a basic PID loop suitable for line following.
 */
@Autonomous(name = "PID Line Following")
public class PIDLineFollowing extends OpMode {

    private PIDController controller;
    private SX1509LineFollowingArray lineFollowingArray;
    private Drive drive;

    @Override
    public void init() {
        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("lineArray");
        lineFollowingArray = new SX1509LineFollowingArray(i2cDevice);

        controller = new PIDController(new PIDController.PIDCoefficients(0.025, 1, 0));

        drive = new Drive(hardwareMap);
    }

    @Override
    public void loop() {
        // get the latest data and calculate the position error
        lineFollowingArray.scan();
        int error = lineFollowingArray.getPosition();

        double update = controller.loop(error);

        telemetry.addData("error", error);
        telemetry.addData("update", update);

        // use the update value (e.g. set the driver motors)
        double baseSpeed = -0.25;
        update = -update;
        drive.setMotorPowers(baseSpeed + update, baseSpeed - update);
        drive.updateMotors();
    }
}
