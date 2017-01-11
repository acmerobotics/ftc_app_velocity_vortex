package com.acmerobotics.velocityvortex.sensors;

/**
 * @author Ryan Brott
 */

public class ExponentialSmoother {

    private double exp, avg;
    private boolean hasUpdated;

    public ExponentialSmoother(double exp) {
        this.exp = exp;
    }

    public double update(double val) {
        if (!hasUpdated) {
            avg = val;
            hasUpdated = true;
        } else {
            avg = exp * val + (1 - exp) * avg;
        }
        return avg;
    }

}
