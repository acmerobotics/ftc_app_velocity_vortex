package com.acmerobotics.vision.test;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

public class NativeJavaCameraView extends JavaCameraView {

    public NativeJavaCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public NativeJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Camera getCamera() {
        return this.mCamera;
    }

}
