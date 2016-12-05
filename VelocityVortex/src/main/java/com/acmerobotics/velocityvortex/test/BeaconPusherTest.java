package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;
import com.acmerobotics.velocityvortex.localization.VuforiaInterface;
import com.acmerobotics.velocityvortex.vision.VuforiaCamera;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@Disabled
@Autonomous(name="Beacon Pusher Test")
public class BeaconPusherTest extends LinearOpMode {

//    private Servo leftPusher, rightPusher;
    private VuforiaCamera vuforiaCamera;
    private OpModeConfiguration.AllianceColor allianceColor;

    @Override
    public void runOpMode() throws InterruptedException {
//        leftPusher = hardwareMap.servo.get("leftPusher");
//        rightPusher = hardwareMap.servo.get("rightPusher");

        VuforiaInterface vuforia = new VuforiaInterface("test", 0);
        vuforiaCamera = new VuforiaCamera(hardwareMap.appContext, vuforia.getLocalizer());
        vuforiaCamera.initSync();

        OpModeConfiguration opModeConfiguration = new OpModeConfiguration(hardwareMap.appContext);
        allianceColor = opModeConfiguration.getAllianceColor();

        waitForStart();

        Beacon b = searchForBeacon();

        while (opModeIsActive()) {
            telemetry.addData("beacon", b.toString());
            telemetry.update();
        }
    }

    public Beacon searchForBeacon() throws InterruptedException {
        ArrayList<Beacon> beacons = new ArrayList<>();
        while (opModeIsActive()) {
            Mat frame = vuforiaCamera.getLatestFrame();
            if (frame == null) {
                Thread.sleep(1);
                continue;
            }
            beacons.clear();
            BeaconAnalyzer.analyzeImage(frame, beacons);
            Collections.sort(beacons, new Comparator<Beacon>() {
                @Override
                public int compare(Beacon lhs, Beacon rhs) {
                    return Integer.compare(lhs.getScore().getNumericScore(), rhs.getScore().getNumericScore());
                }
            });
            for (Beacon beacon : beacons) {
                beacon.draw(frame);
                if (beacon.getLeftRegion().getColor() != beacon.getRightRegion().getColor()) {
                    return beacon;
                }
            }
            frame.release();
        }
        return null;
    }
}
