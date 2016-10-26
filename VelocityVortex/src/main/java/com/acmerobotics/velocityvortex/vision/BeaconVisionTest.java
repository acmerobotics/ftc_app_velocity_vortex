package com.acmerobotics.velocityvortex.vision;

import android.app.Activity;

import com.acmerobotics.library.vision.ImageOverlay;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

@Autonomous(name="Beacon Test", group="Test")
public class BeaconVisionTest extends OpMode {

    public OpenCvCamera camera;
    private FpsCounter fpsCounter;

    private FastCameraView.CvCameraViewListener2 cameraViewListener =
            new FastCameraView.CvCameraViewListener2() {
                @Override
                public void onCameraViewStarted(int width, int height) {
                    fpsCounter.init();
                }

                @Override
                public void onCameraViewStopped() {

                }

                @Override
                public Mat onCameraFrame(FastCameraView.CvCameraViewFrame inputFrame) {
                    fpsCounter.measure();

                    Mat color = inputFrame.rgba();
                    Mat colorT = color.t();
                    Core.flip(color.t(), colorT, 1);
                    Imgproc.resize(colorT, colorT, color.size());

                    ImageOverlay overlay = new ImageOverlay(colorT);

                    int roundedFps = (int) Math.round(fpsCounter.fps());

                    overlay.drawText(roundedFps + " FPS", ImageOverlay.ImageRegion.TOP_LEFT, Core.FONT_HERSHEY_SIMPLEX, 0.15, new Scalar(255, 255, 0), 3);

                    return colorT;
                }
            };

    @Override
    public void init() {
        this.camera = new OpenCvCamera((Activity) hardwareMap.appContext, com.qualcomm.ftcrobotcontroller.R.id.cameraMonitorViewId);
        this.camera.start();
        this.camera.setCameraListener(cameraViewListener);

        this.fpsCounter = new FpsCounter();
    }

    @Override
    public void loop() {
        telemetry.addData("fps", fpsCounter.fps());
    }

    @Override
    public void stop() {
        this.camera.stop();
    }
}
