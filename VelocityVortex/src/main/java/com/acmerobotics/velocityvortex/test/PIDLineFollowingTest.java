package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.DifferentialDrive;
import com.acmerobotics.velocityvortex.drive.PIDController;
import com.acmerobotics.library.file.CSVFile;
import com.acmerobotics.velocityvortex.sensors.SparkFunLineFollowingArray;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

import java.util.Date;

/**
 * This class contains a concept line following array opmode with
 * a basic PID update suitable for line following.
 */
@Disabled
@Autonomous(name = "PID Line Following")
public class PIDLineFollowingTest extends OpMode {

    private PIDController controller;
    private SparkFunLineFollowingArray lineFollowingArray;
    private DifferentialDrive drive;
    private CSVFile<PIDData> csvFile;
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

        csvFile = new CSVFile<PIDData>("pid. " + date + ".csv", PIDData.class);

        I2cDeviceSynch i2cDevice = hardwareMap.i2cDeviceSynch.get("lineArray");
        lineFollowingArray = new SparkFunLineFollowingArray(i2cDevice);
        lineFollowingArray.initialize();

        controller = new PIDController(new DifferentialControlLoopCoefficients(0.025, 0.005, 0));

        drive = new DifferentialDrive(hardwareMap);
    }

    @Override
    public void loop() {
        // get the latest data and calculate the position error
        lineFollowingArray.scan();
        int error = lineFollowingArray.getPosition();

        double update = controller.update(error);

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
