package com.acmerobotics.velocityvortex.localization;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.vuforia.Image;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by kelly on 10/18/2016.
 */

@Autonomous(name="Concept: Vuforia Navigation", group ="Concept")
public class VuforiaTest extends OpMode{

    public static final String TAG = "Target Test";

    OpenGLMatrix lastLocation = null;
    VuforiaLocalizer vuforia;
    List<VuforiaTrackable> allTrackables;
    BlockingQueue<VuforiaLocalizer.CloseableFrame> queue;

    @Override
    public void init() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(R.id.cameraMonitorViewId);
        parameters.vuforiaLicenseKey = "AaNzdGn/////AAAAGVCiwQaxg01ft7Lw8kYMP3aE00RU5hyTkE1CNeaYi16CBF0EC/LWi50VYsSMdJITYz6jBTmG6UGJNaNXhzk1zVIggfVmGyEZFL5doU6eVaLdgLyVmJx6jLgNzSafXSLnisXnlS+YJlCaOh1pwk08tWM8Oz+Au7drZ4BkO8j1uluIkwiewRu5zDZGlbNliFfYeCRqslBEZCGxiuH/idcsD7Q055Bwj+f++zuG3x4YlIGJCHrTpVjJUWEIbdJzJVgukc/vVOz21UNpY6WoAwH5MSeh4/U6lYwMZTQb4icfk0o1EiBdOPJKHsxyVF9l00r+6Mmdf6NJcFTFLoucvPjngWisD2T/sjbtq9N+hHnKRpbK";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables images = this.vuforia.loadTrackablesFromAsset("targetImages");
        VuforiaTrackable toolsTarget = images.get(0);
        toolsTarget.setName("ToolsTarget");

        VuforiaTrackable legoesTarget = images.get(1);
        legoesTarget.setName("LegoesTarget");

        VuforiaTrackable wheelsTarget = images.get(2);
        wheelsTarget.setName("WheelsTarget");

        VuforiaTrackable gearsTarget = images.get(3);
        gearsTarget.setName("GearsTarget");

        allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(images);

        //only used for testing, just carring about position relative to target
        OpenGLMatrix noTransform = OpenGLMatrix.translation(0,0,0);

        toolsTarget.setLocation(noTransform);
        legoesTarget.setLocation(noTransform);
        wheelsTarget.setLocation(noTransform);
        gearsTarget.setLocation(noTransform);

        ((VuforiaTrackableDefaultListener)toolsTarget.getListener()).setPhoneInformation(noTransform, parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)legoesTarget.getListener()).setPhoneInformation(noTransform, parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)wheelsTarget.getListener()).setPhoneInformation(noTransform, parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)gearsTarget.getListener()).setPhoneInformation(noTransform, parameters.cameraDirection);

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();

        images.activate();
        vuforia.setFrameQueueCapacity(10);
        queue = vuforia.getFrameQueue();
    }

    @Override
    public void loop() {

        telemetry.addData("size", queue.remainingCapacity());


        VuforiaLocalizer.CloseableFrame frame;
        telemetry.addData("frames", queue.size());
        if (queue.isEmpty()) {
            telemetry.addData("frames", "empty");
        } else {

            try {
                frame = queue.remove();
            } catch (Exception ie) {}

        }


        for (VuforiaTrackable trackable : allTrackables) {
            /**
             * getUpdatedRobotLocation() will return null if no new information is available since
             * the last time that call was made, or if the trackable is not currently visible.
             * getRobotLocation() will return null if the trackable is not currently visible.
             */
            OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener) trackable.getListener()).getUpdatedRobotLocation();
            if (((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()){
                if (robotLocationTransform != null) {
                    lastLocation = robotLocationTransform;
                }
                telemetry.addData(trackable.getName(), lastLocation.formatAsTransform());
            }
        }
    }
}
