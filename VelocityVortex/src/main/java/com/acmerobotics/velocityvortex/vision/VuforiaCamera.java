package com.acmerobotics.velocityvortex.vision;

import android.content.Context;

import com.acmerobotics.library.camera.OpenCVFrameListener;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.opencv.core.Mat;

/**
 * Camera device that processes frames using a Vuforia camera view
 */
public class VuforiaCamera extends VisionCamera {

    private OpenCVFrameGrabber frameGrabber;
    private OpenCVFrameListener frameListener;

    public VuforiaCamera(Context context, VuforiaLocalizer vuforia) {
        super(context);

        frameGrabber = new OpenCVFrameGrabber(vuforia);
    }

    @Override
    public void setFrameListener(OpenCVFrameListener frameListener) {
        this.frameListener = frameListener;

        if (frameGrabber != null) {
            frameGrabber.setFrameListener(frameListener);
        }
    }

    @Override
    protected void postInit() {

    }

    @Override
    public Mat getLatestFrame() {
        return frameGrabber.getLatestFrame();
    }

    public void start() {
        frameGrabber.setFrameListener(this.frameListener);
        frameGrabber.start();
    }

    public void stop() {
        frameGrabber.close();
    }
}
