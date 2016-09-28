package com.acmerobotics.vision.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
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

public class CameraActivity extends Activity implements CvCameraViewListener2, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    private SeekBar seekBar;

    private int threshold;

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

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        threshold = seekBar.getProgress();

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
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
//        Mat rgba = inputFrame.rgba();
        Mat gray = inputFrame.gray();
        Mat color = inputFrame.rgba();
        Mat output = new Mat();

        Imgproc.adaptiveThreshold(gray, output, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 71, 15);
//        Imgproc.threshold(gray, output, threshold, 70, Imgproc.THRESH_BINARY);

//        Imgproc.Canny(output, output, threshold / 2, threshold);
//         Imgproc.resize(gray, smaller, smaller.size());
//         Imgproc.pyrDown(rgba, rgba);

        Imgproc.GaussianBlur(output, output, new Size(9, 9), 2, 2);

        Mat circles = new Mat();
        Imgproc.HoughCircles(output, circles, Imgproc.CV_HOUGH_GRADIENT, 1, output.width() / 8, 2 * threshold, threshold, 0, 30);

//        Imgproc.cvtColor(output, output, Imgproc.COLOR_GRAY2BGR);

        Imgproc.putText(color, Integer.toString(threshold), new Point(10, 60), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 2);

        Point pt = new Point();
        int radius;

        for (int i = 0; i < circles.cols() && i < 2; i++) {
            double[] vCircle = circles.get(0, i);
            if (vCircle == null) break;
            pt.x = Math.round(vCircle[0]);
            pt.y = Math.round(vCircle[1]);
            radius = (int) Math.round(vCircle[2]);
            Imgproc.circle(color, pt, radius, new Scalar(0, 255, 0), 3);
        }

        return color;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        threshold = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
