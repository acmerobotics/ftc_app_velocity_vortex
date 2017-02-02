package com.acmerobotics.library.configuration;

/**
 * @author Ryan
 */

public class WheelType {
    private MotorType motorType;
    private double gearRatio, wheelRadius;

    public WheelType(MotorType type, double ratio, double radius) {
        motorType = type;
        gearRatio = ratio;
        wheelRadius = radius;
    }

    public MotorType getMotorType() {
        return motorType;
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
