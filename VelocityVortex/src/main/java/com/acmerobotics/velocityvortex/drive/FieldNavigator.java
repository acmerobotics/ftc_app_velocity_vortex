package com.acmerobotics.velocityvortex.drive;

import android.util.Log;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.acmerobotics.velocityvortex.opmodes.Auto;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * @author Ryan
 */

public class FieldNavigator {

    private EnhancedMecanumDrive drive;

    private OpModeConfiguration.AllianceColor allianceColor;
    private double x, y;

    public FieldNavigator(EnhancedMecanumDrive drive, OpModeConfiguration.AllianceColor color) {
        Log.i("FieldNavigator", "init " + color);

        allianceColor = color;
        this.drive = drive;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void moveTo(double x, double y, LinearOpMode opMode) {
        moveTo(x, y, Double.POSITIVE_INFINITY, opMode);
    }

    public void moveTo(double x, double y, double finalHeading, LinearOpMode opMode) {
        double dx = x - this.x;
        double dy = y - this.y;

        double r = Math.hypot(dx, dy);
        double targetHeading = sanitizeHeading(90 - Math.toDegrees(Math.atan2(dy, dx)));
        double currentHeading = sanitizeHeading(drive.getHeading());

        if (allianceColor == OpModeConfiguration.AllianceColor.BLUE) {
            targetHeading = -targetHeading;
            finalHeading = -finalHeading;
        }

        double headingDiff = targetHeading - currentHeading;
        while (Math.abs(headingDiff) > 180) {
            headingDiff -= Math.signum(headingDiff) * 360;
        }
        if (Math.abs(headingDiff) > 90) {
            targetHeading = sanitizeHeading(targetHeading + 180);
            r = -r;
        }

        Log.i("FieldNavigator", String.format("heading:\t%f,%f,%f,%f\t\tradius:\t%f", currentHeading, targetHeading, finalHeading, headingDiff, r));

        drive.setTargetHeading(targetHeading);
        drive.turnSync(0, opMode);

        drive.getDrive().move(r, Auto.MOVEMENT_SPEED, opMode);

        if (Math.abs(finalHeading) != Double.POSITIVE_INFINITY) {
            finalHeading = sanitizeHeading(finalHeading);
            drive.setTargetHeading(finalHeading);
            drive.turnSync(0, opMode);
        }

        this.x = x;
        this.y = y;
    }

    private static double sanitizeHeading(double h) {
        double heading = h % 360;
        if (Math.abs(heading) > 180) {
            heading -= Math.signum(heading) * 360;
        }
        return heading;
    }

}
