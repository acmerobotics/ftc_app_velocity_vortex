package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;

import java.util.Locale;

/**
 * This class implements a regular PID controller.
 */
public class PIDController {

    private DifferentialControlLoopCoefficients coeff;

    private double sum, lastError, lastTime, deriv;

    public PIDController(DifferentialControlLoopCoefficients coefficients) {
        coeff = coefficients;
    }

    /**
     * Run a single iteration of the feedback update with the provided
     * error.
     *
     * @param error calculated error
     * @return the calculated correction (update)
     */
    public double update(double error) {
        // do the PID update
        double time = System.nanoTime() / Math.pow(10, 9);
        double update = 0;
        if (lastTime == 0) {
            // special handling for first iteration
            sum = 0;
        } else {
            double dt = time - lastTime;
            // sum computed using trapezoidal rule
            sum += (error + lastError) * dt / 2.0;
            deriv = (error - lastError) / dt;
            update = coeff.p * error + coeff.i * sum + coeff.d * deriv;
        }

        lastError = error;
        lastTime = time;

        return update;
    }

    public double getErrorSum() {
        return sum;
    }

    public double getErrorDerivative() {
        return deriv;
    }

    public DifferentialControlLoopCoefficients getCoefficients() {
        return coeff;
    }

    @Override
    public String toString() {
        return String.format("(%4.3f,%4.3f,%4.3f)", coeff.p, coeff.i, coeff.d);
    }

}
