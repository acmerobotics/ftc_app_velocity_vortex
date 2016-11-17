package com.acmerobotics.library.camera;

import org.opencv.core.Mat;

public interface OpenCVFrameListener {

    /**
     * This method is invoked when a new frame is available.
     * @param frame the incoming frame
     */
    void onFrame(Mat frame);
}
