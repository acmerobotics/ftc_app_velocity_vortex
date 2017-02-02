package com.acmerobotics.library.configuration;

/**
 * @author Ryan
 */

public enum MotorType {
    ANDYMARK_UNGEARED (28, 40 * 160),
    ANDYMARK_20 (20 * 28, 315),
    ANDYMARK_40 (40 * 28, 160),
    ANDYMARK_60 (60 * 28, 105);

    private double cpr, rpm;

    MotorType(double cpr, double rpm) {
        this.cpr = cpr;
        this.rpm = rpm;
    }

    public double getCPR() {
        return cpr;
    }

    public double getRPM() {
        return rpm;
    }
}