package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.matrices.VectorF;

/**
 * This class implements the basic functionality of an omnidirectional
 * mecanum wheel drive system.
 */
public class MecanumDrive {

    public MecanumDrive(HardwareMap map) {

    }

    /**
     * Sets the movement direction vector relative to the axis parallel
     * to the wheels. The magnitude of this vector describes the movement
     * speed and should be in the range [0, 1] inclusive.
     * @param vector the direction vector
     */
    public void setDirection(VectorF vector) {

    }

    /**
     * Sets the movement direction of motion using polar coordinates. The
     * angle is measured clockwise from the axis parallel to the wheels.
     * @param r the radius
     * @param theta the angle in radians
     */
    public void setDirection(float r, float theta) {
        setDirection(new VectorF(r * (float) Math.cos(theta), r * (float) Math.sin(theta)));
    }

}
