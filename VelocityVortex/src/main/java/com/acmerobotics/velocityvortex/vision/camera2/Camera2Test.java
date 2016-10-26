package com.acmerobotics.velocityvortex.vision.camera2;

import android.app.Activity;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name="Camera 2 Test", group="Test")
public class Camera2Test extends OpMode {

    private Camera2Preview preview;

    @Override
    public void init() {
        preview = new Camera2Preview((Activity) hardwareMap.appContext);
        preview.start();
    }

    @Override
    public void loop() {

    }

    @Override
    public void stop() {
        preview.stop();
    }
}
