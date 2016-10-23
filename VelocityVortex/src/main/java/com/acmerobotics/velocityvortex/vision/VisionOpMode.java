package com.acmerobotics.velocityvortex.vision;

import android.app.Activity;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public abstract class VisionOpMode extends OpMode {

    protected OpenCvCamera camera;

    @Override
    public void init() {
        this.camera = new OpenCvCamera((Activity) hardwareMap.appContext, 0x7f0c0018);
    }

    @Override
    public void start() {
        this.camera.start();
    }

    @Override
    public void stop() {
        this.camera.stop();
    }
}
