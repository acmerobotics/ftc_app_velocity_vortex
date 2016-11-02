package com.acmerobotics.velocityvortex.vision;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.firstinspires.ftc.robotcore.internal.AppUtil;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class OpenCvCamera {

    public static final String TAG = "OpenCvCamera";

    private FastCameraView cameraView;
    private final LinearLayout parentView;
    private final Activity activity;
    private FastCameraView.CvCameraViewListener2 listener;

    private AppUtil appUtil;

    private BaseLoaderCallback loaderCallback;

    public OpenCvCamera(final Activity activity, int parentViewId, int cameraId) {
        this.appUtil = AppUtil.getInstance();
        this.activity = activity;

        this.parentView = (LinearLayout) activity.findViewById(parentViewId);

        this.loaderCallback = new BaseLoaderCallback(activity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i(TAG, "OpenCV loaded successfully");
                        appUtil.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cameraView = new FastCameraView(activity, 0);
                                if (listener != null) {
                                    cameraView.setCvCameraViewListener(listener);
                                }
                                final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                parentView.addView(cameraView, params);
                                cameraView.setVisibility(SurfaceView.VISIBLE);
                            }
                        });
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
    }

    public OpenCvCamera(Activity activity, int parentViewId) {
        this(activity, parentViewId, 0);
    }

    public void start() {
        appUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!OpenCVLoader.initDebug()) {
                    Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, activity, loaderCallback);
                } else {
                    Log.d(TAG, "OpenCV library found inside package. Using it!");
                    loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }
            }
        });
    }

    public void stop() {
        appUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentView.removeView(OpenCvCamera.this.cameraView);
            }
        });
    }

    public void setCameraListener(FastCameraView.CvCameraViewListener2 listener) {
        this.listener = listener;
        if (cameraView != null) {
            cameraView.setCvCameraViewListener(listener);
        }
    }

    public Camera getCamera() {
        return cameraView.getCamera();
    }

}
