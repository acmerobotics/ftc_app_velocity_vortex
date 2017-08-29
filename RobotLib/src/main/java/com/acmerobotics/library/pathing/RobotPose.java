package com.acmerobotics.library.pathing;

/**
 * Created by kelly on 8/23/2017.
 *
 */

public class RobotPose {

    private double x, y, heading;
    private long timestamp;

    public RobotPose(double x, double y, double heading) {
        this (x, y, heading, System.currentTimeMillis());
    }

    public RobotPose(double x, double y, double heading, long timestamp) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.timestamp = timestamp;
    }

    public double getVelocity (RobotPose prev) {
        return Math.hypot(x - prev.x, y - prev.y) / (timestamp - prev.timestamp) * 1000;
    }

    public double getAngularVelocity (RobotPose prev) {
        return (heading - prev.heading) / (timestamp - prev.timestamp);
    }

    public double getX () {
        return x;
    }

    public double getY () {
        return y;
    }

    public double getHeading () {
        return heading;
    }

    public long getTimestamp () {
        return timestamp;
    }

    public boolean coincident (RobotPose pose) {
        return x == pose.x && y == pose.y && heading == pose.heading;
    }
}
