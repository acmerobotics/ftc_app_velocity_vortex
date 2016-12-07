package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.i2c.SparkFunLineFollowingArray;
import com.acmerobotics.velocityvortex.opmodes.DeadReckoningAuto;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="Horizontal Line Follow Test")
public class HorizontalLineFollowTest extends LinearOpMode {

    private SparkFunLineFollowingArray lineSensor;
    private MecanumDrive drive;

    @Override
    public void runOpMode() throws InterruptedException {
        lineSensor = new SparkFunLineFollowingArray(hardwareMap.i2cDeviceSynch.get("lineArray"));
//        lineSensor.getParameters().invertBits = true;

        drive = new MecanumDrive(hardwareMap);

        waitForStart();

        DeadReckoningAuto.followLine(lineSensor, drive, this);
    }
}
