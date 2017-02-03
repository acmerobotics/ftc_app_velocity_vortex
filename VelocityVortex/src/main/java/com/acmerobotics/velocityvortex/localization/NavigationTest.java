package com.acmerobotics.velocityvortex.localization;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * Created by kelly on 11/2/2016.
 */

@Disabled
@Autonomous(name = "Navigation Test")
public class NavigationTest extends OpMode {

    private RobotLocation location = RobotLocation.ORIGIN;
    private VuforiaInterface vuforia;

    @Override
    public void init() {
        vuforia = new VuforiaInterface("vuforia", 1);
        vuforia.update();
    }

    @Override
    public void loop() {

        vuforia.update();
        if (vuforia.getLocation() != null) {
            location = vuforia.getLocation();
            telemetry.addData("visible", "true");
        } else {
            telemetry.addData("visible", "false");
        }

        telemetry.addData("Location", location.asString());
        telemetry.addData("transform", vuforia.getLocationTransform().formatAsTransform());
    }

}