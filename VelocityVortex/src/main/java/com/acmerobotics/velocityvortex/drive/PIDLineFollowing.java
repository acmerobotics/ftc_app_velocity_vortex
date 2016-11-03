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

    private PIDConstants pid;
    private int sum, lastError;
    private double lastTime;
    private SX1509LineFollowingArray lineFollowingArray;
    private Drive drive;

    @Override
    public void init() {
        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("lineArray");
        lineFollowingArray = new SX1509LineFollowingArray(i2cDevice);

        pid = new PIDConstants();
        // set the pid constants to actual values
        // YOUR CODE HERE
        pid.p = 0.025;
        pid.i = 1;

        drive = new Drive(hardwareMap);
    }

    @Override
    public void loop() {
        // get the latest data and calculate the position error
        lineFollowingArray.scan();
        int error = lineFollowingArray.getPosition();

        // variable to hold the final update amount
        double update = 0;

        // do the PID update
        double time = getRuntime();
        if (lastTime == 0) {
            // special handling for first iteration
            sum = 0;
        } else {
            double dt = time - lastTime;
            // sum computed using trapezoidal rule
            sum += (error + lastError) * dt / 2.0;
            double deriv = (error - lastError) / dt;
            update = pid.p * error + pid.i * sum + pid.d * deriv;
        }
        telemetry.addData("error", error);
        telemetry.addData("update", update);

        // use the update value (e.g. set the driver motors)
        // YOUR CODE HERE
        double baseSpeed = -0.25;
        update = -update;
        drive.setMotorPowers(baseSpeed + update, baseSpeed - update);
        drive.updateMotors();

        lastTime = time;
        lastError = error;
    }

    /**
     * This class contains the three constants necessary for the PID loop. Should
     * we use {@link com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients} instead?
     */
    public class PIDConstants {
        /** proportional (P) constant */
        public double p = 0.0;
        /** integral (I) constant */
        public double i = 0.0;
        /** derivative (D) constant */
        public double d = 0.0;
    }
}
