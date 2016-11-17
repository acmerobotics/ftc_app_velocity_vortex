package com.acmerobotics.velocityvortex.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.acmerobotics.library.camera.CanvasOverlay;
import com.acmerobotics.library.camera.FpsCounter;
import com.acmerobotics.library.camera.OpenCVFrameListener;
import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;
import com.acmerobotics.library.vision.BeaconAreaComparator;
import com.acmerobotics.velocityvortex.localization.VuforiaInterface;
import com.acmerobotics.velocityvortex.vision.VuforiaCamera;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.internal.AppUtil;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Autonomous(name="Beacon Vuforia")
public class BeaconVuforiaTest extends OpMode {

    private static final int MAX_DIMENSION = 640;

    private AppUtil appUtil = AppUtil.getInstance();
    private VuforiaInterface vuforia;
    private VuforiaCamera vuforiaCamera;

    private List<Beacon> tempBeacons;
    private List<Beacon> beacons;

    private BeaconOverlay overlayView;
    private FrameLayout frameLayout;

    private FpsCounter fpsCounter;

    public class BeaconOverlay extends View {

        Paint paint = new Paint();

        private BeaconAreaComparator beaconComparator = new BeaconAreaComparator();

        public BeaconOverlay(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (beacons != null) {
                CanvasOverlay overlay = new CanvasOverlay(canvas, 15);

                paint.setColor(Color.RED);

                Collections.sort(beacons, beaconComparator);

                for (Beacon result : beacons) {
                    Beacon.Score score = result.getScore();
                    overlay.drawText(score.getNumericScore() + " " + score.toString() + " " + (result.getLeftRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B") + "," + (result.getRightRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B"), CanvasOverlay.ImageRegion.TOP_LEFT, 0.1, paint);
                }
                overlay.drawText("FPS: " + (Math.round(100 * fpsCounter.fps()) / 100.0), CanvasOverlay.ImageRegion.BOTTOM_RIGHT, 0.1, paint);
            }

            super.onDraw(canvas);
        }
    }

    @Override
    public void init() {
        tempBeacons = new ArrayList<>();
        beacons = new ArrayList<>();

        vuforia = new VuforiaInterface("beacon", 0);
        vuforiaCamera = new VuforiaCamera(hardwareMap.appContext, vuforia.getLocalizer());
        vuforiaCamera.setFrameListener(new OpenCVFrameListener() {
            @Override
            public void onFrame(Mat frame) {
                fpsCounter.measure();
                Imgproc.resize(frame, frame, getSmallSize(frame.size()));
                tempBeacons.clear();
                BeaconAnalyzer.analyzeImage(frame, tempBeacons);
                beacons.clear();
                beacons.addAll(tempBeacons);
                appUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        overlayView.invalidate();
                    }
                });
            }
        });
        vuforiaCamera.startSync();

        beacons = new ArrayList<>();

        fpsCounter = new FpsCounter();
        fpsCounter.init();

        final Activity activity = appUtil.getActivity();
        appUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overlayView = new BeaconOverlay(activity);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                overlayView.setLayoutParams(layoutParams);
                frameLayout = (FrameLayout) activity.findViewById(com.qualcomm.ftcrobotcontroller.R.id.cameraMonitorViewId).getParent();
                frameLayout.addView(overlayView);
            }
        });
    }

    @Override
    public void loop() {
        for (int i = 0; i < 5; i++) {
            if (i < beacons.size()) {
                telemetry.addData(Integer.toString(i), beacons.get(i).getScore().getNumericScore() + ": " + beacons.get(i).getScore().toString() + " " + (beacons.get(i).getLeftRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B") + "," + (beacons.get(i).getRightRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B"));
            } else {
                telemetry.addData(Integer.toString(i), "");
            }
        }
    }

    @Override
    public void stop() {
        vuforiaCamera.stop();
        appUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                frameLayout.removeView(overlayView);
            }
        });
    }

    public Size getSmallSize(Size big) {
        if (big.width > big.height) {
            return new Size(MAX_DIMENSION, big.height * MAX_DIMENSION / big.width);
        } else {
            return new Size(big.width * MAX_DIMENSION / big.height, MAX_DIMENSION);
        }
    }
}
