package com.acmerobotics.velocityvortex.vision;

import android.content.Context;
import android.util.Log;

import com.acmerobotics.library.camera.OpenCVFrameListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.concurrent.CountDownLatch;

/**
 * Abstract base class for dealing with OpenCV-related camera devices
 */
public abstract class VisionCamera {

    private static final String TAG = "VisionCamera";

    private BaseLoaderCallback loaderCallback;
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
                        postInit();
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
     * Initializes OpenCV asynchronously
     */
    public final void init() {
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
     * Completes the actions of {@link #init()} synchronously
     */
    public final void initSync() {
        latch = new CountDownLatch(1);
        init();
        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public abstract void setFrameListener(OpenCVFrameListener frameListener);

    /**
     * Called when OpenCV is initialized
     */
    protected abstract void postInit();

    /**
     * Called to provide the latest frame
     */
    public abstract Mat getLatestFrame();

    /**
     * Called to start live processing
     */
    public abstract void start();

    /**
     * Called to stop live processing
     */
    public abstract void stop();
}
