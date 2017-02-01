package com.acmerobotics.velocityvortex.vision;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.acmerobotics.library.camera.CanvasOverlay;
import com.acmerobotics.library.camera.FastCameraView;
import com.acmerobotics.library.camera.FpsCounter;
import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;
import com.acmerobotics.library.vision.BeaconAreaComparator;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.R;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract opmode that provides a simple beacon detection interface
 */
public abstract class BeaconOpMode extends OpMode {

    protected FastCamera camera;
    protected FpsCounter fpsCounter;
    protected List<Beacon> beacons;
    private boolean ready;

    private FastCameraView.CameraViewListener cameraViewListener =
            new FastCameraView.CameraViewListener() {
                @Override
                public void onCameraViewStarted(int width, int height) {
                    ready = true;
                    fpsCounter.init();
                }

                @Override
                public void onCameraViewStopped() {
                    ready = false;
                }

                public void onFrame(Mat image) {
                    fpsCounter.measure();

                    Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);

                    synchronized (beacons) {
                        beacons.clear();
                        BeaconAnalyzer.analyzeImage(image, beacons);
                    }

                    for (Beacon beacon : beacons) {
                        beacon.draw(image);
                    }

                    Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
                }

                @Override
                public void onDrawFrame(Canvas canvas) {
                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);

                    CanvasOverlay overlay = new CanvasOverlay(canvas, 15);

                    Collections.sort(beacons, new BeaconAreaComparator());

                    for (Beacon result : beacons) {
                        int score = result.getScore().getNumericScore();

                        String description = "";
                        description += score + " " + result.getScore().toString() + "  ";
                        description += (result.getLeftRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B") + ",";
                        description += result.getRightRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B";

                        overlay.drawText(description, CanvasOverlay.ImageRegion.TOP_LEFT, 0.1, paint);
                    }
                    overlay.drawText("FPS: " + (Math.round(100 * fpsCounter.fps()) / 100.0), CanvasOverlay.ImageRegion.BOTTOM_RIGHT, 0.1, paint);
                }
            };

    public boolean isReady() {
        return ready;
    }

    @Override
    protected void preInit() {
        super.preInit();

        ready = false;

        camera = new FastCamera(hardwareMap.appContext, R.id.cameraMonitorViewId);
        camera.setCameraViewListener(cameraViewListener);

        FastCameraView.Parameters parameters = camera.getParameters();
        parameters.maxPreviewHeight = 640;
        parameters.maxPreviewWidth = 640;
        parameters.previewScale = FastCameraView.PreviewScale.SCALE_TO_FIT;

        beacons = new ArrayList<Beacon>();

        this.fpsCounter = new FpsCounter();
    }

    @Override
    protected void postInitLoop() {
        telemetry.addData(">", ready ? "Camera is ready!" : "Camera is loading...");

        super.postInitLoop();
    }

    @Override
    public void stop() {
        camera.stop();
    }
}
