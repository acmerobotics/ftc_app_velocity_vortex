package com.acmerobotics.velocityvortex.vision;

import android.util.Log;

import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import java.util.concurrent.BlockingQueue;

/**
 * This class allows Vuforia frames to be captured in an asynchronous fashion
 */
public class VuforiaFrameGrabber {

    private static final String TAG = "VuforiaFrameGrabber";

    /**
     * Interface to listen for frame updates
     */
    protected interface VuforiaFrameListener {
        /**
         * This method is called every time a new frame is available. New frames will not be
         * processed until this callback returns.
         *
         * @param frame the image
         */
        void onFrame(Image frame);
    }

    private VuforiaFrameListener listener;
    private VuforiaLocalizer vuforia;
    private BlockingQueue<VuforiaLocalizer.CloseableFrame> frameQueue;
    private FrameWorker frameWorker;

    public VuforiaFrameGrabber(VuforiaLocalizer vuforiaLocalizer) {
        vuforia = vuforiaLocalizer;
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
        vuforia.setFrameQueueCapacity(1);
        frameQueue = vuforia.getFrameQueue();
    }

    /**
     * Closes the device and halts frame updates
     */
    public void close() {
        if (frameWorker != null) {
            frameWorker.terminate();
            try {
                frameWorker.join();
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * Begins automatic capture and processing of frames
     */
    public void start() {
        frameWorker = new FrameWorker();
        frameWorker.start();
    }

    /**
     * Gets the latest frame synchronously
     *
     * @return the frame
     */
    public VuforiaLocalizer.CloseableFrame getLatestVuforiaFrame() {
        VuforiaLocalizer.CloseableFrame frame = null;
        try {
            frame = frameQueue.take();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return frame;
    }

    protected static Image getRGBImage(VuforiaLocalizer.CloseableFrame frame) {
        for (int i = 0; i < frame.getNumImages(); i++) {
            Image img = frame.getImage(i);
            if (img.getFormat() == PIXEL_FORMAT.RGB565) {
                return img;
            }
        }
        return null;
    }

    public void setFrameListener(VuforiaFrameListener listener) {
        this.listener = listener;
    }

    private class FrameWorker extends Thread {

        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                if (frameQueue.isEmpty()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    VuforiaLocalizer.CloseableFrame frame = getLatestVuforiaFrame();
                    Image img = getRGBImage(frame);
                    if (img != null && listener != null) listener.onFrame(img);
                    if (frame != null) frame.close();
                }
            }
        }

        public void terminate() {
            running = false;
        }
    }

}
