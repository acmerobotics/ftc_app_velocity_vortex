package com.acmerobotics.velocityvortex.sensors;

/**
 * @author Ryan
 */

public class AverageDifferentiator {

    private long[] times;
    private double[] values;
    private int now, ms, bins;

    public AverageDifferentiator(int bins, int ms) {
        values = new double[bins];
        times = new long[bins];
        this.ms = ms;
        this.bins = bins;
    }

    public double update(double value) {
        long time = System.currentTimeMillis();
        long desiredTime = time - ms;
        now = (now + 1) % bins;
        times[now] = time;
        values[now] = value;
        for (int i = (now + 1) % bins; i != now; i = (i + 1) % bins) {
            if (times[i] > desiredTime) {
                return (value - values[i]) / (time - times[i]);
            }
        }
        return 0;
    }

}
