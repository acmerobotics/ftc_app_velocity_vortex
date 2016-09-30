package com.acmerobotics.vision.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CameraActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    private SeekBar[] seekBars;

    private PictureType pictureType;

    public enum PictureType {
        NORMAL,
        DEBUG,
        EDGES;
        public PictureType next() {
            PictureType[] types = PictureType.values();
            return types[(this.ordinal() + 1) % types.length];
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_surface_view);

        seekBars = new SeekBar[3];
        seekBars[0] = (SeekBar) findViewById(R.id.adaptiveThreshold);
        seekBars[1] = (SeekBar) findViewById(R.id.upperThreshold);
        seekBars[2] = (SeekBar) findViewById(R.id.lowerThreshold);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        pictureType = PictureType.NORMAL;
        findViewById(R.id.cameraLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pictureType = pictureType.next();
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        int adaptiveThreshold = seekBars[0].getProgress();
        adaptiveThreshold *= 2;
        adaptiveThreshold += 1;

        int upperThreshold = seekBars[1].getProgress();
        int lowerThreshold = seekBars[2].getProgress();
        if (lowerThreshold > upperThreshold) {
            lowerThreshold = upperThreshold;
            seekBars[2].setProgress(lowerThreshold);
        }
//        Mat rgba = inputFrame.rgba();
        Mat gray = inputFrame.gray();
        Mat color = inputFrame.rgba();
        Mat output = new Mat();

        Imgproc.adaptiveThreshold(gray, output, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, adaptiveThreshold, 15);
//        Imgproc.threshold(gray, output, threshold, 255, Imgproc.THRESH_BINARY);

//        Imgproc.Canny(output, output, threshold / 2, threshold);
//         Imgproc.resize(gray, smaller, smaller.size());
//         Imgproc.pyrDown(rgba, rgba);

        Imgproc.GaussianBlur(output, output, new Size(9, 9), 2, 2);

        Mat circles = new Mat();
        Imgproc.HoughCircles(output, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 50, upperThreshold, lowerThreshold, 0, 30);

        switch (pictureType) {
            case NORMAL:
                output = color;
                break;
            case EDGES:
                Imgproc.Canny(output, output, upperThreshold / 2, upperThreshold);
                // fall through
            case DEBUG:
                Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);
                break;
        }

        // debug text
        Imgproc.putText(output, pictureType.toString(), new Point(10, 60), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 2);
        Imgproc.putText(output, "L: " + Integer.toString(lowerThreshold), new Point(10, 100), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 2);
        Imgproc.putText(output, "U: " + Integer.toString(upperThreshold), new Point(10, 125), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 2);
        Imgproc.putText(output, "A: " + Integer.toString(adaptiveThreshold), new Point(10, 150), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 2);

        int numCircles = circles.cols();

        Circle button1, button2;

//        int bestError = Integer.MAX_VALUE, error;
//        Circle[] bestPair = new Circle[]{null, null};
//
//        for (int i = 0; i < numCircles; i++) {
//            button1 = Circle.fromDoubleArray(circles.get(0, i));
//            for (int j = i + 1; j < numCircles; j++) {
//                button2 = Circle.fromDoubleArray(circles.get(0, j));
//                error = (int) Math.pow(button1.radius - button2.radius, 2);
//                if (error < bestError) {
//                    bestPair[0] = button1;
//                    bestPair[1] = button2;
//                    bestError = error;
//                }
//            }
//        }
//
//        if (bestError > 2) {
//            Imgproc.putText(output, "NO BUTTONS", new Point(10, 90), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 2);
//        } else {
//            for (int i = 0; i < 2; i++) {
//                Circle button = bestPair[i];
//                Imgproc.circle(output, button.pt, button.radius, new Scalar(0, 255, 0), 3);
//                Imgproc.putText(output, Integer.toString(button.radius), new Point(button.pt.x + (3 * button.radius) / 2, button.pt.y), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255, 0), 2);
//            }
//        }

        for (int i = 0; i < numCircles; i++) {
            Circle button = Circle.fromDoubleArray(circles.get(0, i));
            Imgproc.circle(output, button.pt, button.radius, new Scalar(0, 255, 0), 3);
        }

        return output;
    }
}
