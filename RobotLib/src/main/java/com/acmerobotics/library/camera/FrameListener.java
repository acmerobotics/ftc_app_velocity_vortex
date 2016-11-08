package com.acmerobotics.library.camera;

import android.graphics.Canvas;

import org.opencv.core.Mat;

public interface FrameListener {
    /**
     * This method is invoked when camera preview has started. After this method is invoked
     * the frames will start to be delivered to client via the onCameraFrame() callback.
     * @param width -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    public void onCameraViewStarted(int width, int height);

    /**
     * This method is invoked when camera preview has been stopped for some reason.
     * No frames will be delivered via onCameraFrame() callback after this method is called.
     */
    public void onCameraViewStopped();

    /**
     * This method is invoked when delivery of the frame needs to be done. The incoming image
     * may be modified and the modified result will appear on the preview.
     */
    public void onCameraFrame(Mat inputFrame);

    public void onDrawFrame(Canvas canvas);
}