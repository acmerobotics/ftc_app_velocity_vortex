package com.acmerobotics.velocityvortex.localization;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.text.MessageFormat;

/**
 * Stores positions in 2D with an orientation
 * Created by kelly on 10/23/2016.
 */

public class RobotLocation {

    public float x, y;
    private float heading;

    public static final RobotLocation ORIGIN = new RobotLocation(0, 0, 0);

    public RobotLocation(float x, float y, float heading) {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }

    public RobotLocation(OpenGLMatrix matrix) {
        matrixToLocation(matrix);
    }

    public static RobotLocation matrixToLocation (OpenGLMatrix matrix) {
        VectorF translation = matrix.getTranslation();
        Orientation orientation = Orientation.getOrientation(matrix, AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES);
        float x = translation.get(0);
        float y = translation.get(1);
        float heading = orientation.thirdAngle;
        return new RobotLocation(x, y, heading);
    }

    public OpenGLMatrix toMatrix () {
        return locationToMatrix(this);
    }

    public static OpenGLMatrix locationToMatrix (RobotLocation location) {
        return OpenGLMatrix
                .translation(location.x, location.y, 0)
                .multiplied(Orientation.getRotationMatrix(
                        /* First, in the fixed (field) coordinate system, we rotate 90deg in X, then 90 in Z */
                        AxesReference.EXTRINSIC, AxesOrder.XZX,
                        AngleUnit.DEGREES, -90, location.heading, 0));
    }

    public float getHeading () {
        return heading;
    }

    public void setHeading (float heading) {
        this.heading = degreeify(heading);
    }

    public String asString () {
        return MessageFormat.format("({0}, {1}) {2}", x, y, heading);
    }

    public float degreeify(float f) {
        return (f % 180);
    }
}
