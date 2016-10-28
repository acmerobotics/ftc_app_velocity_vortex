package com.acmerobotics.vision.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * This class implements a simple camera interface for OpenCV based on {@link org.opencv.android.JavaCameraView}
 */
@SuppressWarnings("deprecation")
public class FastCameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final int MAGIC_TEXTURE_ID = 10;

    private static final String TAG = "FastCameraView";

    public enum PreviewType {
        RGBA,
        GRAY
    }

    public enum PreviewScale {
        CENTER,
        SCALE_TO_FIT,
        STRETCH
    }

    public class Parameters {
        public int cameraId = 0;
        public PreviewType previewType = PreviewType.RGBA;
        public PreviewScale previewScale = PreviewScale.CENTER;
        public int maxPreviewWidth = Integer.MAX_VALUE;
        public int maxPreviewHeight = Integer.MAX_VALUE;
    }

    private Parameters parameters;

    private Bitmap mCacheBitmap;
    private FrameListener mListener;
    private boolean mSurfaceExist;
    private boolean mShouldInitialize;

    protected int mFrameWidth;
    protected int mFrameHeight;
    protected int mSurfaceWidth;
    protected int mSurfaceHeight;
    protected int mCameraId;

    private boolean mReady;

    private byte mBuffer[];
    private Mat[] mCameraFrames;
    private Mat mDrawFrame;
    private int mFrameIndex;
    private Thread mThread;
    private boolean mStopThread;

    protected Camera mCamera;
    private SurfaceTexture mSurfaceTexture;

    public FastCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public FastCameraView(Context context) {
        super(context);

        initialize();
    }

    public void initialize() {
        parameters = new Parameters();
        mReady = false;
        mShouldInitialize = false;
        getHolder().addCallback(this);
    }

    public Camera getCamera() {
        return mCamera;
    }

    public Parameters getParameters() {
        return this.parameters;
    }

    /**
     * Sets the camera index
     * @param cameraIndex new camera index
     */
    public void setCameraIndex(int cameraIndex) {
        this.mCameraId = cameraIndex;
    }

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
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        mSurfaceExist = true;
        if (mReady) {
            if (mSurfaceExist) {
                disconnectCamera();
                connectCamera(width, height);
            }
        } else {
            mReady = true;
            if (mShouldInitialize) {
                connectCamera(width, height);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        disconnectCamera();
        mSurfaceExist = false;
        if (mListener != null) {
            mListener.onCameraViewStopped();
        }
    }

    public void start() {
        if (mReady) {
            connectCamera(mSurfaceWidth, mSurfaceHeight);
        }
        mShouldInitialize = true;
    }

    /**
     *
     * @param listener
     */

    public void setFrameListener(FrameListener listener) {
        mListener = listener;
    }

    protected void drawFrame(Mat frame) {
        Utils.matToBitmap(frame, mCacheBitmap);
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            int offX, offY;
            switch (this.parameters.previewScale) {
                case CENTER:
                    offX = (canvas.getWidth() - mCacheBitmap.getWidth()) / 2;
                    offY = (canvas.getHeight() - mCacheBitmap.getHeight()) / 2;
                    canvas.drawBitmap(mCacheBitmap, offX, offY, null);
                    break;
                case SCALE_TO_FIT:
                    double widthScalingFactor = (double) mSurfaceWidth / mFrameWidth;
                    double heightScalingFactor = (double) mSurfaceHeight / mFrameHeight;
                    int destWidth, destHeight;
                    if (widthScalingFactor  < heightScalingFactor) {
                        destWidth = mSurfaceWidth;
                        destHeight = (int) (mFrameHeight * widthScalingFactor);
                        offX = 0;
                        offY = (mSurfaceHeight - destHeight) / 2;
                    } else {
                        destWidth = (int) (mFrameWidth * heightScalingFactor);
                        destHeight = mSurfaceHeight;
                        offX = (mSurfaceWidth - destWidth) / 2;
                        offY = 0;
                    }
                    Rect src = new Rect(0, 0, mFrameWidth, mFrameHeight);
                    Rect dest = new Rect(offX, offY, destWidth + offX, destHeight + offY);
                    canvas.drawBitmap(mCacheBitmap, src, dest, null);
                    break;
                case STRETCH:
                    canvas.drawBitmap(mCacheBitmap,
                            new Rect(0, 0, mFrameWidth, mFrameHeight),
                            new Rect(0, 0, mSurfaceWidth, mSurfaceHeight), null);
                    break;
                default:
                    throw new RuntimeException("Unsupported preview scale: " + this.getParameters().previewScale);
            }
            if (mListener != null) {
                mListener.onDrawFrame(canvas);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    protected boolean initializeCamera(int width, int height) {
        Log.d(TAG, "Initialize java camera");
        boolean result = true;
        synchronized (this) {
            mCamera = Camera.open(mCameraId);

            /* Now set camera parameters */
            try {
                Camera.Parameters params = mCamera.getParameters();
                Log.d(TAG, "getSupportedPreviewSizes()");
                List<Camera.Size> sizes = params.getSupportedPreviewSizes();

                if (sizes != null) {
                    /* Select the size that fits surface considering maximum size allowed */
                    Size frameSize = calculateCameraFrameSize(sizes, width, height);

                    for (Camera.Size size : sizes) {
                        Log.i(TAG, size.width + "x" + size.height);
                    }

                    params.setPreviewFormat(ImageFormat.NV21);
                    Log.d(TAG, "Set preview size to " + Integer.valueOf((int)frameSize.width) + "x" + Integer.valueOf((int)frameSize.height));
                    params.setPreviewSize((int)frameSize.width, (int)frameSize.height);

                    params.setRecordingHint(true);

                    List<String> FocusModes = params.getSupportedFocusModes();
                    if (FocusModes != null && FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                    {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }

                    mCamera.setParameters(params);
                    params = mCamera.getParameters();

                    mFrameWidth = params.getPreviewSize().width;
                    mFrameHeight = params.getPreviewSize().height;

                    int size = mFrameWidth * mFrameHeight;
                    size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
                    mBuffer = new byte[size];

                    mCamera.addCallbackBuffer(mBuffer);
                    mCamera.setPreviewCallbackWithBuffer(this);

                    mCameraFrames = new Mat[2];
                    mCameraFrames[0] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);
                    mCameraFrames[1] = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);
                    mDrawFrame = new Mat();

                    mCacheBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);

                    mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
                    mCamera.setPreviewTexture(mSurfaceTexture);

                    /* Finally we are ready to start the preview */
                    Log.d(TAG, "startPreview");
                    mCamera.startPreview();
                }
                else
                    result = false;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
        }

        return result;
    }

    protected void releaseCamera() {
        synchronized (this) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);

                mCamera.release();
            }
            mCamera = null;
            if (mCameraFrames != null) {
                mCameraFrames[0].release();
                mCameraFrames[1].release();
                mCameraFrames = null;
            }
        }
    }

    private boolean mCameraFrameReady = false;

    protected boolean connectCamera(int width, int height) {

        /* 1. We need to instantiate camera
         * 2. We need to start thread which will be getting frames
         */
        /* First step - initialize camera connection */
        Log.d(TAG, "Connecting to camera");
        if (!initializeCamera(width, height))
            return false;

        mCameraFrameReady = false;

        /* now we can start update thread */
        Log.d(TAG, "Starting processing thread");
        mStopThread = false;
        mThread = new Thread(new FastCameraView.CameraWorker());
        mThread.start();
        if (mListener != null)
            mListener.onCameraViewStarted(mSurfaceWidth, mSurfaceHeight);

        return true;
    }

    protected void disconnectCamera() {
        /* 1. We need to stop thread which updating the frames
         * 2. Stop camera and release it
         */
        Log.d(TAG, "Disconnecting from camera");
        try {
            mStopThread = true;
            Log.d(TAG, "Notify thread");
            synchronized (this) {
                this.notify();
            }
            Log.d(TAG, "Waiting for thread");
            if (mThread != null)
                mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mThread =  null;
        }

        /* Now release camera */
        releaseCamera();

        mCameraFrameReady = false;
    }

    public void stop() {
        disconnectCamera();
    }

    @Override
    public void onPreviewFrame(byte[] frame, Camera arg1) {
        Log.d(TAG, "Preview Frame received. Frame size: " + frame.length);
        synchronized (this) {
            mCameraFrames[mFrameIndex].put(0, 0, frame);
            if (!mCameraFrameReady) {
                mFrameIndex= 1 - mFrameIndex;
                mCameraFrameReady = true;
            }
            this.notify();
        }
        if (mCamera != null)
            mCamera.addCallbackBuffer(mBuffer);
    }

    /**
     * This helper method can be called by subclasses to select camera preview size.
     * It goes over the list of the supported preview sizes and selects the maximum one which
     * fits both values set via setMaxFrameSize() and surface frame allocated for this view
     * @param supportedSizes
     * @param surfaceWidth
     * @param surfaceHeight
     * @return optimal frame size
     */
    protected Size calculateCameraFrameSize(List<Camera.Size> supportedSizes, int surfaceWidth, int surfaceHeight) {
        int calcWidth = 0;
        int calcHeight = 0;

        int maxWidth = Math.min(surfaceWidth, this.parameters.maxPreviewWidth);
        int maxHeight = Math.min(surfaceHeight, this.parameters.maxPreviewHeight);

        for (Camera.Size size : supportedSizes) {
            int width = size.width;
            int height = size.height;

            if (width <= maxWidth && height <= maxHeight) {
                if (width >= calcWidth && height >= calcHeight) {
                    calcWidth = width;
                    calcHeight = height;
                }
            }
        }

        return new Size(calcWidth, calcHeight);
    }

    private class CameraWorker implements Runnable {

        @Override
        public void run() {
            while(!mStopThread) {
                if (mCameraFrameReady) {
                    if (mListener != null) {
                        synchronized (FastCameraView.this) {
                            int cacheIndex = 1 - mFrameIndex;
                            switch (parameters.previewType) {
                                case GRAY:
                                    mDrawFrame = mCameraFrames[cacheIndex].submat(0, mFrameHeight, 0, mFrameWidth);
                                    break;
                                case RGBA:
                                    Imgproc.cvtColor(mCameraFrames[cacheIndex], mDrawFrame, Imgproc.COLOR_YUV2RGBA_NV21, 4);
                                    break;
                                default:
                                    throw new RuntimeException("Unsupported preview type: " + parameters.previewType);
                            }
                            mListener.onCameraFrame(mDrawFrame);
                            drawFrame(mDrawFrame);
                            mCameraFrameReady = false;
                        }
                    }
                } else {
                    try {
                        synchronized (FastCameraView.this) {
                            FastCameraView.this.wait();
                        }
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        }
    }

}