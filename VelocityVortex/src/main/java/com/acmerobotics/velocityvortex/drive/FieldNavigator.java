package com.acmerobotics.velocityvortex.drive;

import android.util.Log;

import com.acmerobotics.library.configuration.OpModeConfiguration;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * @author Ryan
 */

public class FieldNavigator {

    private EnhancedMecanumDrive drive;

    private OpModeConfiguration.AllianceColor allianceColor;
    private double x, y, heading;

    public FieldNavigator(EnhancedMecanumDrive drive, OpModeConfiguration.AllianceColor color) {
        allianceColor = color;
        this.drive = drive;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setHeading(double heading) {
        this.heading = sanitizeHeading(heading);
    }

    public void moveTo(double x, double y, double finalHeading, LinearOpMode opMode) {
        double dx = x - this.x;
        double dy = y - this.y;
        finalHeading = sanitizeHeading(finalHeading);

        double r = Math.hypot(dx, dy);
        double theta = Math.toDegrees(Math.atan2(dy, dx)) - 90;

        double turn = theta - heading;
        if (turn > 90) {
            turn -= 180;
            r = -r;
        } else if (turn < -90) {
            turn += 180;
            r = -r;
        }

        if (allianceColor == OpModeConfiguration.AllianceColor.RED) {
            turn = -turn;
            finalHeading = -finalHeading;
        }

        drive.turnSync(turn, opMode);
        drive.move(r, 0.8, opMode);
        drive.setTargetHeading(finalHeading);
        drive.turnSync(0, opMode);

        this.x = x;
        this.y = y;
        this.heading = finalHeading;
    }

    private static double sanitizeHeading(double h) {
        double heading = h % 360;
        if (Math.abs(heading) > 180) {
            heading -= Math.signum(heading) * 360;
        }
        return heading;
    }

}
