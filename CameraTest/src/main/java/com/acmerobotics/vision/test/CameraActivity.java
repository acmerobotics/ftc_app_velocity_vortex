package com.acmerobotics.vision.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;

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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
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
        Mat image = inputFrame.rgba();
        int width = image.width(), height = image.height();

        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);

        Imgproc.resize(image, image, new Size(480, (480 * height) / width));

        List<Beacon> beacons = BeaconAnalyzer.analyzeImage(image);

        for (Beacon beacon : beacons) {
            beacon.draw(image);
        }

        Imgproc.resize(image, image, new Size(width, height));

        Collections.sort(beacons, new Comparator<Beacon>() {

            @Override
            public int compare(Beacon o1, Beacon o2) {
                Size s1 = o1.getBounds().size;
                Size s2 = o2.getBounds().size;
                double area1 = s1.width * s1.height;
                double area2 = s2.width * s2.height;
                return (area1 > area2) ? -1 : 1;
            }

        });

        int y = 0;
        for (Beacon result : beacons) {
            int score = result.score();

            String description = "";
            description += score + " " + result.getScoreString() + "  ";
            description += (result.getLeftRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B") + ",";
            description += result.getRightRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B";

            double textWidth = Imgproc.getTextSize(description, Core.FONT_HERSHEY_SIMPLEX, 1, 2, null).width;
            Imgproc.rectangle(image, new Point(0, y), new Point(textWidth + 20, y + 30), new Scalar(255, 255, 255), -1);
            Imgproc.putText(image, description, new Point(10, y + 25), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 2);

            y += 30;
        }

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

        return image;
    }

}
