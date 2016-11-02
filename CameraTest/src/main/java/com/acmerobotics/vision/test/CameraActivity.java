package com.acmerobotics.vision.test;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraActivity extends Activity implements FastCameraView.FrameListener {
    private static final String TAG = "OCVSample::Activity";

    private FastCameraView mCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    List<Beacon> beacons;

    private Camera camera = null;

    private List<String> wbOptions;
    private int wbIndex;

    private List<String> sceneOptions;
    private int sceneIndex;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCameraView.start();
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

        mCameraView = (FastCameraView) findViewById(R.id.activity_java_surface_view);

        FastCameraView.Parameters params = mCameraView.getParameters();
        params.maxPreviewWidth = 640;
        params.maxPreviewHeight = 640;
        params.previewScale = FastCameraView.PreviewScale.SCALE_TO_FIT;

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

        mCameraView.setVisibility(SurfaceView.VISIBLE);

        mCameraView.setFrameListener(CameraActivity.this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mCameraView.stop();
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
        camera = mCameraView.getCamera();

        Camera.Parameters params = camera.getParameters();

        wbIndex = -1;
        wbOptions = params.getSupportedWhiteBalance();
        nextCameraWhiteBalance();

        sceneOptions = params.getSupportedSceneModes();
        sceneIndex = -1;
        nextCameraScene();
    }

    public void onCameraViewStopped() {
    }

    public void onCameraFrame(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);

        beacons = BeaconAnalyzer.analyzeImage(image);

        for (Beacon beacon : beacons) {
            beacon.draw(image);
        }

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
    }

    @Override
    public void onDrawFrame(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        CanvasOverlay overlay = new CanvasOverlay(canvas, 30);

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
            int score = result.getScore().getNumericScore();

            String description = "";
            description += score + " " + result.getScore().toString() + "  ";
            description += (result.getLeftRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B") + ",";
            description += result.getRightRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B";

            overlay.drawText(description, CanvasOverlay.ImageRegion.TOP_LEFT, 0.15, paint);
        }
        overlay.drawText("Scene: " + sceneOptions.get(sceneIndex).toString(), CanvasOverlay.ImageRegion.BOTTOM_LEFT, 0.15, paint);
        overlay.drawText("WB: " + wbOptions.get(wbIndex).toString(), CanvasOverlay.ImageRegion.BOTTOM_LEFT, 0.15, paint);
    }

}
