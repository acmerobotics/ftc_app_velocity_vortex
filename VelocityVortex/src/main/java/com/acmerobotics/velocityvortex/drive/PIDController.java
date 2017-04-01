package com.acmerobotics.velocityvortex.drive;

import com.qualcomm.robotcore.util.DifferentialControlLoopCoefficients;
import com.qualcomm.robotcore.util.Range;

/**
 * This class implements a regular PID controller.
 */
public class PIDController {

    private boolean outputBounded, inputBounded;
    private double minOutput, maxOutput, minInput, maxInput, maxSum;

    private DifferentialControlLoopCoefficients coeff;

    private double sum, lastError, lastTime, deriv;

    public PIDController(DifferentialControlLoopCoefficients coefficients) {
        coeff = coefficients;
    }

    public double getError(double actual, double setpoint) {
        double error = actual - setpoint;
        if (inputBounded) {
            double inputRange = maxInput - minInput;
            while (Math.abs(error) > inputRange / 2.0) {
                error -= Math.signum(error) * inputRange;
            }
        }
        return error;
    }

    public double update(double actual, double setpoint) {
        return update(getError(actual, setpoint));
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

            // cap sum to prevent integral windup
            if (maxSum != 0 && Math.abs(sum) > maxSum) {
                sum = Math.signum(sum) * Math.abs(maxSum);
            }

            deriv = (error - lastError) / dt;
            update = coeff.p * error + coeff.i * sum + coeff.d * deriv;
        }

        lastError = error;
        lastTime = time;

        // bound output
        if (outputBounded) {
            update = Range.clip(update, minOutput, maxOutput);
        }

        return update;
    }

    public void setOutputBounds(double min, double max) {
        if (min < max) {
            outputBounded = true;
            minOutput = min;
            maxOutput = max;
        }
    }

    public double getMinOutput() {
        return minOutput;
    }

    public double getMaxOutput() {
        return maxOutput;
    }

    public boolean isOutputBounded() {
        return outputBounded;
    }

    public void clearOutputBounds() {
        outputBounded = false;
    }

    public void setInputBounds(double min, double max) {
        if (min < max) {
            inputBounded = true;
            minInput = min;
            maxInput = max;
        }
    }

    public double getMinInput() {
        return minInput;
    }

    public double getMaxInput() {
        return maxInput;
    }

    public boolean isInputBounded() {
        return inputBounded;
    }

    public void clearInputBounds() {
        inputBounded = false;
    }

    public void setMaxSum(double max) {
        maxSum = max;
    }

    public double getMaxSum() {
        return maxSum;
    }

    public void reset() {
        lastTime = 0;
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
