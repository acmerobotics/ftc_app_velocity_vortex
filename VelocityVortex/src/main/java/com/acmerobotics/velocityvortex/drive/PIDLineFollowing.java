package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.velocityvortex.file.CSVFile;
import com.acmerobotics.velocityvortex.i2c.SparkFunLineFollowingArray;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

import java.util.Date;

/**
 * This class contains a concept line following array opmode with
 * a basic PID loop suitable for line following.
 */
@Autonomous(name = "PID Line Following")
public class PIDLineFollowing extends OpMode {

    private PIDController controller;
    private SparkFunLineFollowingArray lineFollowingArray;
    private DifferentialDrive drive;
    private CSVFile csvFile;
    private PIDData data;

    public class PIDData {
        public long timestamp;
        public double error;
        public double update;
    }

    @Override
    public void init() {
        data = new PIDData();

        Date date = new Date();

        csvFile = new CSVFile("pid. " + date + ".csv", PIDData.class);

        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("lineArray");
        lineFollowingArray = new SparkFunLineFollowingArray(i2cDevice);

        controller = new PIDController(new PIDController.PIDCoefficients(0.025, 0.005, 0));

        drive = new DifferentialDrive(hardwareMap);
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

        data.error = error;
        data.update = update;
        data.timestamp = System.nanoTime();

        csvFile.write(data);
    }

    @Override
    public void stop() {
        csvFile.close();
    }
}
