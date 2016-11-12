package com.acmerobotics.velocityvortex.localization;

import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.CameraDevice;
import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.VideoMode;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.R;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * Handles all vuforia opperations
 * Created by kelly on 10/24/2016.
 */

public class VuforiaInterface extends LocalizationInterface{

    private VuforiaLocalizer vuforia;
    private OpenGLMatrix locationTransform = RobotLocation.ORIGIN.toMatrix();
    private ArrayList<VuforiaTrackable> allTrackables;
    private Mat internal;
    private byte[] imgData;
    private int frameWidth, frameHeight;

    private BlockingQueue<VuforiaLocalizer.CloseableFrame> frameQueue;

    public VuforiaInterface (String name, int priority) {
        super(name, priority);
        init();
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public boolean getFrame(Mat mat) {
        VuforiaLocalizer.CloseableFrame frame;
        if (frameQueue.isEmpty()) return false;
        try {
            frame = frameQueue.take();
        } catch (Exception e) {
            RobotLog.e("Problem getting the frame");
            return false;
        }
        for (int i = 0; i < frame.getNumImages(); i++) {
            Image img = frame.getImage(i);
            if (img.getFormat() == PIXEL_FORMAT.RGB565) {
//                if (mat.type() != CvType.CV_8UC3 || mat.width() != img.getWidth() || mat.height() != img.getHeight()) {
//                    throw new RuntimeException("Invalid Mat: should be of type CV_8UC3 with " + img.getHeight() + " rows and " + img.getWidth() + " cols");
//                }
                ByteBuffer byteBuffer = img.getPixels();
                if (imgData == null || imgData.length != byteBuffer.capacity()) {
                    imgData = new byte[byteBuffer.capacity()];
                }
                if (internal == null || internal.width() != img.getWidth() || internal.height() != img.getHeight()) {
                    internal = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC2);
                }
                byteBuffer.get(imgData);
                internal.put(0, 0, imgData);
                Imgproc.cvtColor(internal, mat, Imgproc.COLOR_BGR5652BGR);

                frame.close();

                return true;
            }
        }
        return false;
    }

    public OpenGLMatrix getLocationTransform () {
        return locationTransform;
    }

    @Override
    public void update() {

        for (VuforiaTrackable trackable : allTrackables) {
            /**
             * getUpdatedRobotLocation() will return null if no new information is available since
             * the last time that call was made, or if the trackable is not currently visible.
             * getRobotLocation() will return null if the trackable is not currently visible.
             */
            OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener) trackable.getListener()).getUpdatedRobotLocation();
            if (((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()){
                if (robotLocationTransform != null) {
                    location = RobotLocation.matrixToLocation(robotLocationTransform);
                    locationTransform = robotLocationTransform;
                }
                return;
            }
        }
        location = null;
    }

    private void init() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(R.id.cameraMonitorViewId);
        parameters.vuforiaLicenseKey = "AaNzdGn/////AAAAGVCiwQaxg01ft7Lw8kYMP3aE00RU5hyTkE1CNeaYi16CBF0EC/LWi50VYsSMdJITYz6jBTmG6UGJNaNXhzk1zVIggfVmGyEZFL5doU6eVaLdgLyVmJx6jLgNzSafXSLnisXnlS+YJlCaOh1pwk08tWM8Oz+Au7drZ4BkO8j1uluIkwiewRu5zDZGlbNliFfYeCRqslBEZCGxiuH/idcsD7Q055Bwj+f++zuG3x4YlIGJCHrTpVjJUWEIbdJzJVgukc/vVOz21UNpY6WoAwH5MSeh4/U6lYwMZTQb4icfk0o1EiBdOPJKHsxyVF9l00r+6Mmdf6NJcFTFLoucvPjngWisD2T/sjbtq9N+hHnKRpbK";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables images = this.vuforia.loadTrackablesFromAsset("velocityVortex");
        VuforiaTrackable wheelsTarget = images.get(2);
        wheelsTarget.setName("wheelsTarget");

        VuforiaTrackable toolsTarget = images.get(0);
        toolsTarget.setName("toolsTarget");

        VuforiaTrackable legoesTarget = images.get(1);
        legoesTarget.setName("legoesTarget");

        VuforiaTrackable gearsTarget = images.get(3);
        gearsTarget.setName("GearsTarget");

        allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(images);

        toolsTarget.setLocation(Field.toolsLocation.toMatrix());
        legoesTarget.setLocation(Field.legoesLocation.toMatrix());
        wheelsTarget.setLocation(Field.wheelsLocation.toMatrix());
        gearsTarget.setLocation(Field.gearsLocation.toMatrix());

        ((VuforiaTrackableDefaultListener)toolsTarget.getListener()).setPhoneInformation(RobotLocation.ORIGIN.toMatrix(), parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)legoesTarget.getListener()).setPhoneInformation(RobotLocation.ORIGIN.toMatrix(), parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)wheelsTarget.getListener()).setPhoneInformation(RobotLocation.ORIGIN.toMatrix(), parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)gearsTarget.getListener()).setPhoneInformation(RobotLocation.ORIGIN.toMatrix(), parameters.cameraDirection);

        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode videoMode = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        frameWidth = videoMode.getWidth();
        frameHeight = videoMode.getHeight();

        images.activate();
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
        vuforia.setFrameQueueCapacity(1);
        frameQueue = vuforia.getFrameQueue();
    }
}
