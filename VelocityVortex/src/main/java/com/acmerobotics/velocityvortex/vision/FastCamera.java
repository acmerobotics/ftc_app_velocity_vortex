package com.acmerobotics.velocityvortex.vision;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;

import com.acmerobotics.library.camera.FastCameraView;
import com.acmerobotics.library.camera.OpenCVFrameListener;

import org.firstinspires.ftc.robotcore.internal.AppUtil;
import org.opencv.core.Mat;

/**
 * Camera device that uses a custom camera view ({@link FastCameraView}) based on OpenCV's
 * {@link org.opencv.android.JavaCameraView}
 */
public class FastCamera extends VisionCamera {

    private AppUtil appUtil = AppUtil.getInstance();
    private ViewGroup parent;
    private FastCameraView cameraView;

    public FastCamera(Context context, final int cameraViewId) {
        super(context);

        final Activity activity = appUtil.getActivity();
        appUtil.synchronousRunOnUiThread(new Runnable() {
            @Override
            public void run() {
                parent = (ViewGroup) activity.findViewById(cameraViewId);
                cameraView = new FastCameraView(activity);
                parent.addView(cameraView);
            }
        });

    }

    /**
     * Get the initialization parameters for the camera view. These parameters must be modified
     * before {@link #start()}} to take effect.
     * @return the camera view parameters
     */
    public FastCameraView.Parameters getParameters() {
        return cameraView.getParameters();
    }

    /**
     * Set the camera view listener
     * @param listener camera view listener
     */
    public void setCameraViewListener(FastCameraView.CameraViewListener listener) {
        cameraView.setCameraViewListener(listener);
    }

    @Override
    public void setFrameListener(final OpenCVFrameListener frameListener) {
        cameraView.setCameraViewListener(new FastCameraView.CameraViewListener() {
            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public void onDrawFrame(Canvas canvas) {

            }

            @Override
            public void onFrame(Mat frame) {
                frameListener.onFrame(frame);
            }
        });
    }

    @Override
    public void stop() {
        appUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraView.stop();
                parent.removeAllViews();
            }
        });
    }

    @Override
    public void start() {
        cameraView.start();
    }

    @Override
    protected void onFinishInit() {

    }

    @Override
    public Mat getLatestFrame() {
        Mat mat = new Mat();
        synchronized (cameraView) {
            cameraView.getLatestFrame().copyTo(mat);
        }
        return mat;
    }
}