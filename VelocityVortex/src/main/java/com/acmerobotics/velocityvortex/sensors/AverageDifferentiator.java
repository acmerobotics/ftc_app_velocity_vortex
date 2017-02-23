package com.acmerobotics.velocityvortex.sensors;

/**
 * @author Ryan
 */

public class AverageDifferentiator {

    public static final int STARTING_CAPACITY = 100;
    public static final int CAPACITY_INCR = 50;

    private long[] times;
    private double[] values;
    private int capacity, now, interval;
    private double lastVel;

    public AverageDifferentiator(int interval) {
        capacity = STARTING_CAPACITY;
        times = new long[capacity];
        values = new double[capacity];
        now = -1;
        this.interval = interval;
    }

    public double update(double value) {
        return update(System.currentTimeMillis(), value);
    }

    public double update(long time, double value) {
        if (value == values[now]) {
            return lastVel;
        }
        int i = now;
        while (true) {
            i = (i + 1) % capacity;
            long pastTime = times[i];

            if (i == now || pastTime == 0) {
                // didn't find a value
                if (i == now) {
                    // need more room
                    expand(CAPACITY_INCR);
                }
                lastVel = 0;
                break;
            }

            if ((time - pastTime) > interval) {
                // found the value
                double dt = (time - pastTime);
                double vel = 0;
                if (dt != 0) {
                    vel = (value - values[i]) / dt;
                }
                lastVel = vel;
                break;
            }
        }
        add(time, value);
        return lastVel;
    }

    public double getLastDerivative() {
        return lastVel;
    }

    private void expand(int incr) {
        int newCapacity = capacity + incr;
        long[] newTimes = new long[newCapacity];
        double[] newValues = new double[newCapacity];

        System.arraycopy(times, 0, newTimes, 0, now + 1);
        System.arraycopy(times, now + 1, newTimes, now + 1 + incr, capacity - now - 1);

        System.arraycopy(values, 0, newValues, 0, now + 1);
        System.arraycopy(values, now + 1, newValues, now + 1 + incr, capacity - now - 1);

        capacity = newCapacity;
        times = newTimes;
        values = newValues;
    }

    public int getCapacity() {
        return capacity;
    }

    public void add(long time, double value) {
        now = (now + 1) % capacity;
        times[now] = time;
        values[now] = value;
    }
}
