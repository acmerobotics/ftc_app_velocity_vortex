package com.acmerobotics.velocityvortex.vision;

import android.util.Log;

import org.opencv.core.Core;

import java.text.DecimalFormat;

public class FpsCounter {
    private static final String TAG               = "FpsMeter";
    private static final int    STEP              = 20;
    private static final DecimalFormat FPS_FORMAT = new DecimalFormat("0.00");

    private int                 mFramesCouner;
    private double              mFrequency;
    private double              mFps;
    private long                mprevFrameTime;
    private String              mStrfps;
    boolean                     mIsInitialized = false;

    public void init() {
        mFramesCouner = 0;
        mFrequency = Core.getTickFrequency();
        mprevFrameTime = Core.getTickCount();
        mStrfps = "";
    }

    public void measure() {
        if (!mIsInitialized) {
            init();
            mIsInitialized = true;
        } else {
            mFramesCouner++;
            if (mFramesCouner % STEP == 0) {
                long time = Core.getTickCount();
                mFps = STEP * mFrequency / (time - mprevFrameTime);
                mprevFrameTime = time;
                Log.i(TAG, mStrfps);
            }
        }
    }

    public double fps() {
        return mFps;
    }

}
