package com.acmerobotics.vision.test;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;
import com.acmerobotics.library.vision.ImageOverlay;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private NativeJavaCameraView mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    private Camera camera = null;

    private List<String> wbOptions;
    private int wbIndex;

    private List<String> sceneOptions;
    private int sceneIndex;

    private Mat lastImage;

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

        mOpenCvCameraView = (NativeJavaCameraView) findViewById(R.id.activity_java_surface_view);

        Button wbButton = (Button) findViewById(R.id.wbButton);
        wbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.this.nextCameraWhiteBalance();
            }
        });

        Button sceneButton = (Button) findViewById(R.id.sceneButton);
        sceneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.this.nextCameraScene();
            }
        });

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
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
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

    public void nextCameraWhiteBalance() {
        Camera.Parameters params = camera.getParameters();
        wbIndex = (wbIndex + 1) % wbOptions.size();
        params.setWhiteBalance(wbOptions.get(wbIndex));
        camera.setParameters(params);
    }

    public void nextCameraScene() {
        Camera.Parameters params = camera.getParameters();
        sceneIndex = (sceneIndex + 1) % sceneOptions.size();
        params.setSceneMode(sceneOptions.get(sceneIndex));
        camera.setParameters(params);
    }

    public void onCameraViewStarted(int width, int height) {
        camera = mOpenCvCameraView.getCamera();

        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        camera.setParameters(params);

        wbIndex = -1;
        wbOptions = params.getSupportedWhiteBalance();
        nextCameraWhiteBalance();

        sceneOptions = params.getSupportedSceneModes();
        sceneIndex = -1;
        nextCameraScene();
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat image = inputFrame.rgba();

        this.lastImage = image;

        int width = image.width(), height = image.height();

        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);

        Imgproc.resize(image, image, new Size(480, (480 * height) / width));

        List<Beacon> beacons = BeaconAnalyzer.analyzeImage(image);

        for (Beacon beacon : beacons) {
            beacon.draw(image);
        }

        Imgproc.resize(image, image, new Size(width, height));

        ImageOverlay overlay = new ImageOverlay(image, 30);
        overlay.setBackgroundColor(new Scalar(0, 0, 0));

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

        for (Beacon result : beacons) {
            int score = result.score();

            String description = "";
            description += score + " " + result.getScoreString() + "  ";
            description += (result.getLeftRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B") + ",";
            description += result.getRightRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B";

            overlay.drawText(description, ImageOverlay.ImageRegion.TOP_LEFT, Core.FONT_HERSHEY_SIMPLEX, 0.15, new Scalar(255, 255, 255), 6);
        }
        overlay.drawText("Scene: " + sceneOptions.get(sceneIndex).toString(), ImageOverlay.ImageRegion.BOTTOM_LEFT, Core.FONT_HERSHEY_SIMPLEX, 0.15, new Scalar(255, 255, 255), 6);
        overlay.drawText("WB: " + wbOptions.get(wbIndex).toString(), ImageOverlay.ImageRegion.BOTTOM_LEFT, Core.FONT_HERSHEY_SIMPLEX, 0.15, new Scalar(255, 255, 255), 6);

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

        return image;
    }

}
