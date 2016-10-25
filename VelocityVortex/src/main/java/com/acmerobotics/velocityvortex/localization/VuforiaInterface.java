package com.acmerobotics.velocityvortex.localization;

import com.vuforia.Frame;
import com.vuforia.Trackable;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.internal.VuforiaLocalizerImpl;
import org.firstinspires.ftc.teamcode.R;

import java.util.ArrayList;

/**
 * Handles all vuforia opperations
 * Created by kelly on 10/24/2016.
 */

public class VuforiaInterface extends LocalizationInterface{

    private VuforiaLocalizer vuforia;
    private ArrayList<VuforiaTrackable> allTrackables;

    public VuforiaInterface (String name, int priority) {
        super(name, priority);
        init();
    }

    public VuforiaLocalizer.CloseableFrame getFrame () {
        return new VuforiaLocalizer.CloseableFrame(new Frame());
    }

    @Override
    public void update() {

    }

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

        toolsTarget.setLocation(Field.toolsLocation.toMatrix());
        legoesTarget.setLocation(Field.legoesLocation.toMatrix());
        wheelsTarget.setLocation(Field.wheelsLocation.toMatrix());
        gearsTarget.setLocation(Field.gearsLocation.toMatrix());

        ((VuforiaTrackableDefaultListener)toolsTarget.getListener()).setPhoneInformation(RobotLocation.ORIGIN.toMatrix(), parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)legoesTarget.getListener()).setPhoneInformation(RobotLocation.ORIGIN.toMatrix(), parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)wheelsTarget.getListener()).setPhoneInformation(RobotLocation.ORIGIN.toMatrix(), parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)gearsTarget.getListener()).setPhoneInformation(RobotLocation.ORIGIN.toMatrix(), parameters.cameraDirection);

        images.activate();
    }
}
