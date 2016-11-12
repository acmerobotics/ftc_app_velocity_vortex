package com.acmerobotics.vision.test;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.acmerobotics.library.camera.CanvasOverlay;
import com.acmerobotics.library.camera.FastCameraView;
import com.acmerobotics.library.camera.FpsCounter;
import com.acmerobotics.library.camera.FrameListener;
import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements FrameListener {

    private static final String TAG = "CameraActivity";

    private FastCameraView cameraView;

    private List<Beacon> beacons;
    private FpsCounter fpsCounter;

    private int intermediateIndex = 0;
    private String intermediateKey;

    private final Comparator<Beacon> beaconSizeComparator = new Comparator<Beacon>() {

        @Override
        public int compare(Beacon o1, Beacon o2) {
            Size s1 = o1.getBounds().size;
            Size s2 = o2.getBounds().size;
            double area1 = s1.width * s1.height;
            double area2 = s2.width * s2.height;
            return (area1 > area2) ? -1 : 1;
        }

    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.start();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private View.OnClickListener cameraViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            intermediateIndex++;
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

        setContentView(R.layout.activity_camera);

        cameraView = (FastCameraView) findViewById(R.id.fastCameraView);

        fpsCounter = new FpsCounter();

        BeaconAnalyzer.DEBUG = false;

        beacons = new ArrayList<>();

        FastCameraView.Parameters params = cameraView.getParameters();
        params.maxPreviewWidth = 640;
        params.maxPreviewHeight = 640;
        params.orientation = FastCameraView.Orientation.AUTO;
        params.previewScale = FastCameraView.PreviewScale.SCALE_TO_FIT;

        cameraView.setFrameListener(CameraActivity.this);

        ToggleButton debugToggle = (ToggleButton) findViewById(R.id.debugToggle);
        debugToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BeaconAnalyzer.DEBUG = isChecked;
                if (isChecked) {
                    cameraView.setOnClickListener(cameraViewClickListener);
                } else {
                    cameraView.setOnClickListener(null);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        cameraView.stop();
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

    public void onCameraViewStarted(int width, int height) {
        fpsCounter.init();
    }

    public void onCameraViewStopped() {
    }

    public void onCameraFrame(Mat image) {
        fpsCounter.measure();

        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);

        beacons.clear();
        BeaconAnalyzer.analyzeImage(image, beacons);

        if (BeaconAnalyzer.DEBUG) {
            Map<String, Mat> intermediates = BeaconAnalyzer.getIntermediates();
            String[] modes = new String[intermediates.keySet().size()];
            intermediates.keySet().toArray(modes);
            intermediateKey = modes[intermediateIndex % modes.length];
            intermediates.get(intermediateKey).copyTo(image);

            if (image.channels() == 3) {
                Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
            }
        } else {
            for (Beacon beacon : beacons) {
                beacon.draw(image);
            }
            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
        }
    }

    @Override
    public void onDrawFrame(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        CanvasOverlay overlay = new CanvasOverlay(canvas, 15);

        Collections.sort(beacons, beaconSizeComparator);

        for (Beacon result : beacons) {
            Beacon.Score score = result.getScore();
            overlay.drawText(score.getNumericScore() + " " + score.toString(), CanvasOverlay.ImageRegion.TOP_LEFT, 0.1, paint);
        }
        overlay.drawText((Math.round(100 * fpsCounter.fps()) / 100.0) + " FPS", CanvasOverlay.ImageRegion.BOTTOM_LEFT, 0.1, paint);
        if (BeaconAnalyzer.DEBUG) overlay.drawText(intermediateKey, CanvasOverlay.ImageRegion.BOTTOM_RIGHT, 0.1, paint);
    }

}
