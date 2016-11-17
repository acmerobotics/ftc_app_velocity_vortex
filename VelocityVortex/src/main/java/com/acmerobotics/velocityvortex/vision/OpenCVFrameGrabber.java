package com.acmerobotics.velocityvortex.vision;

import com.acmerobotics.library.camera.OpenCVFrameListener;
import com.vuforia.Image;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

/**
 * This class facilitates the extraction of OpenCV image data from {@link VuforiaLocalizer}
 */
public class OpenCVFrameGrabber extends VuforiaFrameGrabber {

    private OpenCVFrameListener frameListener;
    private Mat raw;
    private byte[] imgData;

    private VuforiaFrameListener vuforiaFrameListener = new VuforiaFrameListener() {
        @Override
        public void onFrame(Image frame) {
            if (frameListener != null) {
                ByteBuffer byteBuffer = frame.getPixels();
                if (imgData == null || imgData.length != byteBuffer.capacity()) {
                    imgData = new byte[byteBuffer.capacity()];
                }
                if (raw == null || raw.width() != frame.getWidth() || raw.height() != frame.getHeight()) {
                    raw = new Mat(frame.getHeight(), frame.getWidth(), CvType.CV_8UC2);
                }
                byteBuffer.get(imgData);
                raw.put(0, 0, imgData);
                Imgproc.cvtColor(raw, raw, Imgproc.COLOR_BGR5652BGR);

                frameListener.onFrame(raw);
            }
        }
    };

    public OpenCVFrameGrabber(VuforiaLocalizer vuforiaLocalizer) {
        super(vuforiaLocalizer);

        setFrameListener(vuforiaFrameListener);
    }

    public void setFrameListener(OpenCVFrameListener frameListener) {
        this.frameListener = frameListener;
    }
}
