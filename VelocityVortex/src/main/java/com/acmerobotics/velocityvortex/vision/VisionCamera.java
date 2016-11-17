package com.acmerobotics.velocityvortex.vision;

import android.content.Context;
import android.util.Log;

import com.acmerobotics.library.camera.OpenCVFrameListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.concurrent.CountDownLatch;

/**
 * Abstract base class for dealing with OpenCV-related camera devices
 */
public abstract class VisionCamera {

    private static final String TAG = "VisionCamera";

    private BaseLoaderCallback loaderCallback;
    protected OpenCVFrameListener frameListener;
    private Context context;

    private CountDownLatch latch;

    public VisionCamera(Context context) {
        this.context = context;
        loaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i(TAG, "OpenCV loaded successfully");
                        onStart();
                        if (latch != null) latch.countDown();
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

    /**
     * Starts the camera and initializes OpenCV asynchonously
     */
    public final void start() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager f" +
                    " initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, context, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * Completes the actions of {@link #start()} synchronously
     */
    public final void startSync() {
        latch = new CountDownLatch(1);
        start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Stops the camera
     */
    public final void stop() {
        onStop();
    }

    public void setFrameListener(OpenCVFrameListener frameListener) {
        this.frameListener = frameListener;
    }

    /**
     * Called when the camera is stopped
     */
    protected abstract void onStop();

    /**
     * Called when the camera is started and OpenCV is initialized
     */
    protected abstract void onStart();
}
