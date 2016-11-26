package com.acmerobotics.velocityvortex.drive;

/**
 * This class implements a regular PID controller.
 */
public class PIDController {

    private PIDCoefficients coeff;

    private double sum, lastError, lastTime, deriv;

    /**
     * This class contains the necessary parameters to configure
     * a PID controller.
     */
    public static class PIDCoefficients {
        private double p, i, d;

        public PIDCoefficients(double p, double i, double d) {
            this.p = p;
            this.i = i;
            this.d = d;
        }

        public double p() {
            return p;
        }

        public double i() {
            return i;
        }

        public double d() {
            return d;
        }
    }

    public PIDController(PIDCoefficients coefficients) {
        coeff = coefficients;
    }

    /**
     * Run a single iteration of the feedback update with the provided
     * error.
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

}
