package com.acmerobotics.velocityvortex.test;

import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.velocityvortex.vision.BeaconOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Disabled
@Autonomous(name = "Beacon Test")
public class BeaconTest extends BeaconOpMode {
    @Override
    public void init() {
        camera.initSync();
        camera.start();
    }

    @Override
    public void loop() {
        synchronized (beacons) {
            for (int i = 0; i < 5; i++) {
                if (i < beacons.size()) {
                    telemetry.addData(Integer.toString(i), beacons.get(i).getScore().getNumericScore() + ": " + beacons.get(i).getScore().toString() + " " + (beacons.get(i).getLeftRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B") + "," + (beacons.get(i).getRightRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B"));
                } else {
                    telemetry.addData(Integer.toString(i), "");
                }
            }
        }
    }
}
