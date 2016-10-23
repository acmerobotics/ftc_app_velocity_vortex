package com.acmerobotics.velocityvortex.vision;

import android.app.Activity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class OpenCvCamera {

    public static final String TAG = "OpenCvCamera";

    private OpenJavaCameraView cameraView;
    private LinearLayout parentView;
    private Activity activity;

    private BaseLoaderCallback loaderCallback;

    public OpenCvCamera(Activity activity, int parentViewId, int cameraId) {
        this.activity = activity;

        this.parentView = (LinearLayout) activity.findViewById(parentViewId);

        this.cameraView = new OpenJavaCameraView(activity, cameraId);

        this.loaderCallback = new BaseLoaderCallback(activity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i(TAG, "OpenCV loaded successfully");
                        cameraView.enableView();
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        this.parentView.addView(this.cameraView, params);

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this.activity, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        cameraView.setVisibility(SurfaceView.VISIBLE);
    }

    public void stop() {
        cameraView.disableView();

        parentView.removeView(cameraView);

        cameraView = null;
    }

    public void setCameraListener(CameraBridgeViewBase.CvCameraViewListener2 listener) {
        cameraView.setCvCameraViewListener(listener);
    }

}
