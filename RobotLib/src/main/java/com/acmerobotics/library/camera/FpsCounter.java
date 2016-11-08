package com.acmerobotics.library.camera;

import android.util.Log;

import org.opencv.core.Core;

import java.text.DecimalFormat;

public class FpsCounter {
    private static final String TAG               = "FpsMeter";
    private static final int    STEP              = 20;
    private static final DecimalFormat FPS_FORMAT = new DecimalFormat("0.00");

    private int mFramesCounter;
    private double mFrequency;
    private double mFps;
    private long mPrevFrameTime;
    private String mStrfps;
    private boolean mIsInitialized = false;

    public void init() {
        mFramesCounter = 0;
        mFrequency = Core.getTickFrequency();
        mPrevFrameTime = Core.getTickCount();
        mStrfps = "";
        mIsInitialized = true;
    }

    public void measure() {
        if (!mIsInitialized) {
            init();
        } else {
            mFramesCounter++;
            if (mFramesCounter % STEP == 0) {
                long time = Core.getTickCount();
                mFps = STEP * mFrequency / (time - mPrevFrameTime);
                mPrevFrameTime = time;
                Log.i(TAG, mStrfps);
            }
        }
    }

    public double fps() {
        return mFps;
    }

}
