package com.acmerobotics.library.configuration;

import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * @author Ryan
 */

public class WheelType {
    private MotorType motorType;
    private DcMotorSimple.Direction direction;
    private double gearRatio, wheelRadius;

    public WheelType(MotorType type, DcMotorSimple.Direction dir, double ratio, double radius) {
        motorType = type;
        direction = dir;
        gearRatio = ratio;
        wheelRadius = radius;
    }

    public MotorType getMotorType() {
        return motorType;
    }

    public DcMotorSimple.Direction getDirection() {
        return direction;
    }

    public double getGearRatio() {
        return gearRatio;
    }

    public double getWheelRadius() {
        return wheelRadius;
    }

    public double getCPR() {
        return motorType.getCPR() * gearRatio;
    }

    public double getRPM() {
        return motorType.getRPM() / gearRatio;
    }

    public int getCounts(double inches) {
        double circum = 2 * Math.PI * wheelRadius;
        int counts = (int) Math.round((inches * getCPR()) / circum);
        return counts;
    }
}
