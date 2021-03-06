package com.acmerobotics.velocityvortex.drive;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Ryan on 4/5/2017.
 */
public class LinearPath {

    public static final double EPSILON = 0.0001;

    public static class DistanceReport {
        public Segment segment;
        public Vector2d queryPoint, pathPoint;
        public double distance;
    }

    public static class Waypoint {
        public final Vector2d point;
        public final double heading;

        public Waypoint(Vector2d point, double heading) {
            this.point = point;
            this.heading = heading;
        }

        public Waypoint(Vector2d point) {
            this(point, Double.NaN);
        }

        public Waypoint(double x, double y) {
            this(new Vector2d(x, y));
        }

        public Waypoint(double x, double y, double heading) {
            this(new Vector2d(x, y), heading);
        }
    }

    public static class Segment {
        public final Vector2d start, end, seg;
        public final double finalHeading;

        public Segment(Waypoint start, Waypoint end) {
            this.start = start.point;
            this.end = end.point;
            this.finalHeading = end.heading;
            this.seg = this.start.negated().add(this.end);
        }

        public double length() {
            return this.seg.norm();
        }

        /** @param t range [0, 1] inclusive */
        public Vector2d getPoint(double t) {
            return this.seg.multiplied(t).add(this.start);
        }

        public Vector2d getBoundedPoint(double t) {
            if (t <= 0) {
                return this.start.copy();
            } else if (t >= 1) {
                return this.end.copy();
            } else {
                return getPoint(t);
            }
        }

        /** @return [0, 1] position on curve; NaN if not on curve */
        public double getPosition(Vector2d point) {
            Vector2d adj = this.start.negated().add(point);
            double tX = adj.x() / seg.x();
            double tY = adj.y() / seg.y();
            if (Math.abs(seg.x()) < EPSILON) {
                return tY;
            } else if (Math.abs(seg.y()) < EPSILON) {
                return tX;
            } else if (Math.abs(tX - tY) < EPSILON) {
                return (tX + tY) / 2.0;
            } else {
                return Double.NaN;
            }
        }

        public boolean contains(Vector2d point) {
            double pos = getPosition(point);
            return !Double.isNaN(pos) && pos >= 0 && pos <= 1;
        }

        public double getClosestPositionOnPath(Vector2d point) {
            double a = seg.dot(start.negated().add(point));
            return a / seg.dot(seg);
        }

        public static double getDistance(Vector2d a, Vector2d b) {
            return a.negated().add(b).norm();
        }

        public boolean equals(Segment s) {
            return s.start.equals(start) && s.end.equals(end);
        }

        @Override
        public String toString() {
            return String.format("<%f, %f> to <%f, %f>", start.x(), start.y(), end.x(), end.y());
        }

        public String getEquation() {
            return String.format("<%f + %ft, %f + %ft>", start.x(), seg.x(), start.y(), seg.y());
        }
    }

    private List<Segment> segments;
    private double length;

    public LinearPath(List<Waypoint> waypoints) {
        segments = new ArrayList<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Segment s = new Segment(waypoints.get(i), waypoints.get(i+1));
            segments.add(s);
            length += s.length();
        }
    }

    @Override
    public String toString() {
        String str = "";
        for (Segment s : segments) {
            str += s + "\n";
        }
        return str;
    }

    public Segment getSegment(Vector2d point) {
        for (Segment s : segments) {
            if (s.contains(point)) {
                return s;
            }
        }
        return null;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public double length() {
        return length;
    }

    public double getPosition(Vector2d point) {
        double l = 0;
        for (Segment s : segments) {
            if (s.contains(point)) {
                return (l + s.getPosition(point) * s.length()) / length;
            }
            l += s.length();
        }
        return Double.NaN;
    }

    public Vector2d getPoint(double t) {
        double s = t * length;
        if (s <= 0) {
            return segments.get(0).start;
        }
        for (Segment seg : segments) {
            double segLength = seg.length();
            if (segLength > s) {
                return seg.getPoint(s / segLength);
            }
            s -= segLength;
        }
        return segments.get(segments.size() - 1).end;
    }

    public DistanceReport getDistanceReport(Vector2d point) {
        DistanceReport report = new DistanceReport();
        report.distance = Double.POSITIVE_INFINITY;
        for (Segment s : segments) {
            double t = s.getClosestPositionOnPath(point);
            double distance = s.getDistance(point, s.getBoundedPoint(t));
            if (distance < report.distance) {
                report.segment = s;
                report.distance = distance;
                report.queryPoint = point;
                report.pathPoint = s.getBoundedPoint(t);
            }
        }
        return report;
    }

    public Vector2d getLookaheadPoint(Vector2d source, double distance) {
        // solve quadratic for circle/segment intersection
        DistanceReport report = getDistanceReport(source);
        for (int i = segments.indexOf(report.segment); i < segments.size(); i++) {
            Segment s = segments.get(i);

            // substitution to make the expressions simpler
            double u = s.start.x() - source.x();
            double v = s.start.y() - source.y();

            // simple quadratic coefficients
            double a = Math.pow(s.seg.x(), 2) + Math.pow(s.seg.y(), 2);
            double b = 2 * (s.seg.x() * u + s.seg.y() * v);
            double c = Math.pow(u, 2) + Math.pow(v, 2) - Math.pow(distance, 2);

            // only care about the positive solution because t must be in [0, 1]
            double disc = Math.pow(b, 2) - 4 * a * c;
            if (disc >= 0) {
                double t = (-b + Math.sqrt(disc)) / (2 * a);
                if (t >= 0 && t <= 1) {
                    return s.getBoundedPoint(t);
                }
            }
        }
        return segments.get(segments.size() - 1).end.copy();
    }
}
