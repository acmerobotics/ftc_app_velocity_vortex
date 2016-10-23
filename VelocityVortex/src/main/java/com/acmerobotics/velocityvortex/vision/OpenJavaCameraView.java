package com.acmerobotics.velocityvortex.vision;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

public class OpenJavaCameraView extends JavaCameraView {

    public OpenJavaCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public OpenJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Camera getCamera() {
        return this.mCamera;
    }

}
