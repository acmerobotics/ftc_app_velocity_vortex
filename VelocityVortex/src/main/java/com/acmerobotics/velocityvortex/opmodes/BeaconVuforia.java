package com.acmerobotics.velocityvortex.opmodes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.acmerobotics.library.camera.CanvasOverlay;
import com.acmerobotics.library.vision.Beacon;
import com.acmerobotics.library.vision.BeaconAnalyzer;
import com.acmerobotics.velocityvortex.localization.VuforiaInterface;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.AppUtil;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Autonomous(name="Beacon Vuforia")
public class BeaconVuforia extends OpMode {

    private AppUtil appUtil = AppUtil.getInstance();
    private VuforiaInterface vuforia;
    private Mat[] images;
    private boolean hasNewImage;
    private BeaconWorker beaconWorker;
    private List<Beacon> beacons;
    private BeaconOverlay overlayView;
    private FrameLayout frameLayout;
    private AtomicInteger counter;

    private BeaconLoaderCallback loaderCallback;

    public class BeaconOverlay extends View {

        Paint paint = new Paint();

        private Comparator<Beacon> beaconComparator = new Comparator<Beacon>() {

            @Override
            public int compare(Beacon o1, Beacon o2) {
                Size s1 = o1.getBounds().size;
                Size s2 = o2.getBounds().size;
                double area1 = s1.width * s1.height;
                double area2 = s2.width * s2.height;
                return (area1 > area2) ? -1 : 1;
            }

        };

        public BeaconOverlay(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (beacons != null) {
                CanvasOverlay overlay = new CanvasOverlay(canvas, 15);

                paint.setColor(Color.RED);

                Collections.sort(beacons, beaconComparator);

                overlay.drawText("Test", CanvasOverlay.ImageRegion.BOTTOM_CENTER, 0.1, paint);

                for (Beacon result : beacons) {
                    int score = result.getScore().getNumericScore();

                    String description = "";
                    description += score + " " + result.getScore().toString() + "  ";
                    description += (result.getLeftRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B") + ",";
                    description += result.getRightRegion().getColor() == Beacon.BeaconColor.RED ? "R" : "B";

                    overlay.drawText(description, CanvasOverlay.ImageRegion.TOP_LEFT, 0.1, paint);
                }
            }

            super.onDraw(canvas);
        }
    }

    public class BeaconLoaderCallback extends BaseLoaderCallback {
        public BeaconLoaderCallback(Context ctx) {
            super(ctx);
        }

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    RobotLog.i("OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
            counter.decrementAndGet();
        }
    }

    public boolean hasLoaded() {
        return counter.get() == 0;
    }

    @Override
    public void init() {
        counter = new AtomicInteger(2);

        vuforia = new VuforiaInterface("beacon", 0);
        beacons = new ArrayList<>();

        initOverlay();
        initOpenCV();

        hasNewImage = false;
        beaconWorker = new BeaconWorker();
        beaconWorker.start();
    }

    public void initOverlay() {
        final Activity activity = (Activity) hardwareMap.appContext;
        appUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overlayView = new BeaconOverlay(activity);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                overlayView.setLayoutParams(layoutParams);
                frameLayout = (FrameLayout) activity.findViewById(com.qualcomm.ftcrobotcontroller.R.id.cameraMonitorViewId).getParent();
                frameLayout.addView(overlayView);
                counter.decrementAndGet();
            }
        });

    }

    public void initOpenCV() {
        loaderCallback = new BeaconLoaderCallback(hardwareMap.appContext);
        if (!OpenCVLoader.initDebug()) {
            RobotLog.d("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, hardwareMap.appContext, loaderCallback);
        } else {
            RobotLog.d("OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void init_loop() {
        telemetry.addData(">", hasLoaded() ? "Vision loaded" : "Vision is loading...");
    }

    @Override
    public void loop() {
        if (images == null) {
            images = new Mat[2];
            images[0] = new Mat();
            images[1] = new Mat();
        }

        vuforia.getFrame(images[0]);
        synchronized (this) {
            if (!images[0].empty()) {
                hasNewImage = true;
            }
            if (!beaconWorker.isProcessing() && hasNewImage) {
                this.notify();
            }
        }

        beacons = beaconWorker.getBeacons();
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
        appUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                frameLayout.removeView(overlayView);
            }
        });
        synchronized (this) {
            beaconWorker.terminate();
            hasNewImage = false;
            this.notify();
            try {
                beaconWorker.join();
            } catch (InterruptedException e) {
                RobotLog.e(e.getMessage());
            }
        }
    }

    public class BeaconWorker extends Thread {

        private static final int MAX_DIMENSION = 640;

        private boolean processing = false;
        private boolean running = true;
        private List<Beacon> beacons;

        public BeaconWorker() {
            this.beacons = new ArrayList<Beacon>();
        }

        public boolean isProcessing() {
            return processing;
        }

        public void terminate() {
            running = false;
        }

        public List<Beacon> getBeacons() {
            return beacons;
        }

        public Size getSmallSize(Size big) {
            if (big.width > big.height) {
                return new Size(MAX_DIMENSION, big.height * MAX_DIMENSION / big.width);
            } else {
                return new Size(big.width * MAX_DIMENSION / big.height, MAX_DIMENSION);
            }
        }

        @Override
        public void run() {
            while (running) {
                if (hasNewImage) {
                    hasNewImage = false;
                    processing = true;
                    synchronized (BeaconVuforia.this) {
                        Imgproc.resize(images[0], images[1], getSmallSize(images[0].size()));
                        beacons.clear();
                        BeaconAnalyzer.analyzeImage(images[1], beacons);
                        for (Beacon beacon : beacons) {
                            beacon.draw(images[1]);
                        }
                        Imgcodecs.imwrite(new File(Environment.getExternalStorageDirectory().getPath(), "frame" + System.currentTimeMillis() + ".jpg").getPath(), images[1]);
                        appUtil.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                overlayView.invalidate();
                            }
                        });
                    }
                    processing = false;
                } else {
                    synchronized (BeaconVuforia.this) {
                        try {
                            BeaconVuforia.this.wait();
                        } catch (InterruptedException e) {
                            RobotLog.e(e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
