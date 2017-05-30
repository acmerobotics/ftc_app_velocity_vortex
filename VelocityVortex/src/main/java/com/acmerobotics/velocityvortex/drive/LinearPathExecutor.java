package com.acmerobotics.velocityvortex.drive;

import com.acmerobotics.velocityvortex.opmodes.Auto;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.List;

/**
 * @author Ryan
 */

public class LinearPathExecutor {

    private List<LinearPath.Segment> path;
    private int currentSegment, mirrorModifier;
    private HolonomicDrive drive;
    private boolean mirror;

    public LinearPathExecutor(HolonomicDrive drive, LinearPath path) {
        this(drive, path, false);
    }

    public LinearPathExecutor(HolonomicDrive drive, LinearPath path, boolean mirror) {
        setPath(path);
        this.drive = drive;
        this.mirror = mirror;
        this.mirrorModifier = mirror ? -1 : 1;
    }

    public void execute(LinearOpMode opMode) {
        execute(1, opMode);
    }

    public void execute(int n, LinearOpMode opMode) {
        for (int i = 0; i < n; i++) {
            LinearPath.Segment segment = path.get(currentSegment);
            double startHeading = Math.toDegrees(Math.atan2(segment.seg.x(), segment.seg.y()));
            double distance = segment.seg.norm();
            drive.setTargetHeading(mirrorModifier * startHeading);
            drive.turnSync(0, opMode);
            drive.move(distance, Auto.MOVEMENT_SPEED, opMode);
            if (!Double.isNaN(segment.finalHeading)) {
                drive.setTargetHeading(mirrorModifier * segment.finalHeading);
                drive.turnSync(0, opMode);
            }
            currentSegment++;
        }
    }

    public void executeAll(LinearOpMode opMode) {
        execute(path.size() - currentSegment, opMode);
    }

    public void skip() {
        skip(1);
    }

    public void skip(int n) {
        currentSegment += n;
    }

    public void setPath(LinearPath path) {
        this.path = path.getSegments();
        this.currentSegment = 0;
    }

}
