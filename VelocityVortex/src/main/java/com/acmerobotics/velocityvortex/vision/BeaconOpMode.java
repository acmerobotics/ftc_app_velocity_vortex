package com.acmerobotics.velocityvortex.vision;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.acmerobotics.library.camera.CanvasOverlay;
import com.acmerobotics.library.camera.FastCameraView;
import com.acmerobotics.library.camera.FpsCounter;
import com.acmerobotics.library.camera.FrameListener;
import com.acmerobotics.library.camera.SimpleCamera;
import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.R;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BeaconOpMode extends OpMode {

    protected SimpleCamera camera;
    protected FpsCounter fpsCounter;
    protected List<Beacon> beacons;
    private boolean ready;

    private FrameListener frameListener =
            new FrameListener() {
                @Override
                public void onCameraViewStarted(int width, int height) {
                    ready = true;
                    fpsCounter.init();
                }

                @Override
                public void onCameraViewStopped() {
                    ready = false;
                }

                public void onCameraFrame(Mat image) {
                    fpsCounter.measure();

                    Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);

                    beacons.clear();
                    BeaconAnalyzer.analyzeImage(image, beacons);

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

        camera = new SimpleCamera((Activity) hardwareMap.appContext, R.id.cameraMonitorViewId);
        camera.setFrameListener(frameListener);

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
