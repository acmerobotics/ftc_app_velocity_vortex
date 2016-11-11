package com.acmerobotics.velocityvortex.opmodes;

import com.acmerobotics.velocityvortex.drive.MecanumDrive;
import com.acmerobotics.velocityvortex.localization.VuforiaInterface;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name="Beacon Auto")
public class BeaconAuto extends OpMode {

    VuforiaInterface vuforia;
    MecanumDrive mecanumDrive;

    @Override
    public void init() {
        vuforia = new VuforiaInterface("vuforia", 0);

        mecanumDrive = new MecanumDrive(hardwareMap);
    }

    @Override
    public void loop() {

    }
}
