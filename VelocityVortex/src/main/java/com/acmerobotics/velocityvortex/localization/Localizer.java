package com.acmerobotics.velocityvortex.localization;

import com.vuforia.Frame;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.opencv.core.Mat;

/**
 * Manages all Localization Interfaces, handle for main opmode to access localization data and camera frames
 * Created by kelly on 10/23/2016.
 */

public class Localizer {

    private VuforiaInterface vuforia;

    public Mat getFrame () {
        return vuforia.getFrame();
    }
}
