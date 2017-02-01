package com.acmerobotics.library.configuration;

/**
 * @author Ryan
 */

public enum MotorType {
    ANDYMARK_UNGEARED (28),
    ANDYMARK_20 (20 * 28),
    ANDYMARK_40 (40 * 28),
    ANDYMARK_60 (60 * 28),
    ANDYMARK_3_7 (44),
    TETRIX (1440);

    private double cps;

    MotorType(int cps) {
        this.cps = cps;
    }

    public double getCps() {
        return cps;
    }
}