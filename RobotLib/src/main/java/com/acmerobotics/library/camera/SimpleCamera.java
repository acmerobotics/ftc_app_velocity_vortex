package com.acmerobotics.library.camera;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import org.firstinspires.ftc.robotcore.internal.AppUtil;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class SimpleCamera {

    private static final String TAG = "SimpleCamera";

    private AppUtil appUtil = AppUtil.getInstance();
    private FastCameraView cameraView;
    private ViewGroup parent;
    private Context ctx;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(ctx) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.start();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public SimpleCamera(final Activity activity, final int previewViewId) {
        appUtil.synchronousRunOnUiThread(new Runnable() {
            @Override
            public void run() {
                parent = (ViewGroup) activity.findViewById(previewViewId);
                cameraView = new FastCameraView(activity);
                parent.addView(cameraView);
            }
        });

        ctx = activity;
    }

    public void setFrameListener(FrameListener frameListener) {
        cameraView.setFrameListener(frameListener);
    }

    public void start() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager f" +
                    " initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, ctx, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public FastCameraView.Parameters getParameters() {
        return cameraView.getParameters();
    }

    public void stop() {
        appUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraView.stop();
                parent.removeAllViews();
            }
        });
    }

    public void hidePreview() {
        cameraView.hide();
    }

    public void showPreview() {
        cameraView.show();
    }

}
