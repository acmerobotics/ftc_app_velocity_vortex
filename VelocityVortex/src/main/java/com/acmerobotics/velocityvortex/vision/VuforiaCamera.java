package com.acmerobotics.velocityvortex.vision;

import android.content.Context;

import com.acmerobotics.library.camera.OpenCVFrameListener;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

/**
 * Camera device that processes frames using a Vuforia camera view
 */
public class VuforiaCamera extends VisionCamera {

    private OpenCVFrameGrabber frameGrabber;
    private VuforiaLocalizer vuforia;

    public VuforiaCamera(Context context, VuforiaLocalizer vuforia) {
        super(context);

        this.vuforia = vuforia;
    }

    @Override
    public void setFrameListener(OpenCVFrameListener frameListener) {
        super.setFrameListener(frameListener);

        if (frameGrabber != null) {
            frameGrabber.setFrameListener(frameListener);
        }
    }

    @Override
    protected void onStop() {
        frameGrabber.close();
    }

    @Override
    protected void onStart() {
        frameGrabber = new OpenCVFrameGrabber(vuforia);
        frameGrabber.setFrameListener(this.frameListener);
    }
}
